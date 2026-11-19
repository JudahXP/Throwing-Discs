from pathlib import Path
import json,re
repo=Path(__file__).resolve().parents[2]
for v in json.loads((repo/'tools/backports/matrix.json').read_text()):
 p=Path('/workspace/work')/f'backport-{v}.log'
 if (repo/'downloads/backports'/f'validation-{v}.json').exists():state='PACKAGED'
 elif not p.exists():state='not run'
 else:
  s=p.read_text();state='passed' if 'BUILD SUCCESSFUL'in s else 'FAILED'if 'BUILD FAILED'in s else 'running'
  m=re.search(r'All (\d+) required tests passed',s)
  if m:state+='; '+m[1]+' game tests passed'
 print(v,state)
