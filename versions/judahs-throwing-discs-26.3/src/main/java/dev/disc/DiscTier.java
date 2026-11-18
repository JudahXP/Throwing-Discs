package dev.disc;

import net.minecraft.world.item.ToolMaterial;

/** Flight damage is reconstruction tuning; durability follows vanilla tool materials. */
public enum DiscTier {
    COPPER("copper", 4, ToolMaterial.COPPER), IRON("iron", 5, ToolMaterial.IRON),
    GOLD("gold", 4, ToolMaterial.GOLD), DIAMOND("diamond", 7, ToolMaterial.DIAMOND),
    NETHERITE("netherite", 8, ToolMaterial.NETHERITE);
    public final String id;
    public final float damage;
    public final ToolMaterial material;
    DiscTier(String id, float damage, ToolMaterial material) {
        this.id = id; this.damage = damage; this.material = material;
    }
}
