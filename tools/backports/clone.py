from pathlib import Path
import shutil,re,json,sys
repo=Path(__file__).resolve().parents[2];matrix=json.loads((repo/'tools/backports/matrix.json').read_text());base=repo/'versions'
for v in sys.argv[2:]:
 r=base/('judahs-throwing-discs-'+v)
 if r.exists():continue
 shutil.copytree(base/('judahs-throwing-discs-'+sys.argv[1]),r,ignore=shutil.ignore_patterns('build','run','.gradle'))
 p=r/'gradle.properties';s=p.read_text()
 for k,value in [('minecraft_version',v),('mod_version','1.4.1+'+v),('fabric_api_version',matrix[v])]:s=re.sub('^'+k+'=.*',k+'='+value,s,flags=re.M)
 p.write_text(s);p=r/'src/main/resources/fabric.mod.json';d=json.loads(p.read_text());d['depends']['minecraft']='='+v;d['depends']['fabric-api']='>='+matrix[v];p.write_text(json.dumps(d,indent=2)+'\n')
 (r/'README.md').write_text(f'# Fabric backport for Minecraft {v}\n\nBackport in progress.\n');(r/'VALIDATION.md').write_text('Build and runtime verification pending.\n')
