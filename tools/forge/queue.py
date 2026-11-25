"""Compile and package sequentially; no test tasks. Resume skips verified artifacts."""
from pathlib import Path
import json,subprocess,sys,shutil
repo=Path(__file__).resolve().parents[2]
for v in sorted(json.loads((repo/'tools/forge/matrix.json').read_text()),key=lambda v:tuple(map(int,v.split('.')))):
 if (repo/'downloads/forge'/f'validation-{v}.json').exists():continue
 if shutil.disk_usage(repo).free<900_000_000:raise SystemExit('Stopped: insufficient free disk space')
 subprocess.run([sys.executable,'tools/forge/build.py',v],cwd=repo,check=True)
 p=subprocess.run([sys.executable,'tools/forge/package.py',v],cwd=repo)
 if p.returncode:print(v,'needs a fix; continuing other versions',flush=True)
 else:print(v,'compile and JAR checks passed',flush=True)
