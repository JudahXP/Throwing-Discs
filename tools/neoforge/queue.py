from pathlib import Path
import json,subprocess,sys,shutil
repo=Path(__file__).resolve().parents[2]
for v in sorted(json.loads((repo/'tools/neoforge/matrix.json').read_text()),key=lambda v:tuple(map(int,v.split('.'))),reverse=True):
 if (repo/'downloads/neoforge'/f'validation-{v}.json').exists():continue
 if shutil.disk_usage(repo).free<700_000_000:raise SystemExit('Disk space too low; stopped before next build')
 subprocess.run([sys.executable,'tools/neoforge/build.py',v],cwd=repo,check=True)
 r=subprocess.run([sys.executable,'tools/neoforge/package.py',v],cwd=repo)
 print(v,'ready' if r.returncode==0 else 'needs fix',flush=True)
 subprocess.run([sys.executable,'tools/neoforge/cleanup.py'],cwd=repo,check=True)

