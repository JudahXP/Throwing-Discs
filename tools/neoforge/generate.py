from pathlib import Path
import json,shutil,re,xml.etree.ElementTree as E
repo=Path(__file__).resolve().parents[2];versions=[x.text for x in E.parse('/workspace/work/neoforge-metadata.xml').findall('.//version')];matrix={}
for src in sorted((repo/'forge').iterdir()):
 v=src.name.removeprefix('judahs-throwing-discs-')
 if v in ['1.20','1.20.1']:continue
 nums=list(map(int,v.split('.')));prefix=(f'{nums[1]}.{nums[2] if len(nums)>2 else 0}.' if nums[0]==1 else v+('.0.' if len(nums)==2 else '.'))
 matches=[x for x in versions if x.startswith(prefix)]
 if not matches:continue
 neo=matches[-1];java=25 if nums[0]==26 else 17 if nums<( [1,20,5]) else 21
 matrix[v]={'neoforge':neo,'java':java};dst=repo/'neoforge'/src.name
 shutil.copytree(src,dst,ignore=shutil.ignore_patterns('.gradle','build','run','logs'),dirs_exist_ok=True)
 for p in (dst/'src').rglob('*.java'):
  s=p.read_text().replace('net.minecraftforge.registries','net.neoforged.neoforge.registries').replace('net.minecraftforge.event.','net.neoforged.neoforge.event.').replace('net.minecraftforge.client.','net.neoforged.neoforge.client.').replace('net.minecraftforge.fml.','net.neoforged.fml.').replace('net.minecraftforge.api.','net.neoforged.api.').replace('net.minecraftforge.eventbus.api.listener.','net.neoforged.bus.api.').replace('net.minecraftforge.eventbus.api.','net.neoforged.bus.api.').replace('net.minecraftforge.common.MinecraftForge','net.neoforged.neoforge.common.NeoForge')
  if p.name=='ThrowingDiscMod.java':
   start=s.index('    public ThrowingDiscMod(');end=s.index('    private static void register(',start)
   s=s[:start]+'''    public ThrowingDiscMod(net.neoforged.bus.api.IEventBus bus) {
        bus.addListener(ThrowingDiscMod::register);
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(DiscDispense::initialize));
        bus.addListener(ThrowingDiscMod::creative);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ThrowingDiscMod::logout);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent e) -> e.getServer().getPlayerList().getPlayers().forEach(DiscFlights::recover));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e) -> DiscFlights.clear());
    }
'''+s[end:]
  if p.name=='ThrowingDiscClient.java':
   s=re.sub(r'@net.neoforged.fml.common.Mod.EventBusSubscriber\([^\n]+', '@net.neoforged.fml.common.EventBusSubscriber(modid = dev.disc.ThrowingDiscMod.ID, value = net.neoforged.api.distmarker.Dist.CLIENT)',s)
  p.write_text(s)
 meta=dst/'src/main/resources/META-INF/mods.toml';s=meta.read_text();s=re.sub(r'loaderVersion="[^"]+"','loaderVersion="[1,)"',s);s=s.replace('modId="forge"','modId="neoforge"').replace('mandatory=true','type="required"');s=re.sub(r'versionRange="\[[0-9.]+,\)"',f'versionRange="[{neo},)"',s)
 target='mods.toml' if nums<[1,20,5] else 'neoforge.mods.toml';meta.unlink();(meta.parent/target).write_text(s+'\n[[mixins]]\nconfig="throwing_disc.mixins.json"\n')
 (dst/'build.gradle').write_text(f'''plugins {{ id 'java-library'; id 'net.neoforged.moddev' version '2.0.148' }}
version = '1.4.1+{v}'
group = 'dev.disc'
base.archivesName = 'judahs-throwing-discs-neoforge-mc{v}'
java {{ toolchain.languageVersion = JavaLanguageVersion.of({25 if java==25 else 21}); withSourcesJar() }}
sourceSets.main.java.srcDir 'src/client/java'
neoForge {{ version = '{neo}'; mods {{ throwing_disc {{ sourceSet sourceSets.main }} }} }}
tasks.withType(JavaCompile).configureEach {{ options.release = {java}; options.encoding = 'UTF-8' }}
jar {{ from('LICENSE') }}
''')
 (dst/'settings.gradle').write_text("pluginManagement { repositories { gradlePluginPortal(); maven { url = 'https://maven.neoforged.net/releases' } } }\nrootProject.name = 'judahs-throwing-discs-neoforge'\n")
 (dst/'README.md').write_text(f"# Judah's Throwing Discs — NeoForge {v}\n\nPort in progress. Requires NeoForge {neo}. Validation pending.\n")
(repo/'tools/neoforge/matrix.json').write_text(json.dumps(matrix,indent=2)+'\n');print('Prepared',len(matrix),'projects')
