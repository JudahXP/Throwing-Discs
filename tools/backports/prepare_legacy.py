from pathlib import Path
import shutil,json,re
root=Path(__file__).resolve().parents[2];base=root/'versions';v='1.20.1';r=base/('judahs-throwing-discs-'+v);matrix=json.loads((root/'tools/backports/matrix.json').read_text())
if r.exists():raise SystemExit('Already prepared')
shutil.copytree(base/'judahs-throwing-discs-1.21',r,ignore=shutil.ignore_patterns('build','run','.gradle'))
p=r/'gradle.properties';s=p.read_text();s=re.sub(r'^minecraft_version=.*','minecraft_version='+v,s,flags=re.M);s=re.sub(r'^mod_version=.*','mod_version=1.4.1+'+v,s,flags=re.M);s=re.sub(r'^fabric_api_version=.*','fabric_api_version='+matrix[v],s,flags=re.M);p.write_text(s)
p=r/'src/main/resources/fabric.mod.json';d=json.loads(p.read_text());d['depends'].update({'minecraft':'='+v,'fabric-api':'>='+matrix[v],'java':'>=17'});p.write_text(json.dumps(d,indent=2)+'\n')
p=r/'build.gradle';p.write_text(p.read_text().replace('options.release = 21','options.release = 17').replace('JavaVersion.VERSION_21','JavaVersion.VERSION_17'))
for p in (r/'src').rglob('*.java'):p.write_text(p.read_text().replace('Math.clamp','net.minecraft.util.Mth.clamp').replace('.getFirst()', '.get(0)'))
java=r/'src/main/java/dev/disc'
p=java/'ModItems.java';s=p.read_text();s=re.sub(r'            properties.attributes\(.*?\.build\(\)\);\n','',s,flags=re.S);p.write_text(s)
p=java/'ThrowingDiscMod.java';s=p.read_text().replace('ResourceLocation.fromNamespaceAndPath(ID, path)','new ResourceLocation(ID, path)').replace('ModItems.initialize();','ModItems.initialize();\n        DiscEnchantments.initialize();');p.write_text(s)
p=java/'ThrowingDiscItem.java';s=p.read_text().replace('DiscEnchantments.normalize(stack, level);','if(!level.isClientSide) DiscEnchantments.normalize(stack, level);').replace('SoundEvents.TRIDENT_THROW.value()','SoundEvents.TRIDENT_THROW');s=s.rsplit('}',1)[0]+'''    @Override public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute,net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot){
        if(slot!=net.minecraft.world.entity.EquipmentSlot.MAINHAND)return super.getDefaultAttributeModifiers(slot);
        return com.google.common.collect.ImmutableMultimap.of(
            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,new net.minecraft.world.entity.ai.attributes.AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Disc damage",tier.melee-1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION),
            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,new net.minecraft.world.entity.ai.attributes.AttributeModifier(BASE_ATTACK_SPEED_UUID,"Disc speed",-3,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
    }
}
''';p.write_text(s)
p=java/'ThrowingDiscEntity.java';s=p.read_text().replace('carried.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));','carried.hurtAndBreak(1,player,p -> p.broadcastBreakEvent(hand));').replace('defineSynchedData(SynchedEntityData.Builder builder)','defineSynchedData()').replace('builder.define(STACK, ItemStack.EMPTY)','entityData.define(STACK, ItemStack.EMPTY)').replace('@Override public ItemStack getWeaponItem()','public ItemStack getWeaponItem()').replace('int steps) {clientTarget','int steps,boolean teleport) {clientTarget').replace('player.getKnownMovement()', 'player.getDeltaMovement()').replace('.igniteForSeconds(', '.setSecondsOnFire(').replace('getItem().save(registryAccess())','getItem().save(new CompoundTag())').replace('ItemStack.parseOptional(registryAccess(),tag.getCompound("Disc"))','ItemStack.of(tag.getCompound("Disc"))')
a=s.index('    private int vanillaEnchantmentLevel(');b=s.index('    private boolean breakPot(',a);s=s[:a]+'''    private int vanillaEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantment enchantment){return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantment,getItem());}
    public int enchantmentLevel(String name){return vanillaEnchantmentLevel(DiscEnchantments.get(name));}
'''+s[b:]
s=s.replace('if (owner != null) fireball.deflect(net.minecraft.world.entity.projectile.ProjectileDeflection.REVERSE,\n                    this, owner, true);','if(owner!=null){fireball.setDeltaMovement(fireball.getDeltaMovement().scale(-1));fireball.xPower=-fireball.xPower;fireball.yPower=-fireball.yPower;fireball.zPower=-fireball.zPower;fireball.setOwner(owner);}')
p.write_text(s)
p=java/'DiscDispense.java';s=p.read_text().replace('net.minecraft.core.dispenser.BlockSource','net.minecraft.core.BlockSource').replace('source.blockEntity()','source.<net.minecraft.world.level.block.entity.DispenserBlockEntity>getEntity()').replace('source.level()','source.getLevel()').replace('source.pos()','source.getPos()').replace('source.state()','source.getBlockState()').replace('disc.getItem().hurtAndBreak(1,source.getLevel(),null,broken -> {});','if(disc.getItem().hurt(1,source.getLevel().random,null))disc.getItem().shrink(1);');p.write_text(s)
(java/'DiscEnchantments.java').write_text('''package dev.disc;
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
 public static void initialize(){String[] names={"homing","curve","range","keen_edge","harvest"};int[] levels={4,2,5,3,1};for(int i=0;i<names.length;i++)VALUES.put(names[i],Registry.register(BuiltInRegistries.ENCHANTMENT,ThrowingDiscMod.id(names[i]),new DiscEnchantment(names[i],levels[i])));}
 public static Enchantment get(String name){return VALUES.get(name);}
 public static boolean allowed(Enchantment e){return e instanceof DiscEnchantment||e==Enchantments.UNBREAKING||e==Enchantments.MENDING||e==Enchantments.FIRE_ASPECT||e==Enchantments.KNOCKBACK||e==Enchantments.VANISHING_CURSE;}
 public static void normalize(ItemStack stack,Level level){Map<Enchantment,Integer> map=new HashMap<>(EnchantmentHelper.getEnchantments(stack));boolean changed=false;
  if(map.getOrDefault(get("keen_edge"),0)>3){map.put(get("keen_edge"),3);changed=true;}
  if(map.containsKey(get("homing"))&&map.containsKey(get("curve"))){map.remove(get("curve"));changed=true;}
  if(map.remove(Enchantments.FLAMING_ARROWS)!=null){map.put(Enchantments.FIRE_ASPECT,Math.max(1,map.getOrDefault(Enchantments.FIRE_ASPECT,0)));changed=true;}
  if(changed)EnchantmentHelper.setEnchantments(map,stack);
 }
 private DiscEnchantments(){}
}
''')
p=java/'mixin/DiscAnvilMixin.java';s=p.read_text().replace('EnchantmentHelper.getEnchantmentsForCrafting(', 'EnchantmentHelper.getEnchantments(').replace('Enchantment.areCompatible(first,second)','first.isCompatibleWith(second)');# Replace dynamic-registry implementation with static allow-list.
p.write_text('''package dev.disc.mixin;
import dev.disc.*;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AnvilMenu.class) public abstract class DiscAnvilMixin {
 @Inject(method="createResult",at=@At("RETURN")) private void disc$restrict(CallbackInfo ci){
  var menu=(AnvilMenu)(Object)this;var disc=menu.getSlot(0).getItem();var book=menu.getSlot(1).getItem();if(!(disc.getItem() instanceof ThrowingDiscItem))return;
  var applied=EnchantmentHelper.getEnchantments(menu.getSlot(2).getItem()).keySet();
  for(var a:applied)for(var b:applied)if(a!=b&&!a.isCompatibleWith(b)){menu.getSlot(2).set(ItemStack.EMPTY);return;}
  if(book.is(Items.ENCHANTED_BOOK))for(var e:EnchantmentHelper.getEnchantments(book).keySet())if(!DiscEnchantments.allowed(e)){menu.getSlot(2).set(ItemStack.EMPTY);return;}
 }
}
''')
(java/'mixin/DiscEnchantmentMixin.java').write_text('''package dev.disc.mixin;
import dev.disc.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Enchantment.class) public abstract class DiscEnchantmentMixin {
 @Inject(method="canEnchant",at=@At("HEAD"),cancellable=true) private void disc$allow(ItemStack stack,CallbackInfoReturnable<Boolean> ci){if(stack.getItem() instanceof ThrowingDiscItem)ci.setReturnValue(DiscEnchantments.allowed((Enchantment)(Object)this));}
}
''')
(java/'mixin/DiscEnchantingTableMixin.java').write_text('''package dev.disc.mixin;
import dev.disc.*;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EnchantmentHelper.class) public abstract class DiscEnchantingTableMixin {
 @Inject(method="getAvailableEnchantmentResults",at=@At("RETURN")) private static void disc$filter(int power,ItemStack stack,boolean treasure,CallbackInfoReturnable<List<EnchantmentInstance>> ci){ci.getReturnValue().removeIf(e->(stack.getItem() instanceof ThrowingDiscItem&&!DiscEnchantments.allowed(e.enchantment))||(!(stack.getItem() instanceof ThrowingDiscItem)&&!stack.is(Items.BOOK)&&e.enchantment instanceof DiscEnchantments.DiscEnchantment));}
}
''')
p=r/'src/main/resources/throwing_disc.mixins.json';d=json.loads(p.read_text());d['compatibilityLevel']='JAVA_17';d['mixins']+=['DiscEnchantmentMixin','DiscEnchantingTableMixin'];p.write_text(json.dumps(d,indent=2)+'\n')
res=r/'src/main/resources'
for namespace in ['minecraft','throwing_disc']:
 for rel in ['enchantment','tags/enchantment']:
  p=res/'data'/namespace/rel
  if p.exists():shutil.rmtree(p)
 for rel,new in [('recipe','recipes'),('advancement','advancements'),('loot_table','loot_tables'),('tags/item','tags/items'),('tags/entity_type','tags/entity_types')]:
  p=res/'data'/namespace/rel
  if p.exists():p.rename(p.with_name(new.split('/')[-1]))
for name in ['knockback','fire_aspect']:(res/'data/throwing_disc/tags/items'/f'{name}_enchantable.json').write_text(json.dumps({'values':['#throwing_disc:discs']},indent=2)+'\n')
for p in (res/'data/throwing_disc/recipes').glob('*.json'):
 d=json.loads(p.read_text());result=d.get('result')
 if isinstance(result,dict)and'id'in result:result['item']=result.pop('id')
 p.write_text(json.dumps(d,indent=2)+'\n')
def fix(x):
 if isinstance(x,dict):
  for k,v in list(x.items()):
   if k=='items' and isinstance(v,str):x[k]=[v]
   else:fix(v)
 elif isinstance(x,list):
  for v in x:fix(v)
for p in (res/'data/throwing_disc/advancements').rglob('*.json'):
 d=json.loads(p.read_text());fix(d);p.write_text(json.dumps(d,indent=2)+'\n')
(r/'README.md').write_text('# Fabric backport for Minecraft 1.20.1\n\nBackport in progress.\n')
