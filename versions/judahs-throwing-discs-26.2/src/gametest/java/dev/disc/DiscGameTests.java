package dev.disc;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DiscGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
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
        var problems = new net.minecraft.util.ProblemReporter.Collector();
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(problems,h.getLevel().registryAccess());
        disc.addAdditionalSaveData(output);
        output.putBoolean("Returning",true);
        output.putInt("FlightAge",age);
        disc.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(
            problems,h.getLevel().registryAccess(),output.buildResult()));
        h.assertTrue(problems.isEmpty(),"Valid returning flight test setup");
    }
    @GameTest public void returnBouncesAndTimeoutDropsBehindWall(GameTestHelper h) {
        var p=player(h);
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.STONE);
        var disc=projectile(h,p);
        var start=net.minecraft.world.phys.Vec3.atBottomCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,3,4))).add(0,0.25,0);
        disc.setPos(start);
        disc.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-1.25));
        forceReturn(disc,h,20);
        disc.tick();
        h.assertTrue(disc.getDeltaMovement().z>0,"Returning disc reflects from wall");
        double wallBack=h.absolutePos(new net.minecraft.core.BlockPos(1,3,4)).getZ();
        for(int i=0;i<40 && !disc.isRemoved();i++) {
            h.assertTrue(disc.getZ()>=wallBack,"Returning disc stays on its side of wall");
            disc.tick();
        }
        h.assertTrue(!disc.isRemoved(),"Disc remains in flight rather than teleporting across wall");
        forceReturn(disc,h,DiscPhysics.MAX_FLIGHT_TICKS);
        disc.tick();
        h.assertTrue(disc.isRemoved(),"Trapped disc drops at timeout");
        h.assertTrue(p.getInventory().countItem(ModItems.get(DiscTier.IRON))==0,"Timeout cannot return through wall");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            new net.minecraft.world.phys.AABB(start,start).inflate(20),e->e.getItem().is(ModItems.get(DiscTier.IRON)));
        h.assertTrue(drops.size()==1 && drops.getFirst().getZ()>=wallBack,"Exactly one recoverable disc drops on the blocked side");
        h.succeed();
    }
    @GameTest public void cannotCatchAcrossThinWall(GameTestHelper h) {
        var p=player(h);
        var cell=net.minecraft.world.phys.Vec3.atLowerCornerOf(h.absolutePos(new net.minecraft.core.BlockPos(1,2,3)));
        p.setPos(cell.add(0.5,0,0.1));
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.GLASS_PANE);
        var disc=projectile(h,p);
        disc.setPos(cell.add(0.5,p.getBbHeight()*0.7,0.8));
        disc.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-0.2));
        forceReturn(disc,h,20);
        disc.tick();
        h.assertTrue(!disc.isRemoved() && p.getInventory().countItem(ModItems.get(DiscTier.IRON))==0,"Catch radius cannot reach through glass pane");
        h.assertTrue(disc.getZ()>cell.z+0.5,"Disc stays outside pane");
        DiscFlights.recover(p);
        h.assertTrue(p.getInventory().countItem(ModItems.get(DiscTier.IRON))==0,"Disconnect/shutdown recovery cannot cross pane either");
        h.succeed();
    }
    @GameTest public void toolDurabilityAndThrowWear(GameTestHelper h) {
        var p=player(h);
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        int[] expected={95,125,16,780,1015};
        for(DiscTier tier:DiscTier.values()) {
            var item=ModItems.get(tier);
            var stack=new ItemStack(item);
            h.assertTrue(stack.getMaxDamage()==expected[tier.ordinal()],"Matches half vanilla material durability: "+tier);
            p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            item.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
            DiscFlights.recover(p);
            h.assertTrue(p.getMainHandItem().getDamageValue()==1,"One successful throw costs one durability");
            p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(p.getMainHandItem()));
        }
        var lastUse=new ItemStack(ModItems.get(DiscTier.GOLD));
        lastUse.setDamageValue(lastUse.getMaxDamage()-1);
        p.setItemInHand(InteractionHand.MAIN_HAND,lastUse);
        ModItems.get(DiscTier.GOLD).use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        DiscFlights.recover(p);
        h.assertTrue(p.getMainHandItem().isEmpty(),"Last durability breaks the disc without returning a duplicate");
        p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.get(DiscTier.IRON)));
        ModItems.get(DiscTier.IRON).use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        DiscFlights.recover(p);
        h.assertTrue(p.getMainHandItem().getDamageValue()==0,"Creative throws do not wear the disc");
        h.succeed();
    }
    @GameTest public void unbreakingUsesVanillaDurabilityRules(GameTestHelper h) {
        var p=player(h);
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
        for(DiscTier tier:DiscTier.values())
            h.assertTrue(enchantment.value().canEnchant(new ItemStack(ModItems.get(tier))),"Unbreaking accepts "+tier);
        var stack=new ItemStack(ModItems.get(DiscTier.DIAMOND));
        stack.enchant(enchantment,3);
        for(int i=0;i<1000;i++) stack.hurtAndBreak(1,h.getLevel(),p,broken -> h.fail("Unexpected break"));
        h.assertTrue(stack.getDamageValue()>100 && stack.getDamageValue()<400,"Unbreaking III prevents approximately three quarters of wear");
        h.succeed();
    }
    @GameTest public void mendingRepairsHeldDiscsWithExperience(GameTestHelper h) {
        var p=player(h);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING);
        for(DiscTier tier:DiscTier.values())
            h.assertTrue(enchantment.value().canEnchant(new ItemStack(ModItems.get(tier))),"Mending accepts "+tier);
        var stack=new ItemStack(ModItems.get(DiscTier.IRON));
        stack.setDamageValue(20);stack.enchant(enchantment,1);
        p.setItemInHand(InteractionHand.OFF_HAND,stack);
        var orb=new net.minecraft.world.entity.ExperienceOrb(h.getLevel(),p.getX(),p.getY(),p.getZ(),5);
        h.getLevel().addFreshEntity(orb);orb.playerTouch(p);
        h.assertTrue(stack.getDamageValue()==10,"Five XP repairs ten durability in the off-hand");
        h.succeed();
    }
    @GameTest public void fullCreativeInventoryDropsExactlyOne(GameTestHelper h) {
        var p = player(h);
        p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        for (int i=0;i<36;i++) p.getInventory().setItem(i, new ItemStack(Items.STONE,64));
        var d = projectile(h,p);
        d.catchBy(p);
        var drops = h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            p.getBoundingBox().inflate(2), e -> e.getItem().is(ModItems.get(DiscTier.IRON)));
        h.assertTrue(drops.size()==1 && drops.getFirst().getItem().getCount()==1, "Full creative inventory drops the disc instead of deleting it");
        h.assertTrue(p.getInventory().getItem(2).is(Items.STONE), "Occupied slot remains unchanged");
        d.catchBy(p);
        h.assertTrue(drops.getFirst().getItem().getCount()==1, "Second catch cannot duplicate drop");
        h.succeed();
    }
    @GameTest public void piercesMultipleMobsOncePerPass(GameTestHelper h) {
        var p = player(h);
        var first = h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityTypes.HUSK, 1.5F,2,2.5F);
        var second = h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityTypes.HUSK, 1.5F,2,3.5F);
        var d = projectile(h,p);
        var start = d.position();
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,4));
        d.tick();
        h.assertTrue(first.getHealth()<20 && second.getHealth()<20, "One swept segment hits both targets");
        float firstHealth=first.getHealth(), secondHealth=second.getHealth();
        first.invulnerableTime = 0; second.invulnerableTime = 0;
        d.setPos(start);d.tick();
        h.assertTrue(first.getHealth()==firstHealth && second.getHealth()==secondHealth, "Same pass never hits a target twice");
        d.catchBy(p);
        h.succeed();
    }
    @GameTest public void reflectsFromSolidBlock(GameTestHelper h) {
        var p = player(h);
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.STONE);
        var d = projectile(h,p);
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,2));
        d.tick();
        h.assertTrue(d.getDeltaMovement().z<0, "Wall reflects the projectile");
        h.assertTrue(Math.abs(d.getDeltaMovement().z+2.35)<0.001, "Ricochet accelerates by 17.5 percent");
        d.catchBy(p);
        h.succeed();
    }
    @GameTest public void completesFlightAndReturnsToOriginalSlot(GameTestHelper h) {
        var p = player(h);
        var d = projectile(h,p);
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));
        for (int tick=0;tick<120 && !d.isRemoved();tick++) d.tick();
        h.assertTrue(d.isRemoved(), "Normal flight finishes before emergency recovery timeout");
        h.assertTrue(p.getInventory().getItem(2).is(ModItems.get(DiscTier.IRON)), "Normal homing catches into original slot");
        h.succeed();
    }

    @GameTest public void savesFlightWithDataComponents(GameTestHelper h) {
        var p = player(h);
        var d = projectile(h,p);
        d.getItem().set(DataComponents.CUSTOM_NAME, Component.literal("Saved disc"));
        var reporter = new net.minecraft.util.ProblemReporter.Collector();
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(reporter,h.getLevel().registryAccess());
        d.addAdditionalSaveData(output);
        var restored = new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());
        restored.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(
            reporter,h.getLevel().registryAccess(),output.buildResult()));
        h.assertTrue(reporter.isEmpty(), "Flight codecs report no errors: " + reporter.getReport());
        h.assertTrue(p.getUUID().equals(restored.throwerId()), "Thrower UUID survives reload");
        h.assertTrue(ItemStack.isSameItemSameComponents(d.getItem(),restored.getItem()), "Complete component map survives reload");
        d.discard();
        restored.catchBy(p);
        h.assertTrue(p.getInventory().getItem(2).is(ModItems.get(DiscTier.IRON)), "Restored flight remembers source slot");
        h.succeed();
    }
    @GameTest public void allCraftingAndSmithingRecipesWork(GameTestHelper h) {
        var materials = new net.minecraft.world.item.Item[]{Items.COPPER_INGOT,Items.IRON_INGOT,Items.GOLD_INGOT,Items.DIAMOND};
        for (int i=0;i<materials.length;i++) {
            var items = new java.util.ArrayList<ItemStack>(java.util.Collections.nCopies(9,ItemStack.EMPTY));
            for (int slot : new int[]{1,3,5,7}) items.set(slot,new ItemStack(materials[i]));
            var input = net.minecraft.world.item.crafting.CraftingInput.of(3,3,items);
            var recipe = h.getLevel().recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
            h.assertTrue(recipe.value().assemble(input).is(ModItems.get(DiscTier.values()[i])), "Correct crafting output for " + DiscTier.values()[i]);
        }
        var diamond = new ItemStack(ModItems.get(DiscTier.DIAMOND));
        diamond.set(DataComponents.CUSTOM_NAME, Component.literal("Upgrade me"));
        var input = new net.minecraft.world.item.crafting.SmithingRecipeInput(
            new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),diamond,new ItemStack(Items.NETHERITE_INGOT));
        var recipe = h.getLevel().recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMITHING,input,h.getLevel()).orElseThrow();
        var result = recipe.value().assemble(input);
        h.assertTrue(result.is(ModItems.get(DiscTier.NETHERITE)), "Smithing upgrades diamond to netherite");
        h.assertTrue(Component.literal("Upgrade me").equals(result.get(DataComponents.CUSTOM_NAME)), "Smithing preserves custom components");
        h.succeed();
    }
    @GameTest public void originalSlotAndComponents(GameTestHelper h) {
        var p = player(h);
        p.getInventory().setSelectedSlot(2);
        ItemStack stack = new ItemStack(ModItems.get(DiscTier.DIAMOND));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("My returning disc"));
        p.setItemInHand(InteractionHand.MAIN_HAND, stack);
        ModItems.get(DiscTier.DIAMOND).use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        h.assertTrue(p.getMainHandItem().isEmpty(), "Throw transfers real inventory stack");
        p.getInventory().setSelectedSlot(5);
        DiscFlights.recover(p);
        h.assertTrue(p.getInventory().getItem(2).is(ModItems.get(DiscTier.DIAMOND)), "Returns to original hotbar slot");
        h.assertTrue(p.getInventory().getItem(5).isEmpty(), "Does not use newly selected slot");
        h.assertTrue(Component.literal("My returning disc").equals(p.getInventory().getItem(2).get(DataComponents.CUSTOM_NAME)), "Preserves data components");
        DiscFlights.recover(p);
        h.assertTrue(p.getInventory().countItem(ModItems.get(DiscTier.DIAMOND)) == 1, "Repeat recovery cannot duplicate");
        h.succeed();
    }
    @GameTest public void offhandAndOccupiedSlot(GameTestHelper h) {
        var p = player(h);
        var disc = ModItems.get(DiscTier.IRON);
        p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(disc));
        disc.use(h.getLevel(), p, InteractionHand.OFF_HAND);
        DiscFlights.recover(p);
        h.assertTrue(p.getOffhandItem().is(disc), "Offhand catches into offhand");
        p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(p.getOffhandItem()));
        disc.use(h.getLevel(), p, InteractionHand.OFF_HAND);
        p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
        DiscFlights.recover(p);
        h.assertTrue(p.getOffhandItem().is(Items.STICK), "Occupied offhand is not overwritten");
        h.assertTrue(p.getInventory().countItem(disc) == 1, "Disc falls back to main inventory");
        h.succeed();
    }

    @GameTest public void customEnchantmentsAndBooks(GameTestHelper h) {
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var p=player(h);
        var d=projectile(h,p);
        String[] names={"homing","range","curve","keen_edge"};
        int[] levels={4,5,2,3};
        for(int i=0;i<names.length;i++) {
            var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id(names[i]));
            var enchantment=registry.getOrThrow(key);
            h.assertTrue(enchantment.value().getMaxLevel()==levels[i],"Correct maximum enchantment level");
            for(var tier:DiscTier.values()) h.assertTrue(enchantment.value().canEnchant(new ItemStack(ModItems.get(tier))),"Enchantment accepts every disc tier");
            h.assertTrue(!enchantment.value().canEnchant(new ItemStack(Items.DIAMOND_SWORD)),"Disc enchantments reject swords");
            var book=net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new net.minecraft.world.item.enchantment.EnchantmentInstance(enchantment,levels[i]));
            h.assertTrue(book.is(Items.ENCHANTED_BOOK),"Uses vanilla enchanted books");
            var anvil=new net.minecraft.world.inventory.AnvilMenu(0,p.getInventory());
            anvil.getSlot(0).set(new ItemStack(ModItems.get(DiscTier.IRON)));
            anvil.getSlot(1).set(book);
            anvil.createResult();
            h.assertTrue(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantment,anvil.getSlot(2).getItem())==levels[i],"Enchanted book applies through actual anvil menu");
            d.getItem().enchant(enchantment,levels[i]);
            h.assertTrue(d.enchantmentLevel(names[i])==levels[i],"Flight reads enchantment components");
        }
        d.catchBy(p);h.succeed();
    }
    @GameTest public void homingTargetsOnlyVisibleForwardMobs(GameTestHelper h) {
        var p=player(h);var d=projectile(h,p);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("homing")));
        d.getItem().enchant(enchantment,4);
        h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityTypes.HUSK,3.5F,2,5.5F);
        var start=d.position();
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));d.tick();
        h.assertTrue(d.getDeltaMovement().x>0 && d.getDeltaMovement().x<0.15,"Gentle curve toward forward mob");
        d.setPos(start);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-1.25));d.tick();
        h.assertTrue(Math.abs(d.getDeltaMovement().x)<1e-8,"No lock onto mob behind disc");
        h.setBlock(2,3,3,net.minecraft.world.level.block.Blocks.STONE);
        d.setPos(start);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));d.tick();
        h.assertTrue(Math.abs(d.getDeltaMovement().x)<1e-8,"Homing cannot lock through a solid wall");
        d.catchBy(p);h.succeed();
    }
    @GameTest public void shattersPotsAndPreservesContents(GameTestHelper h) {
        var p=player(h);
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(1,3,3));
        h.setBlock(1,3,3,net.minecraft.world.level.block.Blocks.DECORATED_POT);
        var pot=(net.minecraft.world.level.block.entity.DecoratedPotBlockEntity)h.getLevel().getBlockEntity(pos);
        pot.applyComponentsFromItemStack(new ItemStack(Items.DECORATED_POT));
        pot.setTheItem(new ItemStack(Items.DIAMOND,3));
        var d=projectile(h,p);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,3));d.tick();
        h.assertTrue(!h.getLevel().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DECORATED_POT),"Thrown disc shatters decorated pot");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2));
        h.assertTrue(drops.stream().filter(e->e.getItem().is(Items.DIAMOND)).mapToInt(e->e.getItem().getCount()).sum()==3,"Stored contents drop exactly once");
        h.assertTrue(drops.stream().filter(e->e.getItem().is(Items.BRICK)).mapToInt(e->e.getItem().getCount()).sum()==4,"Plain pot drops four bricks");
        h.assertTrue(d.getDeltaMovement().z>0,"Disc continues past broken pot");
        d.catchBy(p);h.succeed();
    }

    @GameTest public void rangeEnchantmentDelaysReturn(GameTestHelper h) {
        var p=player(h);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("range")));
        for(int level=0;level<=5;level++) {
            var d=projectile(h,p);
            if(level>0)d.getItem().enchant(enchantment,level);
            var reporter=new net.minecraft.util.ProblemReporter.Collector();
            var saved=net.minecraft.world.level.storage.TagValueOutput.createWithContext(reporter,h.getLevel().registryAccess());
            d.addAdditionalSaveData(saved);saved.putDouble("Distance",12);
            d.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(reporter,h.getLevel().registryAccess(),saved.buildResult()));
            d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));d.tick();
            var after=net.minecraft.world.level.storage.TagValueOutput.createWithContext(reporter,h.getLevel().registryAccess());d.addAdditionalSaveData(after);
            var input=net.minecraft.world.level.storage.TagValueInput.create(reporter,h.getLevel().registryAccess(),after.buildResult());
            h.assertTrue(input.getBooleanOr("Returning",false)==(level==0),"Range enchantment extends outbound flight beyond twelve blocks");
            d.discard();
        }
        h.succeed();
    }

    @GameTest public void rejectsIncompatibleBooksEvenInCreative(GameTestHelper h) {
        var p=player(h);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        for(var key:java.util.List.of(net.minecraft.world.item.enchantment.Enchantments.PROTECTION,net.minecraft.world.item.enchantment.Enchantments.POWER,net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY,net.minecraft.world.item.enchantment.Enchantments.FLAME)) {
            var anvil=new net.minecraft.world.inventory.AnvilMenu(0,p.getInventory());
            anvil.getSlot(0).set(new ItemStack(ModItems.get(DiscTier.IRON)));
            anvil.getSlot(1).set(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new net.minecraft.world.item.enchantment.EnchantmentInstance(registry.getOrThrow(key),1)));
            anvil.createResult();h.assertTrue(anvil.getSlot(2).getItem().isEmpty(),"Incompatible books cannot be applied even in creative");
        }
        for(var key:java.util.List.of(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT,net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK)) {
            var anvil=new net.minecraft.world.inventory.AnvilMenu(0,p.getInventory());
            anvil.getSlot(0).set(new ItemStack(ModItems.get(DiscTier.IRON)));
            anvil.getSlot(1).set(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new net.minecraft.world.item.enchantment.EnchantmentInstance(registry.getOrThrow(key),1)));
            anvil.createResult();h.assertTrue(!anvil.getSlot(2).getItem().isEmpty(),"Fire Aspect and Knockback books work");
        }
        h.succeed();
    }
    @GameTest public void fireAspectAndKnockbackAffectTargets(GameTestHelper h) {
        var p=player(h);
        var stack=new ItemStack(ModItems.get(DiscTier.IRON));
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        stack.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT),1);
        stack.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK),2);
        var d=new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());d.launch(p,stack,2);h.getLevel().addFreshEntity(d);
        h.assertTrue(!d.isOnFire() && !d.displayFireAnimation(),"Fire Aspect has no flying fire effect");
        var mob=h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityTypes.HUSK,1.5F,2,2.5F);
        d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,2));d.tick();
        h.assertTrue(mob.isOnFire(),"Fire Aspect ignites a successfully hit mob");
        h.assertTrue(mob.getDeltaMovement().z>0,"Knockback pushes target along the flight direction");
        d.catchBy(p);h.succeed();
    }
    @GameTest public void deflectsGhastFireballs(GameTestHelper h) {
        var p=player(h);var d=projectile(h,p);
        var fireball=new net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball(net.minecraft.world.entity.EntityTypes.FIREBALL,h.getLevel());
        fireball.setPos(d.position().add(0,0,2));fireball.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,-0.8));
        h.getLevel().addFreshEntity(fireball);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,3));d.tick();
        h.assertTrue(fireball.getDeltaMovement().z>0,"Disc reverses ghast fireball");
        h.assertTrue(fireball.getOwner()==p,"Reflected fireball credits the thrower");
        fireball.discard();d.catchBy(p);h.succeed();
    }
    @GameTest public void meleeDamageIsShovelMinusTwoPointFive(GameTestHelper h) {
        var shovels=new net.minecraft.world.item.Item[]{Items.COPPER_SHOVEL,Items.IRON_SHOVEL,Items.GOLDEN_SHOVEL,Items.DIAMOND_SHOVEL,Items.NETHERITE_SHOVEL};
        for(var tier:DiscTier.values()) {
            var disc=new ItemStack(ModItems.get(tier)).get(DataComponents.ATTRIBUTE_MODIFIERS);
            var shovel=new ItemStack(shovels[tier.ordinal()]).get(DataComponents.ATTRIBUTE_MODIFIERS);
            for(var entry:shovel.modifiers()) {
                double actual=disc.modifiers().stream().filter(e->e.attribute().equals(entry.attribute())).findFirst().orElseThrow().modifier().amount();
                double reduction=entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)?2.5:0;
                h.assertTrue(Math.abs(actual-(entry.modifier().amount()-reduction))<1e-8,"Melee damage is shovel minus 2.5; speed unchanged");
            }
        }
        h.succeed();
    }
    @GameTest public void disablesBlockingShield(GameTestHelper h) {
        var p=player(h);var victim=player(h);
        victim.setPos(p.position().add(0,0,2));victim.setYRot(180);
        var shield=new ItemStack(Items.SHIELD);
        var blocking=shield.get(DataComponents.BLOCKS_ATTACKS);
        // Model an already-raised shield without relying on artificial players' network-use ticks.
        shield.set(DataComponents.BLOCKS_ATTACKS,new net.minecraft.world.item.component.BlocksAttacks(0,
            blocking.disableCooldownScale(),blocking.damageReductions(),blocking.itemDamage(),blocking.bypassedBy(),blocking.blockSound(),blocking.disableSound()));
        victim.setItemInHand(InteractionHand.OFF_HAND,shield);victim.startUsingItem(InteractionHand.OFF_HAND);
        h.assertTrue(victim.isBlocking(),"Test victim must be actively blocking: "+victim.getTicksUsingItem());
        var d=projectile(h,p);d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,3));d.tick();
        h.assertTrue(victim.getCooldowns().isOnCooldown(victim.getOffhandItem()),"Front-facing shield receives a cooldown");
        h.assertTrue(!victim.isUsingItem(),"Shield is lowered after impact");
        d.catchBy(p);h.succeed();
    }

    @GameTest public void curveFollowsLaunchStrafe(GameTestHelper h) {
        var p=player(h);p.setYRot(0);p.setXRot(0);
        var enchantment=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("curve")));
        double first=0;
        for(int level=1;level<=2;level++) {
            var stack=new ItemStack(ModItems.get(DiscTier.IRON));stack.enchant(enchantment,level);
            p.setKnownMovement(new net.minecraft.world.phys.Vec3(-0.2,0,0));
            var d=new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());d.launch(p,stack,2);h.getLevel().addFreshEntity(d);d.tick();
            h.assertTrue(d.getDeltaMovement().x<0,"Right strafe curves right");
            if(level==1)first=Math.abs(d.getDeltaMovement().x);else h.assertTrue(Math.abs(d.getDeltaMovement().x)>first,"Curve II bends more than Curve I");
            d.discard();
            p.setKnownMovement(new net.minecraft.world.phys.Vec3(0.2,0,0));
            var left=new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());left.launch(p,stack.copy(),2);h.getLevel().addFreshEntity(left);left.tick();
            h.assertTrue(left.getDeltaMovement().x>0,"Left strafe curves left");left.discard();
        }
        h.succeed();
    }

    @GameTest public void returnsAtRoundedRangeBoundary(GameTestHelper h) {
        var p=player(h);p.setPos(p.position().add(0,30,0));
        var d=projectile(h,p);
        for(int dz=-16;dz<=32;dz+=16) h.getLevel().getChunk(net.minecraft.core.BlockPos.containing(p.position().add(0,0,dz)));
        d.setPos(d.position().add(0,0,12));d.setDeltaMovement(new net.minecraft.world.phys.Vec3(0,0,1.25));
        var reporter=new net.minecraft.util.ProblemReporter.Collector();
        var saved=net.minecraft.world.level.storage.TagValueOutput.createWithContext(reporter,h.getLevel().registryAccess());
        d.addAdditionalSaveData(saved);saved.putDouble("Distance",Math.nextDown(12.0));
        d.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(reporter,h.getLevel().registryAccess(),saved.buildResult()));
        d.tick();
        var after=net.minecraft.world.level.storage.TagValueOutput.createWithContext(reporter,h.getLevel().registryAccess());d.addAdditionalSaveData(after);
        var input=net.minecraft.world.level.storage.TagValueInput.create(reporter,h.getLevel().registryAccess(),after.buildResult());
        h.assertTrue(input.getBooleanOr("Returning",false),"Rounding near max range starts return immediately instead of hovering");
        for(int i=0;i<100 && !d.isRemoved();i++)d.tick();
        h.assertTrue(d.isRemoved() && p.getInventory().getItem(2).is(ModItems.get(DiscTier.IRON)),"Rounded-boundary disc comes back normally");
        h.succeed();
    }
    @GameTest public void steeringEnchantmentsAreExclusive(GameTestHelper h) {
        var p=player(h);var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var homing=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("homing")));
        var curve=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id("curve")));
        h.assertTrue(!net.minecraft.world.item.enchantment.Enchantment.areCompatible(homing,curve),"Enchanting recognizes exclusive steering options");
        for(var mode:java.util.List.of(net.minecraft.world.level.GameType.SURVIVAL,net.minecraft.world.level.GameType.CREATIVE)) {
            p.setGameMode(mode);
            for(boolean reverse:new boolean[]{false,true}) {
                var stack=new ItemStack(ModItems.get(DiscTier.IRON));stack.enchant(reverse?curve:homing,1);
                var anvil=new net.minecraft.world.inventory.AnvilMenu(0,p.getInventory());anvil.getSlot(0).set(stack);
                anvil.getSlot(1).set(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new net.minecraft.world.item.enchantment.EnchantmentInstance(reverse?homing:curve,1)));
                anvil.createResult();h.assertTrue(anvil.getSlot(2).getItem().isEmpty(),"Cannot combine Curve and Homing in either order or game mode");
            }
        }
        h.succeed();
    }
    @GameTest public void migratesOldEnchantedDiscs(GameTestHelper h) {
        var registry=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var stack=new ItemStack(ModItems.get(DiscTier.IRON));
        for(String name:new String[]{"homing","curve","keen_edge","ricochet"}) {
            var holder=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,ThrowingDiscMod.id(name)));
            stack.enchant(holder,name.equals("keen_edge")?5:1);
        }
        stack.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FLAME),1);
        DiscEnchantments.normalize(stack,h.getLevel());
        var p=player(h);var d=new ThrowingDiscEntity(ModEntities.DISC,h.getLevel());d.launch(p,stack,2);
        h.assertTrue(d.enchantmentLevel("keen_edge")==3 && d.enchantmentLevel("ricochet")==0,"Legacy Keen Edge clamps to III and Ricochet is removed");
        h.assertTrue(d.enchantmentLevel("homing")==1 && d.enchantmentLevel("curve")==0,"Legacy dual steering keeps Homing");
        h.assertTrue(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT),d.getItem())==1,"Old Flame becomes Fire Aspect I");
        h.assertTrue(!d.isOnFire() && !d.displayFireAnimation(),"Migrated disc has no fire overlay");
        d.discard();h.succeed();
    }
    @GameTest public void stoppedOutboundDiscReturns(GameTestHelper h) {
        var p=player(h);p.setPos(p.position().add(0,30,0));
        var d=projectile(h,p);
        d.setPos(d.position().add(0,0,5));
        d.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        for(int dz=-16;dz<=32;dz+=16) h.getLevel().getChunk(net.minecraft.core.BlockPos.containing(p.position().add(0,0,dz)));
        for(int i=0;i<100 && !d.isRemoved();i++) d.tick();
        h.assertTrue(d.isRemoved() && p.getInventory().getItem(2).is(ModItems.get(DiscTier.IRON)),"A stopped disc returns instead of hovering until timeout");
        h.succeed();
    }
}
