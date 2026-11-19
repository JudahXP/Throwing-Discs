from pathlib import Path
import shutil,json,re
root=Path(__file__).resolve().parents[2];base=root/'versions';matrix=json.loads((root/'tools/backports/matrix.json').read_text())
for v in ['1.21','1.21.1']:
 r=base/('judahs-throwing-discs-'+v)
 if r.exists():continue
 shutil.copytree(base/'judahs-throwing-discs-1.21.4',r,ignore=shutil.ignore_patterns('build','run','.gradle'))
 p=r/'gradle.properties';s=p.read_text();s=re.sub(r'^minecraft_version=.*','minecraft_version='+v,s,flags=re.M);s=re.sub(r'^mod_version=.*','mod_version=1.4.1+'+v,s,flags=re.M);s=re.sub(r'^fabric_api_version=.*','fabric_api_version='+matrix[v],s,flags=re.M);p.write_text(s)
 p=r/'src/main/resources/fabric.mod.json';d=json.loads(p.read_text());d['depends']['minecraft']='='+v;d['depends']['fabric-api']='>='+matrix[v];p.write_text(json.dumps(d,indent=2)+'\n')
 java=r/'src/main/java/dev/disc'
 p=java/'DiscTier.java';p.write_text(p.read_text().replace('ToolMaterial','Tiers'))
 p=java/'ModItems.java';s=p.read_text().replace('.setId(key)','').replace('tier.material.durability()','tier.material.getUses()');s=re.sub(r'\s*\.enchantable\(tier.material.enchantmentValue\(\)\).repairable\(tier.material.repairItems\(\)\)','',s);p.write_text(s)
 p=java/'ThrowingDiscItem.java';s=p.read_text().replace('public InteractionResult use(','public net.minecraft.world.InteractionResultHolder<ItemStack> use(').replace('isOnCooldown(stack)','isOnCooldown(this)').replace('addCooldown(stack,','addCooldown(this,')
 for a,b in [('PASS','pass'),('FAIL','fail'),('SUCCESS','success')]:s=s.replace('InteractionResult.'+a,'net.minecraft.world.InteractionResultHolder.'+b+'(player.getItemInHand(hand))')
 s=s.rsplit('}',1)[0]+'''    @Override public int getEnchantmentValue(){return tier.material.getEnchantmentValue();}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack ingredient){return tier.material.getRepairIngredient().test(ingredient);}
}
''';p.write_text(s)
 p=java/'ModEntities.java';p.write_text(p.read_text().replace('.build(ResourceKey.create(Registries.ENTITY_TYPE, ThrowingDiscMod.id("throwing_disc")))','.build("throwing_disc:throwing_disc")'))
 p=java/'ThrowingDiscEntity.java';s=p.read_text().replace('living.hurtServer(server,','living.hurt(').replace('spawnAtLocation(server, stack)','spawnAtLocation(stack)').replace('living.getItemBlockingWith()','(living.isBlocking()?living.getUseItem():null)').replace('addCooldown(blocking,60)','addCooldown(blocking.getItem(),60)').replace('.getUnitVec3i()', '.getNormal()').replace('public void onRemoval(RemovalReason reason)','public void remove(RemovalReason reason)').replace('super.onRemoval(reason)','super.remove(reason)');p.write_text(s)
 (r/'src/client/java/dev/disc/client/ThrowingDiscEntityRenderer.java').write_text((root/'tools/backports/legacy_renderer.java').read_text())
 p=r/'src/gametest/java/dev/disc/DiscGameTests.java';p.write_text(p.read_text().replace('isOnCooldown(victim.getOffhandItem())','isOnCooldown(victim.getOffhandItem().getItem())'))
 def ingredient(x):return ({'tag':x[1:]} if x.startswith('#') else {'item':x}) if isinstance(x,str) else x
 for p in (r/'src/main/resources/data/throwing_disc/recipe').glob('*.json'):
  d=json.loads(p.read_text())
  if 'key' in d:d['key']={k:ingredient(x) for k,x in d['key'].items()}
  for k in ['base','addition','template','ingredient']:
   if k in d:d[k]=ingredient(d[k])
  p.write_text(json.dumps(d,indent=2)+'\n')
 (r/'README.md').write_text(f'# Fabric backport for Minecraft {v}\n\nBackport in progress.\n')
