from pathlib import Path
import os,subprocess,sys,urllib.parse,urllib.request,json,zipfile
repo=Path(__file__).resolve().parents[2];base=Path('/workspace/work');cache=base/'gradle-cache';cache.mkdir(exist_ok=True)
env=os.environ.copy();env['JAVA_HOME']=str(base/'toolchains/jdk-25.0.5+8');env['JDK_21']=str(base/'toolchains/jdk-21.0.13+9');env['GRADLE_USER_HOME']=str(cache)
u=urllib.parse.urlparse(env.get('HTTPS_PROXY',''));env['JAVA_TOOL_OPTIONS']='-XX:ActiveProcessorCount=2 -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts'+(f' -Dhttps.proxyHost={u.hostname} -Dhttps.proxyPort={u.port} -Dhttp.proxyHost={u.hostname} -Dhttp.proxyPort={u.port}' if u.hostname else '')
for v in sys.argv[1:]:
 root=repo/'versions'/('judahs-throwing-discs-'+v)
 if tuple(map(int,v.split('.'))) >=(1,21):
  jar=cache/'caches/fabric-loom'/v/'minecraft-client.jar';jar.parent.mkdir(parents=True,exist_ok=True)
  if not jar.exists():
   manifest=json.load(urllib.request.urlopen('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'))
   meta=json.load(urllib.request.urlopen(next(d['url'] for d in manifest['versions'] if d['id']==v)))
   urllib.request.urlretrieve(meta['downloads']['client']['url'],jar)
  with zipfile.ZipFile(jar) as z:
   res=root/'src/main/resources'
   for name in ['fire_aspect','knockback']:
    d=json.loads(z.read(f'data/minecraft/enchantment/{name}.json'));original=d['supported_items'];d['supported_items']='#throwing_disc:'+name+'_enchantable'
    (res/'data/minecraft/enchantment'/f'{name}.json').write_text(json.dumps(d,indent=2)+'\n')
    (res/'data/throwing_disc/tags/item'/f'{name}_enchantable.json').write_text(json.dumps({'values':([original] if isinstance(original,str) else original)+['#throwing_disc:discs']},indent=2)+'\n')
 with (base/f'backport-{v}.log').open('w') as log:
  result=subprocess.run(['bash','./gradlew','--no-daemon','--max-workers=2',*os.environ.get('BUILD_TASKS','build runGameTest').split()],cwd=root,env=env,stdout=log,stderr=subprocess.STDOUT)
 print(v,result.returncode,flush=True)
