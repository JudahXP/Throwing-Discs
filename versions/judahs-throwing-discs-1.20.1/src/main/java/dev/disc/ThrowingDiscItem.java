package dev.disc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ThrowingDiscItem extends Item {
    public final DiscTier tier;
    public ThrowingDiscItem(Properties properties, DiscTier tier) { super(properties); this.tier = tier; }
    @Override public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if(!level.isClientSide) DiscEnchantments.normalize(stack, level);
    }
    @Override public boolean hurtEnemy(ItemStack stack, net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel server) server.playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 0.65F, 1.0F);
        return true;
    }
    @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()
                && player.getOffhandItem().getItem() instanceof ThrowingDiscItem) return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return net.minecraft.world.InteractionResultHolder.fail(player.getItemInHand(hand));
        if (level instanceof ServerLevel server && player instanceof ServerPlayer owner) {
            int slot = hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND
                : player.getInventory().selected;
            ThrowingDiscEntity disc = new ThrowingDiscEntity(ModEntities.DISC, server);
            disc.launch(owner, stack.copyWithCount(1), slot);
            if (!server.addFreshEntity(disc)) return net.minecraft.world.InteractionResultHolder.fail(player.getItemInHand(hand));
            disc.damageForThrow(owner, hand);
            // One real stack changes custody, including in creative mode. No ghost item can be moved or copied.
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.getInventory().setChanged();
            owner.containerMenu.broadcastChanges();
            if (disc.isRemoved()) return net.minecraft.world.InteractionResultHolder.success(player.getItemInHand(hand));
            DiscFlights.track(disc);
            disc.keepFlightLoaded(server);
            player.getCooldowns().addCooldown(this, 4);
            player.displayClientMessage(Component.translatable("message.throwing_disc.in_flight"), true);
            server.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.65F, 1.45F);
        }
        return net.minecraft.world.InteractionResultHolder.success(player.getItemInHand(hand));
    }
    @Override public int getEnchantmentValue(){return tier==DiscTier.COPPER?13:tier.material.getEnchantmentValue();}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack ingredient){return tier==DiscTier.COPPER?ingredient.is(net.minecraft.world.item.Items.COPPER_INGOT):tier.material.getRepairIngredient().test(ingredient);}
    @Override public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute,net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot){
        if(slot!=net.minecraft.world.entity.EquipmentSlot.MAINHAND)return super.getDefaultAttributeModifiers(slot);
        return com.google.common.collect.ImmutableMultimap.of(
            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,new net.minecraft.world.entity.ai.attributes.AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Disc damage",tier.melee-1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION),
            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,new net.minecraft.world.entity.ai.attributes.AttributeModifier(BASE_ATTACK_SPEED_UUID,"Disc speed",-3,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
    }
}
