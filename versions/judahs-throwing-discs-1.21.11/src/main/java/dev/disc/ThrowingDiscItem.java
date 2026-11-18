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
    @Override public void inventoryTick(ItemStack stack, ServerLevel level, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        DiscEnchantments.normalize(stack, level);
    }
    @Override public void hurtEnemy(ItemStack stack, net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel server) server.playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.SPEAR_HIT.value(), SoundSource.PLAYERS, 0.65F, 1.0F);
    }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()
                && player.getOffhandItem().getItem() instanceof ThrowingDiscItem) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        if (level instanceof ServerLevel server && player instanceof ServerPlayer owner) {
            int slot = hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND
                : player.getInventory().getSelectedSlot();
            ThrowingDiscEntity disc = new ThrowingDiscEntity(ModEntities.DISC, server);
            disc.launch(owner, stack.copyWithCount(1), slot);
            if (!server.addFreshEntity(disc)) return InteractionResult.FAIL;
            disc.damageForThrow(owner, hand);
            // One real stack changes custody, including in creative mode. No ghost item can be moved or copied.
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.getInventory().setChanged();
            owner.containerMenu.broadcastChanges();
            if (disc.isRemoved()) return InteractionResult.SUCCESS;
            DiscFlights.track(disc);
            disc.keepFlightLoaded(server);
            player.getCooldowns().addCooldown(stack, 4);
            player.displayClientMessage(Component.translatable("message.throwing_disc.in_flight"), true);
            server.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.65F, 1.45F);
        }
        return InteractionResult.SUCCESS;
    }
}
