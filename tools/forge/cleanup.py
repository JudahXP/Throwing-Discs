from pathlib import Path
import sys,json,hashlib,shutil
repo=Path(__file__).resolve().parents[2];cache=Path('/workspace/work/forge-gradle/caches/minecraftforge/forgegradle/mavenizer/caches')
for v in sys.argv[1:]:
 report=json.loads((repo/'downloads/forge'/f'validation-{v}.json').read_text());jar=repo/'downloads/forge'/f'judahs-throwing-discs-1.4.1-forge-mc{v}.jar'
 assert hashlib.sha256(jar.read_bytes()).hexdigest()==report['sha256']
 paths=[cache/'minecraft_tasks'/v,repo/'forge'/('judahs-throwing-discs-'+v)/'.gradle']
 for parent in ['mcp/de/oceanlabs/mcp/mcp_config','forge/.global/mcp','forge/net/minecraftforge/forge']:
  paths+=list((cache/parent).glob(v+'-*'))
 for p in paths:
  if p.is_dir():shutil.rmtree(p)
 print(v,'temporary build caches cleared',flush=True)
