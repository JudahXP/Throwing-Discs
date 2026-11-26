package dev.disc.paper;
public enum DiscTier {
 COPPER("copper",5,3,14,95), IRON("iron",6,4,16,125), GOLD("gold",5,3,18,16), DIAMOND("diamond",7.5,5,19,780), NETHERITE("netherite",8.5,6,21,1015);
 public final String id; public final double thrownDamage,meleeDamage,range; public final int durability;
 DiscTier(String id,double thrownDamage,double meleeDamage,double range,int durability) { this.id=id;this.thrownDamage=thrownDamage;this.meleeDamage=meleeDamage;this.range=range;this.durability=durability; }
}
