from pathlib import Path
import sys,json,zipfile,shutil,re,struct,xml.etree.ElementTree as ET,hashlib
repo=Path(__file__).resolve().parents[2];out=repo/'downloads/backports';out.mkdir(exist_ok=True);logs=Path('/workspace/work')
for v in sys.argv[1:]:
 r=repo/'versions'/('judahs-throwing-discs-'+v);log=(logs/f'backport-{v}.log').read_text();assert 'BUILD SUCCESSFUL' in log,v
 m=re.search(r'All (\d+) required tests passed',log);assert m,v
 suites=[ET.parse(p).getroot() for p in (r/'build/test-results/test').glob('TEST-*.xml')];count=sum(int(s.get('tests')) for s in suites);assert count==11 and all(s.get('failures')=='0' and s.get('errors')=='0' for s in suites)
 jars=[p for p in (r/'build/libs').glob('*.jar') if '-sources' not in p.name and '-dev' not in p.name];assert len(jars)==1
 copper=tuple(map(int,v.split('.')))>=(1,21,9);java=17 if tuple(map(int,v.split('.')))<(1,20,5)else 21
 with zipfile.ZipFile(jars[0]) as z:
  d=json.loads(z.read('fabric.mod.json'));assert d['version']=='1.4.1+'+v and d['depends']['minecraft']=='='+v
  assert not any('DiscGameTests' in n or 'DiscPhysicsTest' in n or 'ricochet.json' in n for n in z.namelist())
  assert any('copper_throwing_disc' in n for n in z.namelist())==copper
  assert struct.unpack('>H',z.read('dev/disc/DiscPhysics.class')[6:8])[0]==44+java
  for tier in (['copper']if copper else[])+['iron','gold','diamond','netherite']:
   n=f'assets/throwing_disc/textures/item/{tier}_throwing_disc.png';assert z.read(n)==(repo/'versions/judahs-throwing-discs-1.21.11/src/main/resources'/n).read_bytes()
 (r/'README.md').write_text(f'''# Judah’s Throwing Discs 1.4.1 — Minecraft {v} (Fabric)

Install only the matching version JAR on client and server. Requires Fabric Loader 0.19.5+, matching Fabric API, and Java {java}+.

{'Includes copper, iron, golden, diamond and netherite discs.' if copper else 'Includes iron, golden, diamond and netherite discs; copper is omitted.'}

Preserves 1.4.1 smooth direct return, unlimited bounce count, no total piercing cap, Harvest I, reusable dispenser throws, and complete Ricochet removal. Phasing is allowed only on return and only when the whole movement step remains within eight blocks of the destination. Each target can be hit once per outbound/return pass. Uses the available trident impact sound.

Throw / melee / base range: iron 6 / 4 / 16; gold 5 / 3 / 18; diamond 7.5 / 5 / 19; netherite 8.5 / 6 / 21. {'Copper 5 / 3 / 14.' if copper else ''}

Harvest breaks mature crops, melons/pumpkins and upper sugar cane/bamboo, with vanilla drops and no automatic replanting. Young crops and the bottom cane/bamboo block remain. Dispensers recover the actual disc in an empty slot or drop it when missing/full. Throws use durability.

Build with JDK 25 for Gradle/Loom and a Java 21 compiler toolchain: `./gradlew build runGameTest`. Output targets Java {java}. See VALIDATION.md.
''')
 (r/'VALIDATION.md').write_text(f'''# Validation — Minecraft {v}

Build succeeded; {count} unit tests and {m[1]} required dedicated-server game tests passed. Coverage includes return/collision behavior, repeated bounces, enchantments, persistence, Harvest, crafting and dispenser reuse. JAR metadata, Java {java} class target, copper inclusion, original texture bytes and absence of test classes/Ricochet definitions were checked.

Headless automated checks only. Graphical playtesting and multiplayer latency testing have not been performed.
''')
 name=f'judahs-throwing-discs-1.4.1-mc{v}'
 shutil.copy2(jars[0],out/(name+'.jar'))
 with zipfile.ZipFile(out/(name+'-source.zip'),'w',zipfile.ZIP_DEFLATED)as z:
  for p in sorted(r.rglob('*')):
   rel=p.relative_to(r)
   if p.is_file()and not any(x in {'.git','.gradle','build','run'}for x in rel.parts):z.write(p,Path(r.name)/rel)
 (out/f'validation-{v}.json').write_text(json.dumps({'minecraft':v,'unit_tests':count,'game_tests':int(m[1]),'java_target':java,'copper':copper,'jar_sha256':hashlib.sha256(jars[0].read_bytes()).hexdigest()},indent=2)+'\n')
 print(v,'packaged')
