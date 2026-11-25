package dev.disc;
import net.minecraft.world.item.Tiers;
public enum DiscTier {
 IRON("iron",6,4,16,Tiers.IRON),
 GOLD("gold",5,3,18,Tiers.GOLD), DIAMOND("diamond",7.5F,5,19,Tiers.DIAMOND),
 NETHERITE("netherite",8.5F,6,21,Tiers.NETHERITE),
 COPPER("copper",5,3,14,Tiers.IRON);
 public final String id;
 public final float damage, melee;
 public final double range;
 public final Tiers material;
 DiscTier(String id,float damage,float melee,double range,Tiers material) {
  this.id=id;this.damage=damage;this.melee=melee;this.range=range;this.material=material;
 }
}
