package dev.disc;
import net.minecraft.world.item.ToolMaterial;
public enum DiscTier {
 COPPER("copper",5,3,14,ToolMaterial.COPPER), IRON("iron",6,4,16,ToolMaterial.IRON),
 GOLD("gold",5,3,18,ToolMaterial.GOLD), DIAMOND("diamond",7.5F,5,19,ToolMaterial.DIAMOND),
 NETHERITE("netherite",8.5F,6,21,ToolMaterial.NETHERITE);
 public final String id;
 public final float damage, melee;
 public final double range;
 public final ToolMaterial material;
 DiscTier(String id,float damage,float melee,double range,ToolMaterial material) {
  this.id=id;this.damage=damage;this.melee=melee;this.range=range;this.material=material;
 }
}
