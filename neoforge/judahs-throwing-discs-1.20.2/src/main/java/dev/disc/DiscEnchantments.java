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
  DiscEnchantment(String name,int maximum){super(Rarity.UNCOMMON,EnchantmentCategory.BREAKABLE,new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND});this.name=name;this.maximum=maximum;}
  @Override public boolean canEnchant(ItemStack stack){return stack.getItem() instanceof ThrowingDiscItem;}
  @Override public int getMaxLevel(){return maximum;}
  @Override public int getMinCost(int level){return 8+(level-1)*8;}
  @Override public int getMaxCost(int level){return getMinCost(level)+20;}
  @Override public float getDamageBonus(int level,net.minecraft.world.entity.MobType type){return name.equals("keen_edge")?level:0;}
  @Override protected boolean checkCompatibility(Enchantment other){return super.checkCompatibility(other)&&!(other instanceof DiscEnchantment d&&((name.equals("curve")&&d.name.equals("homing"))||(name.equals("homing")&&d.name.equals("curve"))));}
 }
 public static void initialize(net.neoforged.neoforge.registries.RegisterEvent event){String[] names={"homing","curve","range","keen_edge","harvest"};int[] levels={4,2,5,3,1};for(int i=0;i<names.length;i++)VALUES.put(names[i],register(event,ThrowingDiscMod.id(names[i]),new DiscEnchantment(names[i],levels[i])));}
 public static Enchantment get(String name){return VALUES.get(name);}
 public static boolean allowed(Enchantment e){return e instanceof DiscEnchantment||e==Enchantments.UNBREAKING||e==Enchantments.MENDING||e==Enchantments.FIRE_ASPECT||e==Enchantments.KNOCKBACK||e==Enchantments.VANISHING_CURSE;}
 public static void normalize(ItemStack stack,Level level){Map<Enchantment,Integer> map=new HashMap<>(EnchantmentHelper.getEnchantments(stack));boolean changed=false;
  if(map.getOrDefault(get("keen_edge"),0)>3){map.put(get("keen_edge"),3);changed=true;}
  if(map.containsKey(get("homing"))&&map.containsKey(get("curve"))){map.remove(get("curve"));changed=true;}
  if(map.remove(Enchantments.FLAMING_ARROWS)!=null){map.put(Enchantments.FIRE_ASPECT,Math.max(1,map.getOrDefault(Enchantments.FIRE_ASPECT,0)));changed=true;}
  if(changed)EnchantmentHelper.setEnchantments(map,stack);
 }
 private static Enchantment register(net.neoforged.neoforge.registries.RegisterEvent event, net.minecraft.resources.ResourceLocation id, Enchantment value) { event.register(net.minecraft.core.registries.Registries.ENCHANTMENT, id, () -> value); return value; }
 private DiscEnchantments(){}
}
