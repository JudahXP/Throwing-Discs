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
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoveSimulationType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ThrowingDiscEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(
        ThrowingDiscEntity.class, EntityDataSerializers.ITEM_STACK);
    private UUID thrower;
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
        float target = hasVisualHeading ? (float)Math.clamp(-Math.toDegrees(turn)*3,-25,25) : 0;
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
        carried.hurtAndBreak(1, player, hand);
        setItem(carried);
        if (carried.isEmpty()) discard();
    }
    @Override public boolean displayFireAnimation() { return false; }
    public UUID throwerId() { return thrower; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STACK, ItemStack.EMPTY);
    }
    @Override public ItemStack getItem() { return entityData.get(STACK); }
    private void setItem(ItemStack stack) { entityData.set(STACK, stack); }
    @Override public MoveSimulationType getMoveSimulationType() { return MoveSimulationType.AUTHORITATIVE_SIDE; }
    @Override protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this, 2);
    }
    @Override public ItemStack getWeaponItem() { return getItem(); }
    public void launch(ServerPlayer player, ItemStack stack, int slot) {
        setOwner(player);
        thrower = player.getUUID();
        originSlot = slot;
        DiscEnchantments.normalize(stack, level());
        setItem(stack);
        setPos(player.getX(), player.getEyeY() - 0.15, player.getZ());
        setDeltaMovement(player.getLookAngle().scale(DiscPhysics.SPEED));
        Vec3 right = new Vec3(-player.getLookAngle().z, 0, player.getLookAngle().x).normalize();
        double side = player.getKnownMovement().dot(right);
        curveDirection = Math.abs(side) < 0.025 ? 0 : (float)Math.signum(side);


    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) updateVisualBank();
        if (!(level() instanceof ServerLevel server)) return;
        if (getItem().isEmpty()) { discard(); return; }
        clearFire();
        DiscEnchantments.normalize(getItem(), server);
        DiscFlights.track(this);
        age++;
        ServerPlayer player = thrower == null ? null : server.getServer().getPlayerList().getPlayer(thrower);
        if (player == null || !player.isAlive() || player.isSpectator()) { dropStack(server); return; }
        if (player.level() != server || age > DiscPhysics.MAX_FLIGHT_TICKS) { dropStack(server); return; }
        if (!returning && (age >= DiscPhysics.MAX_OUTBOUND_TICKS
            || getDeltaMovement().lengthSqr() < 1.0e-10
            || distance >= DiscPhysics.RANGE + DiscPhysics.rangeBonus(enchantmentLevel("range")) + bounces * DiscPhysics.BOUNCE_BONUS - 1.0e-6)) beginReturn();
        if (returning) flyHome(player); else flyOutbound(server);
        if (!isRemoved()) keepFlightLoaded(server);
    }
    public void keepFlightLoaded(ServerLevel server) {
        // Short-lived shared tickets expire after flight; never remove another disc's ticket.
        server.getChunkSource().addTicketWithRadius(ModEntities.FLIGHT_TICKET, chunkPosition(), 2);
    }
    private void beginReturn() {
        returning = true;
        hitThisPass.clear();
        sound(SoundEvents.TRIDENT_RETURN, 0.8F, 1.0F);
    }
    private void flyOutbound(ServerLevel server) {
        int curve = Math.clamp(enchantmentLevel("curve"),0,2);
        if (curve > 0 && curveDirection != 0) {
            setDeltaMovement(getDeltaMovement().yRot((float)Math.toRadians(-curveDirection * curve * 1.5)));
            needsSync = true;
        }
        steerTowardMob(server);
        double timeLeft = 1;
        // Resolve the unused fraction of a tick after each impact, including corners.
        for (int step = 0; step < 4 && timeLeft > 1.0e-5 && !returning; step++) {
            Vec3 start = position();
            double speed = getDeltaMovement().length();
            double available = returning ? Double.POSITIVE_INFINITY : Math.max(0,
                DiscPhysics.RANGE + DiscPhysics.rangeBonus(enchantmentLevel("range")) + bounces * DiscPhysics.BOUNCE_BONUS - distance);
            double travelTime = Math.min(timeLeft, available / Math.max(speed, 1.0e-9));
            Vec3 move = getDeltaMovement().scale(travelTime);
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
            if (!blocked) return;
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i());
            setPos(finish.add(normal.scale(0.015)));
            setDeltaMovement(DiscPhysics.reflect(getDeltaMovement(), normal));
            needsSync = true;
            sound(SoundEvents.TRIDENT_HIT_GROUND, 0.65F, 1.0F);
            if (++bounces >= DiscPhysics.MAX_BOUNCES) { beginReturn(); return; }
            timeLeft -= start.distanceTo(finish) / Math.max(speed, 1.0e-9);
        }
    }
    private int vanillaEnchantmentLevel(net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .get(key).map(holder -> net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(holder,getItem())).orElse(0);
    }
    public int enchantmentLevel(String name) {
        var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT, ThrowingDiscMod.id(name));
        return level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .get(key).map(holder -> net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(holder, getItem())).orElse(0);
    }
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
                e -> e.isAlive() && !e.isSpectator() && !hitThisPass.contains(e.getUUID()) && !e.isAlliedTo(getOwner()))) {
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
            needsSync = true;
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
        Vec3 target = player.position().add(0, player.getBbHeight() * 0.7, 0);
        setDeltaMovement(DiscPhysics.home(getDeltaMovement(), target.subtract(position())));
        double timeLeft = 1;
        for (int step = 0; step < 4 && timeLeft > 1.0e-5; step++) {
            Vec3 start = position();
            double speed = getDeltaMovement().length();
            double available = returning ? Double.POSITIVE_INFINITY : Math.max(0,
                DiscPhysics.RANGE + DiscPhysics.rangeBonus(enchantmentLevel("range")) + bounces * DiscPhysics.BOUNCE_BONUS - distance);
            double travelTime = Math.min(timeLeft, available / Math.max(speed, 1.0e-9));
            Vec3 move = getDeltaMovement().scale(travelTime);
            Vec3 end = start.add(move);
            if (!server.hasChunk(BlockPos.containing(end).getX() >> 4, BlockPos.containing(end).getZ() >> 4)) {
                dropStack(server);
                return;
            }
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
            // Clip movement before checking catch distance; never catch across a thin wall.
            if (DiscPhysics.segmentWithin(start, finish, target, 0.8) && clearPath(start, target)) {
                catchBy(player);
                return;
            }
            pierce(server, start, finish);
            setPos(finish);
            needsSync = true;
            if (!blocked) return;
            if (hit.isInside()) { dropStack(server); return; }
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i());
            setPos(finish.add(normal.scale(0.015)));
            setDeltaMovement(DiscPhysics.reflect(getDeltaMovement(), normal));
            sound(SoundEvents.TRIDENT_HIT_GROUND, 0.65F, 1.0F);
            timeLeft -= start.distanceTo(finish) / Math.max(speed, 1.0e-9);
        }
    }
    private record Target(Entity entity, double distance) {}
    private void pierce(ServerLevel server, Vec3 start, Vec3 end) {
        var targets = new ArrayList<Target>();
        for (Entity entity : server.getEntities(this, new AABB(start, end).inflate(0.3),
                e -> (e instanceof LivingEntity || e instanceof net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball) && e.isAlive() && !e.isSpectator()
                    && !e.getUUID().equals(thrower) && !hitThisPass.contains(e.getUUID()))) {
            AABB box = entity.getBoundingBox().inflate(0.2);
            if (box.contains(start)) targets.add(new Target(entity, 0));
            else box.clip(start, end).ifPresent(point -> targets.add(new Target(entity, start.distanceToSqr(point))));
        }
        targets.sort(Comparator.comparingDouble(Target::distance));
        float damage = getItem().getItem() instanceof ThrowingDiscItem disc ? disc.tier.damage : 4;
        damage = DiscPhysics.speedDamage(damage + Math.clamp(enchantmentLevel("keen_edge"),0,3), getDeltaMovement().length());
        Entity owner = getOwner();
        for (Target target : targets) {
            if (owner != null && !owner.doTeamsAllowDamage(target.entity())) continue;
            hitThisPass.add(target.entity().getUUID());
            if (target.entity() instanceof net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball fireball) {
                if (owner != null) fireball.deflect(net.minecraft.world.entity.projectile.ProjectileDeflection.REVERSE,
                    this, net.minecraft.world.entity.EntityReference.of(owner), true, getDeltaMovement());
                continue;
            }
            LivingEntity living = (LivingEntity)target.entity();
            if (living instanceof net.minecraft.world.entity.player.Player victim
                && owner instanceof net.minecraft.world.entity.player.Player attacker && !victim.canHarmPlayer(attacker)) continue;
            ItemStack blocking = living.getItemBlockingWith();
            boolean facing = blocking != null && living.getLookAngle().dot(position().subtract(living.position()).normalize()) > 0;
            boolean damaged = living.hurtServer(server, damageSources().thrown(this, owner), damage);
            if (facing) {
                var blocks = blocking.get(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS);
                if (blocks != null) blocks.disable(server, living, 3.0F, blocking);
            }
            if (damaged) {
                sound(SoundEvents.SPEAR_HIT.value(), 0.65F, 1.0F);
                int fireAspect = Math.clamp(vanillaEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT),0,2);
                if (fireAspect > 0) living.igniteForSeconds(4 * fireAspect);
                Vec3 motion = getDeltaMovement();
                double knockback = 0.15 + 0.2 * Math.clamp(vanillaEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK),0,2);
                if (motion.x*motion.x + motion.z*motion.z > 1.0e-8) living.knockback(knockback, -motion.x, -motion.z, damageSources().thrown(this, owner), damage);
            }
        }
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
        spawnAtLocation(server, stack);
        discard();
    }
    private void sound(SoundEvent sound, float volume, float pitch) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
    @Override public void onRemoval(RemovalReason reason) {
        DiscFlights.forget(this);
        super.onRemoval(reason);
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Disc", ItemStack.CODEC, getItem());
        output.storeNullable("Thrower", UUIDUtil.CODEC, thrower);
        output.putInt("OriginSlot", originSlot);
        output.putInt("FlightAge", age);
        output.putInt("Bounces", bounces);
        output.putBoolean("Returning", returning);
        output.putDouble("Distance", distance);
        output.putFloat("CurveDirection", curveDirection);
        output.store("HitThisPass", UUIDUtil.CODEC.listOf(), new ArrayList<>(hitThisPass));
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setItem(input.read("Disc", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        thrower = input.read("Thrower", UUIDUtil.CODEC).orElse(null);
        originSlot = input.getIntOr("OriginSlot", -1);
        age = input.getIntOr("FlightAge", 0);
        bounces = input.getIntOr("Bounces", 0);
        returning = input.getBooleanOr("Returning", false);
        distance = input.getDoubleOr("Distance", 0);
        curveDirection = Math.clamp(input.getFloatOr("CurveDirection",0),-1,1);
        hitThisPass.clear();
        hitThisPass.addAll(input.read("HitThisPass", UUIDUtil.CODEC.listOf()).orElse(java.util.List.of()));
    }
}
