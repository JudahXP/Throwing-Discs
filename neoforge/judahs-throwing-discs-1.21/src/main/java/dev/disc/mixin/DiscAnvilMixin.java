package dev.disc.mixin;

import dev.disc.ThrowingDiscItem;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class DiscAnvilMixin {
    @Inject(method = "createResult", at = @At("RETURN"))
    private void throwingDisc$restrictBooks(CallbackInfo ci) {
        AnvilMenu menu = (AnvilMenu)(Object)this;
        ItemStack disc = menu.getSlot(0).getItem();
        ItemStack book = menu.getSlot(1).getItem();
        if (!(disc.getItem() instanceof ThrowingDiscItem)) return;
        var result = menu.getSlot(2).getItem();
        var applied = EnchantmentHelper.getEnchantmentsForCrafting(result).keySet();
        for (var first : applied) for (var second : applied) {
            if (!first.equals(second) && !net.minecraft.world.item.enchantment.Enchantment.areCompatible(first,second)) {
                menu.getSlot(2).set(ItemStack.EMPTY);
                return;
            }
        }
        if (!book.is(Items.ENCHANTED_BOOK)) return;
        for (var enchantment : EnchantmentHelper.getEnchantmentsForCrafting(book).keySet()) {
            if (!enchantment.value().canEnchant(disc)) {
                menu.getSlot(2).set(ItemStack.EMPTY);
                return;
            }
        }
    }
}
