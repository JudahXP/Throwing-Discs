from pathlib import Path
import json,sys,zipfile,hashlib,shutil,struct
repo=Path(__file__).resolve().parents[2];matrix=json.loads((repo/'tools/forge/matrix.json').read_text());out=repo/'downloads/forge';out.mkdir(exist_ok=True)
for v in sys.argv[1:]:
 root=repo/'forge'/('judahs-throwing-discs-'+v);c=matrix[v];log=Path('/workspace/work')/f'forge-{v}.log';assert 'BUILD SUCCESSFUL' in log.read_text(),v
 jars=[p for p in (root/'build/libs').glob('*.jar') if ('-srg.jar' in p.name if c['java']==17 else not any(x in p.name for x in ['-sources','-srg','-dev']))];assert len(jars)==1,(v,jars)
 jar=jars[0]
 with zipfile.ZipFile(jar) as z:
  pack=json.loads(z.read('pack.mcmeta'));assert 'pack' in pack
  names=z.namelist();assert 'META-INF/mods.toml' in names and 'fabric.mod.json' not in names
  assert not any('ricochet' in n.lower() or 'DiscGameTests' in n for n in names)
  for tier in ['copper','iron','gold','diamond','netherite']:assert f'assets/throwing_disc/textures/item/{tier}_throwing_disc.png' in names
  assert struct.unpack('>H',z.read('dev/disc/ThrowingDiscMod.class')[6:8])[0]==44+c['java']
  if c['java']==17:assert 'throwing_disc.refmap.json' in names and json.loads(z.read('throwing_disc.refmap.json'))['mappings']
 name=f'judahs-throwing-discs-1.4.1-forge-mc{v}';shutil.copy2(jar,out/(name+'.jar'))
 with zipfile.ZipFile(out/(name+'-source.zip'),'w',zipfile.ZIP_DEFLATED) as z:
  for p in sorted(root.rglob('*')):
   rel=p.relative_to(root)
   if p.is_file() and not any(s in {'.gradle','build','run','logs','crash-reports'} for s in rel.parts):z.write(p,Path(root.name)/rel)
 (out/f'validation-{v}.json').write_text(json.dumps({'minecraft':v,'forge':c['forge'],'java':c['java'],'compilation':'passed','jar_inspection':'passed','gameplay_tested':False,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()},indent=2)+'\n')
 print(v,'packaged',flush=True)
