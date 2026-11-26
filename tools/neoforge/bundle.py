from pathlib import Path
import json,zipfile,hashlib
repo=Path(__file__).resolve().parents[2];out=repo/'downloads';matrix=json.loads((repo/'tools/neoforge/matrix.json').read_text());assert len(matrix)==23
reports=[]
for v in matrix:
 d=json.loads((out/'neoforge'/f'validation-{v}.json').read_text());p=out/'neoforge'/f'judahs-throwing-discs-1.4.1-neoforge-mc{v}.jar';assert hashlib.sha256(p.read_bytes()).hexdigest()==d['sha256'];reports.append(d)
readme="Judah's Throwing Discs 1.4.1 — NeoForge\n\nInstall only the JAR matching your Minecraft version. Requires the NeoForge version listed in validation.json or a compatible newer release. Do not install together with Fabric or Forge editions.\n\nIncludes copper-first creative ordering, mod icons, Harvest and the existing disc behavior. Minecraft 1.20 has no NeoForge release.\n\nValidation: compilation and JAR inspection only. Gameplay not tested.\n"
checks=[]
for source in [False,True]:
 name='judahs-throwing-discs-1.4.1-neoforge-all-versions'+('-sources' if source else '')+'.zip';p=out/name
 with zipfile.ZipFile(p,'w',zipfile.ZIP_DEFLATED) as z:
  z.writestr('README.txt',readme);z.writestr('validation.json',json.dumps(reports,indent=2))
  for v in matrix:
   f=out/'neoforge'/f'judahs-throwing-discs-1.4.1-neoforge-mc{v}{"-source.zip" if source else ".jar"}';z.write(f,f.name)
 checks.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+name)
(out/'judahs-throwing-discs-1.4.1-neoforge-SHA256SUMS.txt').write_text('\n'.join(checks)+'\n');print('23 NeoForge versions bundled')
