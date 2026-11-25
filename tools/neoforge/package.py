from pathlib import Path
import json,zipfile,hashlib,shutil,sys,tomllib,struct
repo=Path(__file__).resolve().parents[2];matrix=json.loads((repo/'tools/neoforge/matrix.json').read_text());out=repo/'downloads/neoforge';out.mkdir(exist_ok=True)
for v in sys.argv[1:]:
 r=repo/'neoforge'/('judahs-throwing-discs-'+v);assert 'BUILD SUCCESSFUL' in (Path('/workspace/work')/f'neoforge-{v}.log').read_text()
 jars=[p for p in (r/'build/libs').glob('*.jar') if 'sources' not in p.name];assert len(jars)==1;jar=jars[0]
 with zipfile.ZipFile(jar) as z:
  meta='META-INF/neoforge.mods.toml' if 'META-INF/neoforge.mods.toml' in z.namelist() else 'META-INF/mods.toml';d=tomllib.loads(z.read(meta).decode());assert z.read(d['mods'][0]['logoFile']).startswith(b'\x89PNG');assert 'pack' in json.loads(z.read('pack.mcmeta'));assert len(z.namelist())==len(set(z.namelist()))
  assert json.loads(z.read('throwing_disc.mixins.json'))['compatibilityLevel']!='JAVA_25'
  for tier in ['copper','iron','gold','diamond','netherite']:assert f'assets/throwing_disc/textures/item/{tier}_throwing_disc.png' in z.namelist()
  assert not any('ricochet' in n.lower() for n in z.namelist())
 name=f'judahs-throwing-discs-1.4.1-neoforge-mc{v}';shutil.copy2(jar,out/(name+'.jar'))
 (r/'README.md').write_text(f"# Judah's Throwing Discs — NeoForge {v}\n\nRequires NeoForge {matrix[v]['neoforge']}. Compilation and JAR inspection passed; gameplay not tested. Install only the matching Minecraft version. Includes copper-first creative ordering and mod icon.\n")
 with zipfile.ZipFile(out/(name+'-source.zip'),'w',zipfile.ZIP_DEFLATED) as z:
  for p in r.rglob('*'):
   rel=p.relative_to(r)
   if p.is_file() and not any(x in {'.gradle','build','run','logs','crash-reports'} for x in rel.parts):z.write(p,Path(r.name)/rel)
 (out/f'validation-{v}.json').write_text(json.dumps({'minecraft':v,**matrix[v],'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'compilation':'passed','jar_inspection':'passed','gameplay_tested':False},indent=2)+'\n');print(v,'packaged',flush=True)
