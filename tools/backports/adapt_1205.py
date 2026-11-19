from pathlib import Path
import json
repo=Path(__file__).resolve().parents[2]
for v in ['1.20.5','1.20.6']:
 r=repo/'versions'/('judahs-throwing-discs-'+v);java=r/'src/main/java/dev/disc'
 p=r/'build.gradle';p.write_text(p.read_text().replace('options.release = 17','options.release = 21').replace('JavaVersion.VERSION_17','JavaVersion.VERSION_21'))
 p=r/'src/main/resources/fabric.mod.json';d=json.loads(p.read_text());d['depends']['java']='>=21';p.write_text(json.dumps(d,indent=2)+'\n')
 p=r/'src/main/resources/throwing_disc.mixins.json';p.write_text(p.read_text().replace('JAVA_17','JAVA_21'))
 p=java/'ThrowingDiscEntity.java';s=p.read_text().replace('defineSynchedData()','defineSynchedData(SynchedEntityData.Builder builder)').replace('entityData.define(STACK, ItemStack.EMPTY)','builder.define(STACK, ItemStack.EMPTY)').replace('carried.hurtAndBreak(1,player,p -> p.broadcastBreakEvent(hand));','carried.hurtAndBreak(1,player,net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));').replace('getItem().save(new CompoundTag())','getItem().save(registryAccess())').replace('tag.put("Disc",getItem().save(registryAccess()));','if(!getItem().isEmpty())tag.put("Disc",getItem().save(registryAccess()));').replace('ItemStack.of(tag.getCompound("Disc"))','ItemStack.parseOptional(registryAccess(),tag.getCompound("Disc"))').replace('.setSecondsOnFire(','.igniteForSeconds(');p.write_text(s)
 p=java/'DiscDispense.java';s=p.read_text().replace('if(disc.getItem().hurt(1,source.level().random,null))disc.getItem().shrink(1);','disc.getItem().hurtAndBreak(1,source.level().random,null,()->{});');p.write_text(s)
 p=java/'ThrowingDiscItem.java';s=p.read_text();s=s[:s.index('    @Override public com.google.common.collect.Multimap')]+ '}\n';p.write_text(s)
 p=java/'ModItems.java';s=p.read_text().replace('            if (tier == DiscTier.NETHERITE)', '''            properties.attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_DAMAGE_UUID,"Disc damage",tier.melee-1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_SPEED_UUID,"Disc speed",-3,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build());
            if (tier == DiscTier.NETHERITE)''');p.write_text(s)
 p=java/'DiscEnchantments.java';s=p.read_text().replace('super(Rarity.UNCOMMON,EnchantmentCategory.BREAKABLE,new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND})','super(Enchantment.definition(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,ThrowingDiscMod.id("discs")),5,maximum,Enchantment.dynamicCost(8,8),Enchantment.dynamicCost(28,8),2,EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND))').replace('net.minecraft.world.entity.MobType type','net.minecraft.world.entity.EntityType<?> type');s=s[:s.index(' public static void normalize')]+''' public static void normalize(ItemStack stack,Level level){
  var map=new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
  map.set(get("keen_edge"),Math.min(3,map.getLevel(get("keen_edge"))));
  if(map.getLevel(get("homing"))>0)map.set(get("curve"),0);
  if(map.getLevel(Enchantments.FLAME)>0){map.set(Enchantments.FLAME,0);map.set(Enchantments.FIRE_ASPECT,Math.max(1,map.getLevel(Enchantments.FIRE_ASPECT)));}
  EnchantmentHelper.setEnchantments(stack,map.toImmutable());
 }
 private DiscEnchantments(){}
}
''';p.write_text(s)
 p=java/'mixin/DiscAnvilMixin.java';p.write_text(p.read_text().replace('EnchantmentHelper.getEnchantments(', 'EnchantmentHelper.getEnchantmentsForCrafting('))
 p=java/'mixin/DiscEnchantingTableMixin.java';p.write_text(p.read_text().replace('disc$filter(int power','disc$filter(net.minecraft.world.flag.FeatureFlagSet flags,int power'))
 p=r/'src/gametest/java/dev/disc/DiscGameTests.java';s=p.read_text().replace('d.getItem().setHoverName(net.minecraft.network.chat.Component.literal("Keep me"))','d.getItem().set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Keep me"))');s=s.replace('"Reference tuning preserved");','"Reference tuning preserved");h.assertTrue(stack.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().anyMatch(e->e.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)&&Math.abs(e.modifier().amount()-(t.melee-1))<0.001),"Melee attribute installed");');p.write_text(s)
 for p in (r/'src/main/resources/data/throwing_disc/recipes').glob('*.json'):
  d=json.loads(p.read_text());result=d.get('result')
  if isinstance(result,dict) and 'item' in result:result['id']=result.pop('item')
  p.write_text(json.dumps(d,indent=2)+'\n')
