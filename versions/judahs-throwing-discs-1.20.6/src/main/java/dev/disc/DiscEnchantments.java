package dev.disc;
import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
public final class DiscEnchantments {
 private static final Map<String,Enchantment> VALUES=new HashMap<>();
 public static class DiscEnchantment extends Enchantment {
  public final String name;private final int maximum;
  DiscEnchantment(String name,int maximum){super(Enchantment.definition(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,ThrowingDiscMod.id("discs")),5,maximum,Enchantment.dynamicCost(8,8),Enchantment.dynamicCost(28,8),2,EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND));this.name=name;this.maximum=maximum;}
  @Override public boolean canEnchant(ItemStack stack){return stack.getItem() instanceof ThrowingDiscItem;}
  @Override public float getDamageBonus(int level,net.minecraft.world.entity.EntityType<?> type){return name.equals("keen_edge")?level:0;}
  @Override protected boolean checkCompatibility(Enchantment other){return super.checkCompatibility(other)&&!(other instanceof DiscEnchantment d&&((name.equals("curve")&&d.name.equals("homing"))||(name.equals("homing")&&d.name.equals("curve"))));}
 }
 public static void initialize(){String[] names={"homing","curve","range","keen_edge","harvest"};int[] levels={4,2,5,3,1};for(int i=0;i<names.length;i++)VALUES.put(names[i],Registry.register(BuiltInRegistries.ENCHANTMENT,ThrowingDiscMod.id(names[i]),new DiscEnchantment(names[i],levels[i])));}
 public static Enchantment get(String name){return VALUES.get(name);}
 public static boolean allowed(Enchantment e){return e instanceof DiscEnchantment||e==Enchantments.UNBREAKING||e==Enchantments.MENDING||e==Enchantments.FIRE_ASPECT||e==Enchantments.KNOCKBACK||e==Enchantments.VANISHING_CURSE;}
 public static void normalize(ItemStack stack,Level level){
  var map=new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
  map.set(get("keen_edge"),Math.min(3,map.getLevel(get("keen_edge"))));
  if(map.getLevel(get("homing"))>0)map.set(get("curve"),0);
  if(map.getLevel(Enchantments.FLAME)>0){map.set(Enchantments.FLAME,0);map.set(Enchantments.FIRE_ASPECT,Math.max(1,map.getLevel(Enchantments.FIRE_ASPECT)));}
  EnchantmentHelper.setEnchantments(stack,map.toImmutable());
 }
 private DiscEnchantments(){}
}
