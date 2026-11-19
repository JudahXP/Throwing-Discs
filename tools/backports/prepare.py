from pathlib import Path
import shutil,json,re
root=Path(__file__).resolve().parents[2];base=root/'versions';source=base/'judahs-throwing-discs-1.21.11'
matrix={'1.20':'0.83.0+1.20','1.20.1':'0.92.12+1.20.1','1.20.2':'0.91.6+1.20.2','1.20.3':'0.91.1+1.20.3','1.20.4':'0.97.3+1.20.4','1.20.5':'0.97.8+1.20.5','1.20.6':'0.100.8+1.20.6','1.21':'0.102.0+1.21','1.21.1':'0.116.18+1.21.1','1.21.2':'0.106.1+1.21.2','1.21.3':'0.114.1+1.21.3','1.21.4':'0.119.4+1.21.4','1.21.5':'0.128.2+1.21.5','1.21.6':'0.128.2+1.21.6','1.21.7':'0.129.0+1.21.7','1.21.8':'0.136.1+1.21.8','1.21.9':'0.134.1+1.21.9','1.21.10':'0.138.4+1.21.10'}
(root/'tools/backports/matrix.json').write_text(json.dumps(matrix,indent=2)+'\n')
def clean(x):
 if isinstance(x,list):return [clean(v) for v in x if not(isinstance(v,str) and 'copper_throwing_disc' in v)]
 if isinstance(x,dict):return {k:clean(v) for k,v in x.items() if 'copper_throwing_disc' not in k}
 return x
for v in ['1.21.10','1.21.9','1.21.8','1.21.7','1.21.6','1.21.5']:
 r=base/('judahs-throwing-discs-'+v)
 if r.exists():continue
 shutil.copytree(source,r,ignore=shutil.ignore_patterns('build','run','.gradle'))
 p=r/'gradle.properties';p.write_text(p.read_text().replace('1.21.11',v).replace('0.141.1+'+v,matrix[v]))
 p=r/'src/main/resources/fabric.mod.json';d=json.loads(p.read_text());d['depends']['minecraft']='='+v;d['depends']['fabric-api']='>='+matrix[v];p.write_text(json.dumps(d,indent=2)+'\n')
 for p in (r/'src').rglob('*.java'):
  s=p.read_text().replace('Identifier','ResourceLocation').replace('needsSync','hasImpulse').replace('projectile.hurtingprojectile.LargeFireball','projectile.LargeFireball').replace('SoundEvents.SPEAR_HIT.value()','SoundEvents.TRIDENT_HIT')
  if v not in ['1.21.9','1.21.10']:
   s=s.replace('Items.COPPER_INGOT,','').replace('Items.COPPER_SHOVEL,','').replace('{95,125,16,780,1015}','{125,16,780,1015}')
   if p.name=='TextureTest.java':s=s.replace('"copper",','')
   s=re.sub(r'COPPER\("copper"[^\n]*?\), ', '',s)
   s=s.replace('net.minecraft.world.entity.EntityReference.of(owner)','owner')
   s=re.sub(r'new net.minecraft.server.level.TicketType\(20,.*?FLAG_KEEP_DIMENSION_ACTIVE\)', 'new net.minecraft.server.level.TicketType(20, false, net.minecraft.server.level.TicketType.TicketUse.LOADING_AND_SIMULATION)',s,flags=re.S)
   s=s.replace('EntityRenderers.register','net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register')
   s=s.replace('carried.hurtAndBreak(1, player, hand);','carried.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));')
   if p.name=='ThrowingDiscEntityRenderer.java':
    s=s.replace('import net.minecraft.client.renderer.SubmitNodeCollector;','import net.minecraft.client.renderer.MultiBufferSource;')
    s=re.sub(r'import [^;]*CameraRenderState;','',s).replace('submit(DiscRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)','render(DiscRenderState state, PoseStack poses, MultiBufferSource buffers, int light)').replace('state.item.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);','state.item.render(poses, buffers, light, OverlayTexture.NO_OVERLAY);').replace('super.submit(state, poses, collector, camera);','super.render(state, poses, buffers, light);')
  if p.name=='DiscGameTests.java':
   s=s.replace('h.assertTrue(','check(h,').replace('h.fail(','fail(h,')
   s=s.replace('public class DiscGameTests {','public class DiscGameTests {\n    private static void check(GameTestHelper h,boolean value,String message){h.assertTrue(value,Component.literal(message));}\n    private static void fail(GameTestHelper h,String message){h.fail(Component.literal(message));}')
   if v not in ['1.21.9','1.21.10']:s=s.replace('private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {','private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {\n        h.getLevel().getServer().setPvpAllowed(true);')
  p.write_text(s)
 if v not in ['1.21.9','1.21.10']:
  for p in (r/'src/main/resources').rglob('*copper*'):
   if p.is_file():p.unlink()
  for p in (r/'src/main/resources').rglob('*.json'):p.write_text(json.dumps(clean(json.loads(p.read_text())),indent=2)+'\n')
 if v=='1.21.5':
  p=r/'src/main/java/dev/disc/ThrowingDiscEntity.java';s=p.read_text().replace('import net.minecraft.world.level.storage.ValueInput;','import net.minecraft.nbt.CompoundTag;').replace('import net.minecraft.world.level.storage.ValueOutput;','').replace('ValueOutput output','CompoundTag output').replace('ValueInput input','CompoundTag input').replace('output.store("Disc", ItemStack.CODEC, getItem())','output.store("Disc", ItemStack.CODEC, registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), getItem())').replace('input.read("Disc", ItemStack.CODEC)','input.read("Disc", ItemStack.CODEC, registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE))');p.write_text(s)
  p=r/'src/gametest/java/dev/disc/DiscGameTests.java';s=p.read_text();s=re.sub(r'net.minecraft.world.level.storage.TagValueOutput.createWithContext\((?:problems|reporter),h.getLevel\(\).registryAccess\(\)\)','new net.minecraft.nbt.CompoundTag()',s);s=re.sub(r'net.minecraft.world.level.storage.TagValueInput.create\(\s*(?:problems|reporter),h.getLevel\(\).registryAccess\(\),(\w+).buildResult\(\)\)',r'\1',s);s=re.sub(r'\s*var (?:problems|reporter)\s*=\s*new net.minecraft.util.ProblemReporter.Collector\(\);','',s);s=re.sub(r'\s*check\(h,(?:problems|reporter).isEmpty\(\),[^;]*;','',s);p.write_text(s)
 (r/'README.md').write_text(f'# Fabric backport for Minecraft {v}\n\nBackport in progress; see tools/backports/STATUS.md.\n')
 (r/'VALIDATION.md').write_text('Runtime verification pending after workspace recovery.\n')
 print(v)
