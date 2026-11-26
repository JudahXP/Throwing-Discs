from pathlib import Path
import json,hashlib,shutil
repo=Path(__file__).resolve().parents[2]
for p in (repo/'downloads/neoforge').glob('validation-*.json'):
 d=json.loads(p.read_text());v=d['minecraft'];jar=repo/'downloads/neoforge'/f'judahs-throwing-discs-1.4.1-neoforge-mc{v}.jar';assert hashlib.sha256(jar.read_bytes()).hexdigest()==d['sha256']
 # Generated Minecraft sources and binaries, not the mod output.
 r=repo/'neoforge'/('judahs-throwing-discs-'+v)/'build'
 for name in ['moddev','tmp','neoForm']:
  shutil.rmtree(r/name,ignore_errors=True)
shutil.rmtree('/workspace/work/neoforge-gradle/caches/neoformruntime/intermediate_results',ignore_errors=True)
print('Rebuildable NeoForm intermediate cache cleared',flush=True)
