package dev.disc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/** Keep saved discs usable while retiring old enchantment behavior. */
public final class DiscEnchantments {
    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ThrowingDiscMod.id(name));
    }
    public static void normalize(ItemStack stack, Level level) {
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var keen = registry.getOrThrow(key("keen_edge"));
        var homing = registry.getOrThrow(key("homing"));
        var curve = registry.getOrThrow(key("curve"));
        var flame = registry.getOrThrow(Enchantments.FLAME);
        var fire = registry.getOrThrow(Enchantments.FIRE_ASPECT);
        int oldKeen = EnchantmentHelper.getItemEnchantmentLevel(keen,stack);
        int oldFlame = EnchantmentHelper.getItemEnchantmentLevel(flame,stack);
        boolean conflicting = EnchantmentHelper.getItemEnchantmentLevel(homing,stack)>0 && EnchantmentHelper.getItemEnchantmentLevel(curve,stack)>0;
        if (oldKeen<=3 && oldFlame==0 && !conflicting) return;
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            if(oldKeen>3) mutable.set(keen,3);
            if(conflicting) mutable.set(curve,0);
            if(oldFlame>0) {
                mutable.set(flame,0);
                mutable.set(fire,Math.max(1,mutable.getLevel(fire)));
            }
        });
    }
    private DiscEnchantments() {}
}
