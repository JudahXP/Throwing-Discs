from pathlib import Path
import json,zipfile,hashlib
repo=Path(__file__).resolve().parents[2];matrix=json.loads((repo/'tools/forge/matrix.json').read_text());out=repo/'downloads';reports=[]
for v in matrix:
 report=json.loads((out/'forge'/f'validation-{v}.json').read_text());assert not report.get('superseded',False),v;reports.append(report)
readme='''Judah's Throwing Discs 1.4.1 — Forge

Install only the JAR matching your Minecraft version, with Forge. Do not install the Fabric edition alongside it.
Includes all five disc tiers and the existing 1.4.1 behavior: smooth direct return, return-only phasing within eight blocks, unlimited bounce count, Harvest, reusable dispenser throws, and no Ricochet enchantment.

Minecraft 1.20.5 and 1.21.2 are omitted because Forge does not publish releases for them.
Validation was deliberately limited to compilation and JAR inspection. No game-test suites or gameplay tests were run for these Forge ports.
'''
checks=[]
for source in [False,True]:
 name='judahs-throwing-discs-1.4.1-forge-all-versions'+('-sources' if source else '')+'.zip';p=out/name
 with zipfile.ZipFile(p,'w',zipfile.ZIP_DEFLATED) as z:
  z.writestr('README.txt',readme);z.writestr('validation.json',json.dumps(reports,indent=2))
  for v in matrix:
   f=out/'forge'/f'judahs-throwing-discs-1.4.1-forge-mc{v}{"-source.zip" if source else ".jar"}';z.write(f,f.name)
 assert p.stat().st_size<100_000_000
 checks.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+name)
(out/'judahs-throwing-discs-1.4.1-forge-SHA256SUMS.txt').write_text('\n'.join(checks)+'\n')
(repo/'tools/forge/STATUS.md').write_text(readme+'\n'+ '\n'.join(f"- Minecraft {v}: Forge {c['forge'].split('-')[1]}, Java {c['java']}; compile and JAR checks passed." for v,c in matrix.items())+'\n')
p=repo/'README.md';s=p.read_text();s=s.split('\n## Forge ports\n')[0];s+='\n## Forge ports\n\n'+readme.split('\n\n',1)[1]+'\n[Download Forge mods](downloads/judahs-throwing-discs-1.4.1-forge-all-versions.zip) · [Download Forge source](downloads/judahs-throwing-discs-1.4.1-forge-all-versions-sources.zip)\n';p.write_text(s)
print('Bundled',len(matrix),'Forge versions')
