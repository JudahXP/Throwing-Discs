package dev.disc;

import net.minecraft.gametest.framework.GameTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DiscGameTests implements FabricGameTest {
    private static void check(GameTestHelper h,boolean value,String message){h.assertTrue(value,message);}
    private static void fail(GameTestHelper h,String message){h.fail(message);}
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        h.getLevel().getServer().setPvpAllowed(true);
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "disc-test"), false);
        var p = new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(), h.getLevel(),
            cookie.gameProfile(), cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection, p, cookie);
        p.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,2,1))));
        p.setNoGravity(true);
        return p;
    }
    private static ThrowingDiscEntity projectile(GameTestHelper h, net.minecraft.server.level.ServerPlayer p) {
        var d = new ThrowingDiscEntity(ModEntities.DISC, h.getLevel());
        d.launch(p, new ItemStack(ModItems.get(DiscTier.IRON)), 2);
        h.getLevel().addFreshEntity(d);
        DiscFlights.track(d);
        return d;
    }
    private static void forceReturn(ThrowingDiscEntity disc, GameTestHelper h, int age) {
        var output = new net.minecraft.nbt.CompoundTag();
        disc.addAdditionalSaveData(output);
        output.putBoolean("Returning",true);
        output.putInt("FlightAge",age);
        disc.readAdditionalSaveData(output);
    }
    @GameTest(template=EMPTY_STRUCTURE) public void returnBouncesAndTimeoutDropsBehindWall(GameTestHelper h) {
        var p=player(h);p.setPos(p.position().add(0,0,-12));
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.STONE);
        var disc=projectile(h,p);
        var start=net.minecraft.world.phys.Vec3.atBottomCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,3,4))).add(0,0.25,0);
        disc.setPos(start);
        disc.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-1.25));
        forceReturn(disc,h,20);
        disc.tick();
        check(h,disc.getDeltaMovement().z>0,"Returning disc reflects from wall");
        double wallBack=h.absolutePos(new net.minecraft.core.BlockPos(1,3,4)).getZ();
        forceReturn(disc,h,DiscPhysics.MAX_FLIGHT_TICKS);
        disc.tick();
        check(h,disc.isRemoved(),"Trapped disc drops at timeout");
        check(h,p.getInventory().countItem(ModItems.get(DiscTier.IRON))==0,"Timeout cannot return through wall");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            new net.minecraft.world.phys.AABB(start,start).inflate(20),e->e.getItem().is(ModItems.get(DiscTier.IRON)));
        check(h,drops.size()==1 && drops.getFirst().getZ()>=wallBack,"Exactly one recoverable disc drops on the blocked side");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void returnsThroughNearbyThinWall(GameTestHelper h) {
        var p=player(h);
        var cell=net.minecraft.world.phys.Vec3.atLowerCornerOf(h.absolutePos(new net.minecraft.core.BlockPos(1,2,3)));
        p.setPos(cell.add(0.5,0,0.1));
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.GLASS_PANE);
        var disc=projectile(h,p);
        disc.setPos(cell.add(0.5,p.getBbHeight()*0.7,0.8));
        disc.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-0.2));
        forceReturn(disc,h,20);
        disc.tick();
        check(h,disc.isRemoved() && p.getInventory().countItem(ModItems.get(DiscTier.IRON))==1,"Returning disc phases through a nearby pane and is caught");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void toolDurabilityAndThrowWear(GameTestHelper h) {
        var p=player(h);
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        int[] expected={125,16,780,1015,95};
        for(DiscTier tier:DiscTier.values()) {
            var item=ModItems.get(tier);
            var stack=new ItemStack(item);
            check(h,stack.getMaxDamage()==expected[tier.ordinal()],"Matches half vanilla material durability: "+tier);
            p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            item.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
            DiscFlights.recover(p);
            check(h,p.getMainHandItem().getDamageValue()==1,"One successful throw costs one durability");
            p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(p.getMainHandItem()));
        }
        var lastUse=new ItemStack(ModItems.get(DiscTier.GOLD));
        lastUse.setDamageValue(lastUse.getMaxDamage()-1);
        p.setItemInHand(InteractionHand.MAIN_HAND,lastUse);
        ModItems.get(DiscTier.GOLD).use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        DiscFlights.recover(p);
        check(h,p.getMainHandItem().isEmpty(),"Last durability breaks the disc without returning a duplicate");
        p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.get(DiscTier.IRON)));
        ModItems.get(DiscTier.IRON).use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        DiscFlights.recover(p);
        check(h,p.getMainHandItem().getDamageValue()==0,"Creative throws do not wear the disc");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void unbreakingUsesVanillaDurabilityRules(GameTestHelper h) {
        var p=player(h);
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
        for(DiscTier tier:DiscTier.values())
            check(h,enchantment.value().canEnchant(new ItemStack(ModItems.get(tier))),"Unbreaking accepts "+tier);
        var stack=new ItemStack(ModItems.get(DiscTier.DIAMOND));
        stack.enchant(enchantment,3);
        for(int i=0;i<1000;i++) stack.hurtAndBreak(1,h.getLevel(),p,broken -> fail(h,"Unexpected break"));
        check(h,stack.getDamageValue()>100 && stack.getDamageValue()<400,"Unbreaking III prevents approximately three quarters of wear");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void mendingRepairsHeldDiscsWithExperience(GameTestHelper h) {
        var p=player(h);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING);
        for(DiscTier tier:DiscTier.values())
            check(h,enchantment.value().canEnchant(new ItemStack(ModItems.get(tier))),"Mending accepts "+tier);
        var stack=new ItemStack(ModItems.get(DiscTier.IRON));
        stack.setDamageValue(20);stack.enchant(enchantment,1);
        p.setItemInHand(InteractionHand.OFF_HAND,stack);
        var orb=new net.minecraft.world.entity.ExperienceOrb(h.getLevel(),p.getX(),p.getY(),p.getZ(),5);
        h.getLevel().addFreshEntity(orb);orb.playerTouch(p);
        check(h,stack.getDamageValue()==10,"Five XP repairs ten durability in the off-hand");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void fullCreativeInventoryDropsExactlyOne(GameTestHelper h) {
        var p = player(h);
        p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        for (int i=0;i<36;i++) p.getInventory().setItem(i, new ItemStack(Items.STONE,64));
        var d = projectile(h,p);
        d.catchBy(p);
        var drops = h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            p.getBoundingBox().inflate(2), e -> e.getItem().is(ModItems.get(DiscTier.IRON)));
        check(h,drops.size()==1 && drops.getFirst().getItem().getCount()==1, "Full creative inventory drops the disc instead of deleting it");
        check(h,p.getInventory().getItem(2).is(Items.STONE), "Occupied slot remains unchanged");
        d.catchBy(p);
        check(h,drops.getFirst().getItem().getCount()==1, "Second catch cannot duplicate drop");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void piercesMultipleMobsOncePerPass(GameTestHelper h) {
        var p = player(h);
        var first = h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.HUSK, 1.5F,2,2.5F);
        var second = h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.HUSK, 1.5F,2,3.5F);
        var d = projectile(h,p);
        var start = d.position();
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,4));
        d.tick();
        check(h,first.getHealth()<20 && second.getHealth()<20, "One swept segment hits both targets");
        float firstHealth=first.getHealth(), secondHealth=second.getHealth();
        first.invulnerableTime=0; second.invulnerableTime=0;
        d.setPos(start);d.tick();
        check(h,first.getHealth()==firstHealth && second.getHealth()==secondHealth, "Same pass never hits a target twice");
        d.catchBy(p);
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void reflectsFromSolidBlock(GameTestHelper h) {
        var p = player(h);
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.STONE);
        var d = projectile(h,p);
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,2));
        d.tick();
        check(h,d.getDeltaMovement().z<0, "Wall reflects the projectile");
        check(h,Math.abs(d.getDeltaMovement().z+2.35)<0.001, "Ricochet accelerates by 17.5 percent");
        d.catchBy(p);
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void completesFlightAndReturnsToOriginalSlot(GameTestHelper h) {
        var p = player(h);p.setPos(p.position().add(0,30,0));
        for(int dz=-32;dz<=48;dz+=16) h.getLevel().getChunk(net.minecraft.core.BlockPos.containing(p.position().add(0,0,dz)));
        var d = projectile(h,p);
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));
        for (int tick=0;tick<120 && !d.isRemoved();tick++) d.tick();
        check(h,d.isRemoved(), "Normal flight finishes before emergency recovery timeout");
        check(h,p.getInventory().getItem(2).is(ModItems.get(DiscTier.IRON)), "Normal homing catches into original slot");
        h.succeed();
    }

    @GameTest(template=EMPTY_STRUCTURE) public void savesFlightWithDataComponents(GameTestHelper h) {
        var p = player(h);
        var d = projectile(h,p);
        d.getItem().set(DataComponents.CUSTOM_NAME, Component.literal("Saved disc"));
        var output = new net.minecraft.nbt.CompoundTag();
        d.addAdditionalSaveData(output);
        var restored = new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());
        restored.readAdditionalSaveData(output);
        check(h,p.getUUID().equals(restored.throwerId()), "Thrower UUID survives reload");
        check(h,ItemStack.isSameItemSameComponents(d.getItem(),restored.getItem()), "Complete component map survives reload");
        d.discard();
        restored.catchBy(p);
        check(h,p.getInventory().getItem(2).is(ModItems.get(DiscTier.IRON)), "Restored flight remembers source slot");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void allCraftingAndSmithingRecipesWork(GameTestHelper h) {
        var materials = new net.minecraft.world.item.Item[]{Items.IRON_INGOT,Items.GOLD_INGOT,Items.DIAMOND};
        for (int i=0;i<materials.length;i++) {
            var items = new java.util.ArrayList<ItemStack>(java.util.Collections.nCopies(9,ItemStack.EMPTY));
            for (int slot : new int[]{1,3,5,7}) items.set(slot,new ItemStack(materials[i]));
            var input = net.minecraft.world.item.crafting.CraftingInput.of(3,3,items);
            var recipe = h.getLevel().recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
            check(h,recipe.value().assemble(input,h.getLevel().registryAccess()).is(ModItems.get(DiscTier.values()[i])), "Correct crafting output for " + DiscTier.values()[i]);
        }
        var diamond = new ItemStack(ModItems.get(DiscTier.DIAMOND));
        diamond.set(DataComponents.CUSTOM_NAME, Component.literal("Upgrade me"));
        var input = new net.minecraft.world.item.crafting.SmithingRecipeInput(
            new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),diamond,new ItemStack(Items.NETHERITE_INGOT));
        var recipe = h.getLevel().recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMITHING,input,h.getLevel()).orElseThrow();
        var result = recipe.value().assemble(input,h.getLevel().registryAccess());
        check(h,result.is(ModItems.get(DiscTier.NETHERITE)), "Smithing upgrades diamond to netherite");
        check(h,Component.literal("Upgrade me").equals(result.get(DataComponents.CUSTOM_NAME)), "Smithing preserves custom components");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void originalSlotAndComponents(GameTestHelper h) {
        var p = player(h);
        p.getInventory().selected=2;
        ItemStack stack = new ItemStack(ModItems.get(DiscTier.DIAMOND));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("My returning disc"));
        p.setItemInHand(InteractionHand.MAIN_HAND, stack);
        ModItems.get(DiscTier.DIAMOND).use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        check(h,p.getMainHandItem().isEmpty(), "Throw transfers real inventory stack");
        p.getInventory().selected=5;
        DiscFlights.recover(p);
        check(h,p.getInventory().getItem(2).is(ModItems.get(DiscTier.DIAMOND)), "Returns to original hotbar slot");
        check(h,p.getInventory().getItem(5).isEmpty(), "Does not use newly selected slot");
        check(h,Component.literal("My returning disc").equals(p.getInventory().getItem(2).get(DataComponents.CUSTOM_NAME)), "Preserves data components");
        DiscFlights.recover(p);
        check(h,p.getInventory().countItem(ModItems.get(DiscTier.DIAMOND)) == 1, "Repeat recovery cannot duplicate");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void offhandAndOccupiedSlot(GameTestHelper h) {
        var p = player(h);
        var disc = ModItems.get(DiscTier.IRON);
        p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(disc));
        disc.use(h.getLevel(), p, InteractionHand.OFF_HAND);
        DiscFlights.recover(p);
        check(h,p.getOffhandItem().is(disc), "Offhand catches into offhand");
        p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(p.getOffhandItem()));
        disc.use(h.getLevel(), p, InteractionHand.OFF_HAND);
        p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
        DiscFlights.recover(p);
        check(h,p.getOffhandItem().is(Items.STICK), "Occupied offhand is not overwritten");
        check(h,p.getInventory().countItem(disc) == 1, "Disc falls back to main inventory");
        h.succeed();
    }

    @GameTest(template=EMPTY_STRUCTURE) public void customEnchantmentsAndBooks(GameTestHelper h) {
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var p=player(h);
        var d=projectile(h,p);
        String[] names={"homing","range","curve","keen_edge","harvest"};
        int[] levels={4,5,2,3,1};
        for(int i=0;i<names.length;i++) {
            var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id(names[i]));
            var enchantment=registry.getOrThrow(key);
            check(h,enchantment.value().getMaxLevel()==levels[i],"Correct maximum enchantment level");
            for(var tier:DiscTier.values()) check(h,enchantment.value().canEnchant(new ItemStack(ModItems.get(tier))),"Enchantment accepts every disc tier");
            check(h,!enchantment.value().canEnchant(new ItemStack(Items.DIAMOND_SWORD)),"Disc enchantments reject swords");
            var book=net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new net.minecraft.world.item.enchantment.EnchantmentInstance(enchantment,levels[i]));
            check(h,book.is(Items.ENCHANTED_BOOK),"Uses vanilla enchanted books");
            var anvil=new net.minecraft.world.inventory.AnvilMenu(0,p.getInventory());
            anvil.getSlot(0).set(new ItemStack(ModItems.get(DiscTier.IRON)));
            anvil.getSlot(1).set(book);
            anvil.createResult();
            check(h,net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantment,anvil.getSlot(2).getItem())==levels[i],"Enchanted book applies through actual anvil menu");
            d.getItem().enchant(enchantment,levels[i]);
            check(h,d.enchantmentLevel(names[i])==levels[i],"Flight reads enchantment components");
        }
        d.catchBy(p);h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void homingTargetsOnlyVisibleForwardMobs(GameTestHelper h) {
        var p=player(h);var d=projectile(h,p);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("homing")));
        d.getItem().enchant(enchantment,4);
        h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.HUSK,3.5F,2,5.5F);
        var start=d.position();
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));d.tick();
        check(h,d.getDeltaMovement().x>0 && d.getDeltaMovement().x<0.15,"Gentle curve toward forward mob");
        d.setPos(start);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-1.25));d.tick();
        check(h,Math.abs(d.getDeltaMovement().x)<1e-8,"No lock onto mob behind disc");
        h.setBlock(2,3,3,net.minecraft.world.level.block.Blocks.STONE);
        d.setPos(start);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));d.tick();
        check(h,Math.abs(d.getDeltaMovement().x)<1e-8,"Homing cannot lock through a solid wall");
        d.catchBy(p);h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void shattersPotsAndPreservesContents(GameTestHelper h) {
        var p=player(h);
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(1,3,3));
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.DECORATED_POT);
        var pot=(net.minecraft.world.level.block.entity.DecoratedPotBlockEntity)h.getLevel().getBlockEntity(pos);
        pot.applyComponentsFromItemStack(new ItemStack(Items.DECORATED_POT));
        pot.setTheItem(new ItemStack(Items.DIAMOND,3));
        var d=projectile(h,p);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,3));d.tick();
        check(h,!h.getLevel().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DECORATED_POT),"Thrown disc shatters decorated pot");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2));
        check(h,drops.stream().filter(e->e.getItem().is(Items.DIAMOND)).mapToInt(e->e.getItem().getCount()).sum()==3,"Stored contents drop exactly once");
        check(h,drops.stream().filter(e->e.getItem().is(Items.BRICK)).mapToInt(e->e.getItem().getCount()).sum()==4,"Plain pot drops four bricks");
        check(h,d.getDeltaMovement().z>0,"Disc continues past broken pot");
        d.catchBy(p);h.succeed();
    }

    @GameTest(template=EMPTY_STRUCTURE) public void rangeEnchantmentDelaysReturn(GameTestHelper h) {
        var p=player(h);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("range")));
        for(int level=0;level<=5;level++) {
            var d=projectile(h,p);
            if(level>0)d.getItem().enchant(enchantment,level);
            var saved=new net.minecraft.nbt.CompoundTag();
            d.addAdditionalSaveData(saved);saved.putDouble("Distance",16);
            d.readAdditionalSaveData(saved);
            d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));d.tick();
            var after=new net.minecraft.nbt.CompoundTag();d.addAdditionalSaveData(after);
            var input=after;
            check(h,input.getBoolean("Returning")==(level==0),"Range enchantment extends outbound flight beyond sixteen blocks");
            d.discard();
        }
        h.succeed();
    }

    @GameTest(template=EMPTY_STRUCTURE) public void rejectsIncompatibleBooksEvenInCreative(GameTestHelper h) {
        var p=player(h);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        for(var key:java.util.List.of(net.minecraft.world.item.enchantment.Enchantments.PROTECTION,net.minecraft.world.item.enchantment.Enchantments.POWER,net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY,net.minecraft.world.item.enchantment.Enchantments.FLAME)) {
            var anvil=new net.minecraft.world.inventory.AnvilMenu(0,p.getInventory());
            anvil.getSlot(0).set(new ItemStack(ModItems.get(DiscTier.IRON)));
            anvil.getSlot(1).set(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new net.minecraft.world.item.enchantment.EnchantmentInstance(registry.getOrThrow(key),1)));
            anvil.createResult();check(h,anvil.getSlot(2).getItem().isEmpty(),"Incompatible books cannot be applied even in creative");
        }
        for(var key:java.util.List.of(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT,net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK)) {
            var anvil=new net.minecraft.world.inventory.AnvilMenu(0,p.getInventory());
            anvil.getSlot(0).set(new ItemStack(ModItems.get(DiscTier.IRON)));
            anvil.getSlot(1).set(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new net.minecraft.world.item.enchantment.EnchantmentInstance(registry.getOrThrow(key),1)));
            anvil.createResult();check(h,!anvil.getSlot(2).getItem().isEmpty(),"Fire Aspect and Knockback books work");
        }
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void fireAspectAndKnockbackAffectTargets(GameTestHelper h) {
        var p=player(h);
        var stack=new ItemStack(ModItems.get(DiscTier.IRON));
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        stack.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT),1);
        stack.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK),2);
        var d=new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());d.launch(p,stack,2);h.getLevel().addFreshEntity(d);
        check(h,!d.isOnFire() && !d.displayFireAnimation(),"Fire Aspect has no flying fire effect");
        var mob=h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.HUSK,1.5F,2,2.5F);
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,2));d.tick();
        check(h,mob.isOnFire(),"Fire Aspect ignites a successfully hit mob");
        check(h,mob.getDeltaMovement().z>0,"Knockback pushes target along the flight direction");
        d.catchBy(p);h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void deflectsGhastFireballs(GameTestHelper h) {
        var p=player(h);var d=projectile(h,p);
        var fireball=new net.minecraft.world.entity.projectile.LargeFireball(net.minecraft.world.entity.EntityType.FIREBALL,h.getLevel());
        fireball.setPos(d.position().add(0,0,2));fireball.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-0.8));
        h.getLevel().addFreshEntity(fireball);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,3));d.tick();
        check(h,fireball.getDeltaMovement().z>0,"Disc reverses ghast fireball");
        check(h,fireball.getOwner()==p,"Reflected fireball credits the thrower");
        fireball.discard();d.catchBy(p);h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void meleeMatchesReference(GameTestHelper h) {
        var shovels=new net.minecraft.world.item.Item[]{Items.IRON_SHOVEL,Items.GOLDEN_SHOVEL,Items.DIAMOND_SHOVEL,Items.NETHERITE_SHOVEL};
        for(var tier:DiscTier.values()) {
            var disc=new ItemStack(ModItems.get(tier)).get(DataComponents.ATTRIBUTE_MODIFIERS);
            var shovel=new ItemStack(shovels[tier == DiscTier.COPPER ? 0 : tier.ordinal()]).get(DataComponents.ATTRIBUTE_MODIFIERS);
            for(var entry:shovel.modifiers()) {
                double actual=disc.modifiers().stream().filter(e->e.attribute().equals(entry.attribute())).findFirst().orElseThrow().modifier().amount();
                double expected=entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)?tier.melee-1:-3;
                check(h,Math.abs(actual-expected)<1e-8,"Melee damage matches reference; speed unchanged");
            }
        }
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void disablesBlockingShield(GameTestHelper h) {
        var p=player(h);var victim=player(h);
        victim.setPos(p.position().add(0,0,2));victim.setYRot(180);
        var shield=new ItemStack(Items.SHIELD);
        victim.setItemInHand(InteractionHand.OFF_HAND,shield);victim.startUsingItem(InteractionHand.OFF_HAND);for(int tick=0;tick<6;tick++)victim.doTick();
        check(h,victim.isBlocking(),"Test victim must be actively blocking: "+victim.getTicksUsingItem());
        var d=projectile(h,p);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,3));d.tick();
        check(h,victim.getCooldowns().isOnCooldown(victim.getOffhandItem()),"Front-facing shield receives a cooldown");
        check(h,!victim.isUsingItem(),"Shield is lowered after impact");
        d.catchBy(p);h.succeed();
    }

    @GameTest(template=EMPTY_STRUCTURE) public void curveFollowsLaunchStrafe(GameTestHelper h) {
        var p=player(h);p.setYRot(0);p.setXRot(0);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("curve")));
        double first=0;
        for(int level=1;level<=2;level++) {
            var stack=new ItemStack(ModItems.get(DiscTier.IRON));stack.enchant(enchantment,level);
            p.setKnownMovement(new net.minecraft.world.phys.Vec3(-0.2,0,0));
            var d=new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());d.launch(p,stack,2);h.getLevel().addFreshEntity(d);d.tick();
            check(h,d.getDeltaMovement().x<0,"Right strafe curves right");
            if(level==1)first=Math.abs(d.getDeltaMovement().x);else check(h,Math.abs(d.getDeltaMovement().x)>first,"Curve II bends more than Curve I");
            d.discard();
            p.setKnownMovement(new net.minecraft.world.phys.Vec3(0.2,0,0));
            var left=new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());left.launch(p,stack.copy(),2);h.getLevel().addFreshEntity(left);left.tick();
            check(h,left.getDeltaMovement().x>0,"Left strafe curves left");left.discard();
        }
        h.succeed();
    }

    @GameTest(template=EMPTY_STRUCTURE) public void returnsAtRoundedRangeBoundary(GameTestHelper h) {
        var p=player(h);p.setPos(p.position().add(0,30,0));
        var d=projectile(h,p);
        for(int dz=-16;dz<=32;dz+=16) h.getLevel().getChunk(net.minecraft.core.BlockPos.containing(p.position().add(0,0,dz)));
        d.setPos(d.position().add(0,0,12));d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));
        var saved=new net.minecraft.nbt.CompoundTag();
        d.addAdditionalSaveData(saved);saved.putDouble("Distance",Math.nextDown(16.0));
        d.readAdditionalSaveData(saved);
        d.tick();
        var after=new net.minecraft.nbt.CompoundTag();d.addAdditionalSaveData(after);
        var input=after;
        check(h,input.getBoolean("Returning"),"Rounding near max range starts return immediately instead of hovering");
        for(int i=0;i<100 && !d.isRemoved();i++)d.tick();
        check(h,d.isRemoved() && p.getInventory().getItem(2).is(ModItems.get(DiscTier.IRON)),"Rounded-boundary disc comes back normally");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void steeringEnchantmentsAreExclusive(GameTestHelper h) {
        var p=player(h);var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var homing=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("homing")));
        var curve=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("curve")));
        check(h,!net.minecraft.world.item.enchantment.Enchantment.areCompatible(homing,curve),"Enchanting recognizes exclusive steering options");
        for(var mode:java.util.List.of(net.minecraft.world.level.GameType.SURVIVAL,net.minecraft.world.level.GameType.CREATIVE)) {
            p.setGameMode(mode);
            for(boolean reverse:new boolean[]{false,true}) {
                var stack=new ItemStack(ModItems.get(DiscTier.IRON));stack.enchant(reverse?curve:homing,1);
                var anvil=new net.minecraft.world.inventory.AnvilMenu(0,p.getInventory());anvil.getSlot(0).set(stack);
                anvil.getSlot(1).set(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new net.minecraft.world.item.enchantment.EnchantmentInstance(reverse?homing:curve,1)));
                anvil.createResult();check(h,anvil.getSlot(2).getItem().isEmpty(),"Cannot combine Curve and Homing in either order or game mode");
            }
        }
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void migratesOldEnchantedDiscs(GameTestHelper h) {
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var stack=new ItemStack(ModItems.get(DiscTier.IRON));
        for(String name:new String[]{"homing","curve","keen_edge"}) {
            var holder=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id(name)));
            stack.enchant(holder,name.equals("keen_edge")?5:1);
        }
        stack.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FLAME),1);
        DiscEnchantments.normalize(stack,h.getLevel());
        var p=player(h);var d=new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());d.launch(p,stack,2);
        check(h,d.enchantmentLevel("keen_edge")==3,"Legacy Keen Edge clamps to III");
        check(h,d.enchantmentLevel("homing")==1 && d.enchantmentLevel("curve")==0,"Legacy dual steering keeps Homing");
        check(h,net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT),d.getItem())==1,"Old Flame becomes Fire Aspect I");
        check(h,!d.isOnFire() && !d.displayFireAnimation(),"Migrated disc has no fire overlay");
        d.discard();h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void stoppedOutboundDiscReturns(GameTestHelper h) {
        var p=player(h);p.setPos(p.position().add(0,30,0));
        var d=projectile(h,p);
        d.setPos(d.position().add(0,0,5));
        d.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        for(int dz=-16;dz<=32;dz+=16) h.getLevel().getChunk(net.minecraft.core.BlockPos.containing(p.position().add(0,0,dz)));
        for(int i=0;i<100 && !d.isRemoved();i++) d.tick();
        check(h,d.isRemoved() && p.getInventory().getItem(2).is(ModItems.get(DiscTier.IRON)),"A stopped disc returns instead of hovering until timeout");
        h.succeed();
    }

    @GameTest(template=EMPTY_STRUCTURE) public void ricochetIsNotRegistered(GameTestHelper h) {
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        check(h,registry.get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("ricochet"))).isEmpty(),"Ricochet has no registry entry, book or command option");
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void harvestDropsRipeCropsAndPreservesYoungCrops(GameTestHelper h) {
        var p=player(h);var d=projectile(h,p);
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        d.getItem().enchant(registry.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("harvest"))),1);
        h.setBlock(1,1,2,net.minecraft.world.level.block.Blocks.FARMLAND);
        h.setBlock(1,1,3,net.minecraft.world.level.block.Blocks.FARMLAND);
        h.setBlock(1,2,2,net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE,7));
        h.setBlock(1,2,3,net.minecraft.world.level.block.Blocks.WHEAT);
        var start=net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,2,1)));
        DiscHarvest.harvest(h.getLevel(),d,start,start.add(0,0,3));
        check(h,h.getBlockState(new net.minecraft.core.BlockPos(1,2,2)).isAir(),"Mature wheat breaks");
        check(h,h.getBlockState(new net.minecraft.core.BlockPos(1,2,3)).is(net.minecraft.world.level.block.Blocks.WHEAT),"Young wheat remains");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(start,start.add(0,0,3)).inflate(2),e->e.getItem().is(Items.WHEAT));
        check(h,!drops.isEmpty(),"Harvest drops wheat");d.discard();h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void dispenserRecoversDiscForReuse(GameTestHelper h) {
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(1,5,1));
        h.getLevel().setBlock(pos,net.minecraft.world.level.block.Blocks.DISPENSER.defaultBlockState(),3);
        var inventory=(net.minecraft.world.level.block.entity.DispenserBlockEntity)h.getLevel().getBlockEntity(pos);
        for(int attempt=0;attempt<2;attempt++) {
            var stack=attempt==0?new ItemStack(ModItems.get(DiscTier.IRON)):inventory.getItem(2);
            check(h,!stack.isEmpty(),"Previous return supplies the next throw");
            inventory.setItem(2,stack);
            var state=net.minecraft.world.level.block.Blocks.DISPENSER.defaultBlockState().setValue(net.minecraft.world.level.block.DispenserBlock.FACING,net.minecraft.core.Direction.SOUTH);
            h.getLevel().setBlock(pos,state,3);
            var behavior=net.minecraft.world.level.block.DispenserBlock.DISPENSER_REGISTRY.get(ModItems.get(DiscTier.IRON));
            var remainder=behavior.dispense(new net.minecraft.core.dispenser.BlockSource(h.getLevel(),pos,state,inventory),stack);
            inventory.setItem(2,remainder);
            var flights=h.getLevel().getEntitiesOfClass(ThrowingDiscEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2),e->!e.isRemoved());
            check(h,flights.size()==1 && remainder.isEmpty(),"Registered dispenser behavior transfers exactly one disc");
            var d=flights.getFirst();
            for(int dz=-32;dz<=48;dz+=16) h.getLevel().getChunk(pos.offset(0,0,dz));
            for(int tick=0;tick<200 && !d.isRemoved();tick++) d.tick();
            check(h,d.isRemoved() && inventory.getItem(2).is(ModItems.get(DiscTier.IRON)),"Disc returns to dispenser without duplication");
            check(h,inventory.getItem(2).getDamageValue()==attempt+1,"Every dispenser throw consumes one durability");
        }
        h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void outboundCannotPhaseNearOwner(GameTestHelper h) {
        var p=player(h);var d=projectile(h,p);
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.STONE);
        var start=net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,3,2)));
        d.setPos(start);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));d.tick();
        check(h,d.getDeltaMovement().z<0,"Outbound disc bounces even within eight blocks of owner");
        d.discard();h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void returnStillCollidesOutsideEightBlocks(GameTestHelper h) {
        var p=player(h);p.setPos(p.position().add(0,0,-8));
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.STONE);
        var d=projectile(h,p);
        d.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,3,4))));
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-1.25));forceReturn(d,h,20);d.tick();
        check(h,d.getDeltaMovement().z>0,"Returning disc bounces outside the eight-block sphere");
        d.discard();h.succeed();
    }
    @GameTest(template=EMPTY_STRUCTURE) public void bouncesBeyondFormerLimit(GameTestHelper h) {
        var p=player(h);var d=projectile(h,p);
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.STONE);
        d.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,3,2))));
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));
        var saved=new net.minecraft.nbt.CompoundTag();
        d.addAdditionalSaveData(saved);saved.putInt("Bounces",8);
        d.readAdditionalSaveData(saved);
        d.tick();
        var after=new net.minecraft.nbt.CompoundTag();d.addAdditionalSaveData(after);
        var input=after;
        check(h,input.getInt("Bounces")>8 && !input.getBoolean("Returning"),"Ninth bounce does not force return");
        d.discard();h.succeed();
    }
}
