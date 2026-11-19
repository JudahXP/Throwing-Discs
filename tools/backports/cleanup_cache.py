from pathlib import Path
import sys,shutil,json,hashlib
repo=Path(__file__).resolve().parents[2];cache=Path('/workspace/work/gradle-cache/caches/fabric-loom')
for v in sys.argv[1:]:
 manifest=json.loads((repo/'downloads/backports'/f'validation-{v}.json').read_text());jar=repo/'downloads/backports'/f'judahs-throwing-discs-1.4.1-mc{v}.jar'
 assert hashlib.sha256(jar.read_bytes()).hexdigest()==manifest['jar_sha256']
 paths=[cache/v,repo/'versions'/('judahs-throwing-discs-'+v)/'.gradle']+list((cache/'minecraftMaven').glob('*/*/*/'+v+'-*'))
 for p in paths:
  if p.exists():shutil.rmtree(p)
 print(v,'rebuildable cache removed; source, artifacts and reports retained')
