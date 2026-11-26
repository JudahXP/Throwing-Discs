package dev.disc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ThrowingDiscEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(
        ThrowingDiscEntity.class, EntityDataSerializers.ITEM_STACK);
    private UUID thrower;
    private BlockPos dispenser;
    private int originSlot;
    private int age;
    private int bounces;
    private boolean returning;
    private double distance;
    private float curveDirection;
    private float visualBank;
    private double previousHeading;
    private boolean hasVisualHeading;
    public float visualBank() { return visualBank; }
    private void updateVisualBank() {
        Vec3 motion = getDeltaMovement();
        double heading = Math.atan2(motion.x, motion.z);
        double turn = Math.atan2(Math.sin(heading-previousHeading),Math.cos(heading-previousHeading));
        float target = hasVisualHeading ? (float)net.minecraft.util.Mth.clamp(-Math.toDegrees(turn)*3,-25,25) : 0;
        visualBank += (target-visualBank)*0.5F;
        previousHeading = heading;
        hasVisualHeading = true;
    }
    private final Set<UUID> hitThisPass = new HashSet<>();

    public ThrowingDiscEntity(EntityType<? extends ThrowingDiscEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }
    public void damageForThrow(ServerPlayer player, net.minecraft.world.InteractionHand hand) {
        ItemStack carried = getItem().copy();
        carried.hurtAndBreak(1,player,p -> p.broadcastBreakEvent(hand));
        setItem(carried);
        if (carried.isEmpty()) discard();
    }
    @Override public boolean displayFireAnimation() { return false; }
    public UUID throwerId() { return thrower; }
    @Override protected void defineSynchedData() {
        entityData.define(STACK, ItemStack.EMPTY);
    }
    @Override public ItemStack getItem() { return entityData.get(STACK); }
    private void setItem(ItemStack stack) { entityData.set(STACK, stack); }
    private Vec3 clientTarget; private int clientSteps;
    @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport) {clientTarget=new Vec3(x,y,z);clientSteps=Math.max(1,steps);}
    public ItemStack getWeaponItem() { return getItem(); }
    public void launch(ServerPlayer player, ItemStack stack, int slot) {
        setOwner(player);
        thrower = player.getUUID();
        originSlot = slot;
        DiscEnchantments.normalize(stack, level());
        setItem(stack);
        setPos(player.getX(), player.getEyeY() - 0.15, player.getZ());
        setDeltaMovement(player.getLookAngle().scale(DiscPhysics.SPEED));
        Vec3 right = new Vec3(-player.getLookAngle().z, 0, player.getLookAngle().x).normalize();
        double side = player.getDeltaMovement().dot(right);
        curveDirection = Math.abs(side) < 0.025 ? 0 : (float)Math.signum(side);


    }
    public double baseRange() {
        return getItem().getItem() instanceof ThrowingDiscItem disc ? disc.tier.range : 16;
    }
    public void launchFromDispenser(ServerLevel server, BlockPos pos, Vec3 direction, ItemStack stack, int slot) {
        dispenser = pos.immutable(); originSlot = slot;
        DiscEnchantments.normalize(stack, server); setItem(stack);
        setPos(Vec3.atCenterOf(pos).add(direction.scale(0.85)));
        setDeltaMovement(direction.scale(DiscPhysics.SPEED));
    }
    @Override public void tick() {
        super.tick();
        if(level().isClientSide()){updateVisualBank();if(clientSteps>0&&clientTarget!=null){setPos(position().lerp(clientTarget,1.0/clientSteps));clientSteps--;}}
        if (!(level() instanceof ServerLevel server)) { return; }
        if (getItem().isEmpty()) { discard(); return; }
        clearFire();
        DiscEnchantments.normalize(getItem(), server);
        DiscFlights.track(this);
        age++;
        ServerPlayer player = thrower == null ? null : server.getServer().getPlayerList().getPlayer(thrower);
        if (dispenser == null && (player == null || !player.isAlive() || player.isSpectator())) { dropStack(server); return; }
        if ((dispenser == null && player.level() != server) || age > DiscPhysics.MAX_FLIGHT_TICKS) { dropStack(server); return; }
        if (dispenser != null && (!server.hasChunkAt(dispenser) || !(server.getBlockEntity(dispenser) instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity))) { dropStack(server); return; }
        if (!returning && (age >= DiscPhysics.MAX_OUTBOUND_TICKS
            || getDeltaMovement().lengthSqr() < 1.0e-10
            || distance >= baseRange() + DiscPhysics.rangeBonus(enchantmentLevel("range")) + bounces * DiscPhysics.BOUNCE_BONUS - 1.0e-6)) beginReturn();
        if (returning) flyHome(player); else flyOutbound(server);
        if (!isRemoved()) keepFlightLoaded(server);
    }
    public void keepFlightLoaded(ServerLevel server) {
        // Short-lived shared tickets expire after flight; never remove another disc's ticket.
        server.getChunkSource().addRegionTicket(ModEntities.FLIGHT_TICKET, chunkPosition(), 2, chunkPosition());
    }
    private void beginReturn() {
        returning = true;
        hitThisPass.clear();
        sound(SoundEvents.TRIDENT_RETURN, 0.8F, 1.0F);
    }
    private void flyOutbound(ServerLevel server) {
        int curve = net.minecraft.util.Mth.clamp(enchantmentLevel("curve"),0,2);
        if (curve > 0 && curveDirection != 0) {
            setDeltaMovement(getDeltaMovement().yRot((float)Math.toRadians(-curveDirection * curve * 1.5)));
            hasImpulse = true;
        }
        steerTowardMob(server);
        double timeLeft = 1;
        // Resolve the unused fraction of a tick after each impact, including corners.
        for (int step = 0; step < 4 && timeLeft > 1.0e-5 && !returning; step++) {
            Vec3 start = position();
            double speed = getDeltaMovement().length();
            Vec3 move = getDeltaMovement().scale(timeLeft);
            Vec3 end = start.add(move);
            if (!server.hasChunk(BlockPos.containing(end).getX() >> 4, BlockPos.containing(end).getZ() >> 4)) { beginReturn(); return; }
            var hit = server.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
            boolean blocked = hit.getType() == HitResult.Type.BLOCK;
            if (blocked && breakPot(server, hit)) {
                Vec3 impact = hit.getLocation();
                pierce(server, start, impact);
                if (!returning) distance += start.distanceTo(impact);
                timeLeft -= start.distanceTo(impact) / Math.max(speed, 1.0e-9);
                setPos(impact.add(move.normalize().scale(0.001)));
                continue;
            }
            Vec3 finish = blocked ? hit.getLocation() : end;
            pierce(server, start, finish);
            distance += start.distanceTo(finish);
            setPos(finish);
            if (!blocked) { hasImpulse = true; return; }
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
            setPos(finish.add(normal.scale(0.015)));
            setDeltaMovement(DiscPhysics.reflect(getDeltaMovement(), normal));
            hasImpulse = true;
            sound(SoundEvents.TRIDENT_HIT_GROUND, 0.65F, 1.0F);
            bounces++;
            timeLeft -= start.distanceTo(finish) / Math.max(speed, 1.0e-9);
        }
    }
    private int vanillaEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantment enchantment){return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantment,getItem());}
    public int enchantmentLevel(String name){return vanillaEnchantmentLevel(DiscEnchantments.get(name));}
    private boolean breakPot(ServerLevel server, net.minecraft.world.phys.BlockHitResult hit) {
        var state = server.getBlockState(hit.getBlockPos());
        if (!state.is(net.minecraft.world.level.block.Blocks.DECORATED_POT)) return false;
        state.onProjectileHit(server, state, hit, this);
        return !server.getBlockState(hit.getBlockPos()).is(net.minecraft.world.level.block.Blocks.DECORATED_POT);
    }
    private void steerTowardMob(ServerLevel server) {
        int strength = enchantmentLevel("homing");
        if (strength <= 0) return;
        Vec3 forward = getDeltaMovement().normalize();
        net.minecraft.world.entity.Mob nearest = null;
        double nearestDistance = 100;
        for (var mob : server.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, getBoundingBox().inflate(10),
                e -> e.isAlive() && !e.isSpectator() && !hitThisPass.contains(e.getUUID()) && (getOwner() == null || !e.isAlliedTo(getOwner())))) {
            if (mob instanceof net.minecraft.world.entity.TamableAnimal pet && pet.isTame()) continue;
            Vec3 target = mob.getBoundingBox().getCenter();
            Vec3 delta = target.subtract(position());
            double squared = delta.lengthSqr();
            if (squared < nearestDistance && forward.dot(delta.normalize()) >= 0.7 && clearPath(position(),target)) {
                nearest = mob; nearestDistance = squared;
            }
        }
        if (nearest != null) {
            setDeltaMovement(DiscPhysics.steer(getDeltaMovement(), nearest.getBoundingBox().getCenter().subtract(position()), strength));
            hasImpulse = true;
        }
    }
    private boolean clearPath(Vec3 start, Vec3 end) {
        return level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }
    /** Administrative recovery must not move the stack through an obstruction either. */
    public void recoverFor(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        Vec3 target = player.position().add(0, player.getBbHeight() * 0.7, 0);
        if (player.level() == server && clearPath(position(), target)) catchBy(player);
        else dropStack(server);
    }
    private void flyHome(ServerPlayer player) {
        ServerLevel server = (ServerLevel)level();
        Vec3 target = dispenser == null ? player.position().add(0, player.getBbHeight() * 0.7, 0) : Vec3.atCenterOf(dispenser);
        setDeltaMovement(DiscPhysics.home(getDeltaMovement(), target.subtract(position())));
        double timeLeft = 1;
        for (int step = 0; step < 4 && timeLeft > 1.0e-5; step++) {
            Vec3 start = position();
            double speed = getDeltaMovement().length();
            double available = returning ? Double.POSITIVE_INFINITY : Math.max(0,
                baseRange() + DiscPhysics.rangeBonus(enchantmentLevel("range")) + bounces * DiscPhysics.BOUNCE_BONUS - distance);
            double travelTime = Math.min(timeLeft, available / Math.max(speed, 1.0e-9));
            Vec3 move = getDeltaMovement().scale(travelTime);
            Vec3 end = start.add(move);
            if (!server.hasChunk(BlockPos.containing(end).getX() >> 4, BlockPos.containing(end).getZ() >> 4)) {
                dropStack(server);
                return;
            }
            var hit = server.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
            // Only the part of the return inside the eight-block destination sphere may phase.
            boolean phase = returning && start.distanceToSqr(target) <= 64 && end.distanceToSqr(target) <= 64;
            boolean blocked = hit.getType() == HitResult.Type.BLOCK && !phase;
            if (blocked && breakPot(server, hit)) {
                Vec3 impact = hit.getLocation();
                pierce(server, start, impact);
                if (!returning) distance += start.distanceTo(impact);
                timeLeft -= start.distanceTo(impact) / Math.max(speed, 1.0e-9);
                setPos(impact.add(move.normalize().scale(0.001)));
                continue;
            }
            Vec3 finish = blocked ? hit.getLocation() : end;
            // Clip movement before checking catch distance; never catch across a thin wall.
            if (DiscPhysics.segmentWithin(start, finish, target, 0.8) && (start.distanceToSqr(target) <= 64 || clearPath(start, target))) {
                if (dispenser == null) catchBy(player); else catchByDispenser(server);
                return;
            }
            pierce(server, start, finish);
            setPos(finish);
            hasImpulse = true;
            if (!blocked) { hasImpulse = true; return; }
            if (hit.isInside()) { dropStack(server); return; }
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
            setPos(finish.add(normal.scale(0.015)));
            setDeltaMovement(DiscPhysics.reflect(getDeltaMovement(), normal));
            sound(SoundEvents.TRIDENT_HIT_GROUND, 0.65F, 1.0F);
            timeLeft -= start.distanceTo(finish) / Math.max(speed, 1.0e-9);
        }
    }
    private record Target(Entity entity, double distance) {}
    private void pierce(ServerLevel server, Vec3 start, Vec3 end) {
        DiscHarvest.harvest(server, this, start, end);
        var targets = new ArrayList<Target>();
        for (Entity entity : server.getEntities(this, new AABB(start, end).inflate(0.3),
                e -> (e instanceof LivingEntity || e instanceof net.minecraft.world.entity.projectile.LargeFireball) && e.isAlive() && !e.isSpectator()
                    && !e.getUUID().equals(thrower) && !hitThisPass.contains(e.getUUID()))) {
            AABB box = entity.getBoundingBox().inflate(0.2);
            if (box.contains(start)) targets.add(new Target(entity, 0));
            else box.clip(start, end).ifPresent(point -> targets.add(new Target(entity, start.distanceToSqr(point))));
        }
        targets.sort(Comparator.comparingDouble(Target::distance));
        float damage = getItem().getItem() instanceof ThrowingDiscItem disc ? disc.tier.damage : 4;
        damage = DiscPhysics.speedDamage(damage + net.minecraft.util.Mth.clamp(enchantmentLevel("keen_edge"),0,3), getDeltaMovement().length());
        Entity owner = getOwner();
        for (Target target : targets) {
            if (owner != null && owner.isAlliedTo(target.entity()) && owner.getTeam() != null && !owner.getTeam().isAllowFriendlyFire()) continue;
            hitThisPass.add(target.entity().getUUID());
            if (target.entity() instanceof net.minecraft.world.entity.projectile.LargeFireball fireball) {
                if(owner!=null){fireball.setDeltaMovement(fireball.getDeltaMovement().scale(-1));fireball.xPower=-fireball.xPower;fireball.yPower=-fireball.yPower;fireball.zPower=-fireball.zPower;fireball.setOwner(owner);}
                continue;
            }
            LivingEntity living = (LivingEntity)target.entity();
            if (living instanceof net.minecraft.world.entity.player.Player victim
                && owner instanceof net.minecraft.world.entity.player.Player attacker && !victim.canHarmPlayer(attacker)) continue;
            ItemStack blocking = (living.isBlocking()?living.getUseItem():null);
            boolean facing = blocking != null && living.getLookAngle().dot(position().subtract(living.position()).normalize()) > 0;
            boolean damaged = living.hurt( damageSources().thrown(this, owner), damage);
            if (facing) {
                if(living instanceof net.minecraft.world.entity.player.Player shieldPlayer){shieldPlayer.getCooldowns().addCooldown(blocking.getItem(),60);shieldPlayer.stopUsingItem();server.broadcastEntityEvent(shieldPlayer,(byte)30);}
            }
            if (damaged) {
                sound(SoundEvents.TRIDENT_HIT, 0.65F, 1.0F);
                int fireAspect = net.minecraft.util.Mth.clamp(vanillaEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT),0,2);
                if (fireAspect > 0) living.setSecondsOnFire(4 * fireAspect);
                Vec3 motion = getDeltaMovement();
                double knockback = 0.15 + 0.2 * net.minecraft.util.Mth.clamp(vanillaEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK),0,2);
                if (motion.x*motion.x + motion.z*motion.z > 1.0e-8) living.knockback(knockback, -motion.x, -motion.z);
            }
        }
    }
    private void catchByDispenser(ServerLevel server) {
        if (!(server.getBlockEntity(dispenser) instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity inventory)) { dropStack(server); return; }
        int slot = originSlot >= 0 && originSlot < inventory.getContainerSize() && inventory.getItem(originSlot).isEmpty() ? originSlot : -1;
        if (slot < 0) for (int i=0;i<inventory.getContainerSize();i++) if (inventory.getItem(i).isEmpty()) { slot=i; break; }
        if (slot < 0) { dropStack(server); return; }
        ItemStack stack = getItem(); setItem(ItemStack.EMPTY);
        inventory.setItem(slot, stack); inventory.setChanged();
        sound(SoundEvents.TRIDENT_RETURN,0.5F,1); discard();
    }
    public void catchBy(ServerPlayer player) {
        if (isRemoved() || getItem().isEmpty()) return;
        ItemStack stack = getItem();
        setItem(ItemStack.EMPTY); // Clear projectile custody before any inventory operation.
        Inventory inventory = player.getInventory();
        if ((Inventory.isHotbarSlot(originSlot) || originSlot == Inventory.SLOT_OFFHAND)
                && inventory.getItem(originSlot).isEmpty()) inventory.setItem(originSlot, stack);
        else {
            // Inventory.add silently consumes overflow in creative mode; preserve full-inventory discs.
            if (inventory.getFreeSlot() != -1) inventory.add(stack);
            if (!stack.isEmpty()) {
                ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), stack);
                drop.setTarget(player.getUUID());
                drop.setNoPickUpDelay();
                player.level().addFreshEntity(drop);
            }
        }
        inventory.setChanged();
        player.containerMenu.broadcastChanges();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.5F, 1.0F);
        discard();
    }
    private void dropStack(ServerLevel server) {
        ItemStack stack = getItem();
        setItem(ItemStack.EMPTY);
        spawnAtLocation(stack);
        discard();
    }
    private void sound(SoundEvent sound, float volume, float pitch) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
    @Override public void remove(RemovalReason reason) {
        DiscFlights.forget(this);
        super.remove(reason);
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);tag.put("Disc",getItem().save(new CompoundTag()));
        if(thrower!=null)tag.putUUID("Thrower",thrower);if(dispenser!=null)tag.putLong("Dispenser",dispenser.asLong());
        tag.putInt("OriginSlot",originSlot);tag.putInt("FlightAge",age);tag.putInt("Bounces",bounces);tag.putBoolean("Returning",returning);tag.putDouble("Distance",distance);tag.putFloat("CurveDirection",curveDirection);
        var hits=new net.minecraft.nbt.ListTag();for(UUID id:hitThisPass)hits.add(net.minecraft.nbt.StringTag.valueOf(id.toString()));tag.put("HitThisPass",hits);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);setItem(ItemStack.of(tag.getCompound("Disc")));
        thrower=tag.hasUUID("Thrower")?tag.getUUID("Thrower"):null;dispenser=tag.contains("Dispenser")?BlockPos.of(tag.getLong("Dispenser")):null;
        originSlot=tag.contains("OriginSlot")?tag.getInt("OriginSlot"):-1;age=tag.getInt("FlightAge");bounces=tag.getInt("Bounces");returning=tag.getBoolean("Returning");distance=tag.getDouble("Distance");curveDirection=net.minecraft.util.Mth.clamp(tag.getFloat("CurveDirection"),-1,1);
        hitThisPass.clear();var hits=tag.getList("HitThisPass",8);for(int i=0;i<hits.size();i++)try{hitThisPass.add(UUID.fromString(hits.getString(i)));}catch(IllegalArgumentException ignored){}
    }
}
