from pathlib import Path
import json,zipfile,hashlib
repo=Path(__file__).resolve().parents[2];out=repo/'downloads/backports';versions=list(json.loads((repo/'tools/backports/matrix.json').read_text()))
results=[json.loads((out/f'validation-{v}.json').read_text())for v in versions];assert len(results)==18 and all(d['unit_tests']==11 and d['game_tests']>=9 for d in results)
notes='''Judah's Throwing Discs 1.4.1 — Fabric backports

All 18 released Minecraft versions from 1.20 through 1.21.10. Install ONLY the JAR matching your exact Minecraft version, on client and server, with Fabric Loader 0.19.5+ and matching Fabric API. Java 17+ for 1.20–1.20.4; Java 21+ for 1.20.5 onward.

Copper discs are included in every supported version. Every version includes iron, golden, diamond and netherite discs.

Preserves smooth direct return without a forced arc, unlimited bounce count, no total piercing-count cap, Harvest I, reusable dispenser throws and complete Ricochet removal. Each entity may be hit once per outbound/return pass. Phasing is allowed ONLY while returning and when the entire movement step stays within eight blocks of the destination player/dispenser. Base ranges are unchanged.

Throw / melee / base range: copper 5 / 3 / 14; iron 6 / 4 / 16; gold 5 / 3 / 18; diamond 7.5 / 5 / 19; netherite 8.5 / 6 / 21.

Harvest breaks mature wheat, carrots, potatoes, beetroot, nether wart, cocoa, melons, pumpkins and upper sugar cane/bamboo. Vanilla drops; no automatic replanting. Young crops and the bottom cane/bamboo block remain. Dispensers recover the real returning disc in an empty slot, or drop it if missing/full. Throws consume durability. Older versions use the available trident impact sound.

All included builds passed unit and dedicated-server game tests. See VALIDATION.json and per-project VALIDATION.md. Graphical playtesting and multiplayer latency testing have not been performed.
'''
name='judahs-throwing-discs-1.4.1-fabric-1.20-to-1.21.10';checks=[]
for kind,suffix in [('', '.jar'),('-sources','-source.zip')]:
 p=repo/'downloads'/(name+kind+'.zip')
 with zipfile.ZipFile(p,'w',zipfile.ZIP_DEFLATED)as z:
  for v in versions:
   f=out/f'judahs-throwing-discs-1.4.1-mc{v}{suffix}';assert f.is_file();z.write(f,f.name)
  z.writestr('README.txt',notes);z.writestr('VALIDATION.json',json.dumps(results,indent=2)+'\n')
 assert p.stat().st_size<100_000_000
 with zipfile.ZipFile(p)as z:assert z.testzip()is None and len(z.namelist())==20
 checks.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name)
(repo/'downloads'/(name+'-SHA256SUMS.txt')).write_text('\n'.join(checks)+'\n')
p=repo/'README.md';old=p.read_text();section=f'''## Fabric backports: Minecraft 1.20–1.21.10

[Download backport JARs](https://github.com/JudahXP/Throwing-Discs/raw/refs/heads/main/downloads/{name}.zip) · [Download complete backport source](https://github.com/JudahXP/Throwing-Discs/raw/refs/heads/main/downloads/{name}-sources.zip)

All 18 targets passed their builds and automated unit/server tests. Use the JAR for your exact Minecraft version. Copper discs are included in every supported version. Java 17+ for 1.20–1.20.4; Java 21+ for 1.20.5 onward. Fabric Loader 0.19.5+ and matching Fabric API are required. Graphical playtesting has not been performed.

'''
p.write_text(old.split('\n',1)[0]+'\n\n'+section+'## Newer versions: Minecraft 1.21.11 and 26.x\n\n'+old.split('\n',1)[1].lstrip().replace('[Download all mod JARs]','[Download newer-version JARs]').replace('[Download complete source]','[Download newer-version source]'));print('\n'.join(checks))
