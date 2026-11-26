from pathlib import Path
import os,sys,subprocess,urllib.parse,json
repo=Path(__file__).resolve().parents[2];work=Path('/workspace/work');env=os.environ.copy();env['_JAVA_OPTIONS']='-Xms128m -Xmx4g';env['JAVA_HOME']=str(work/'toolchains/jdk-25.0.5+8');env['GRADLE_USER_HOME']=str(work/'neoforge-gradle');u=urllib.parse.urlparse(env.get('HTTPS_PROXY',''));env['JAVA_TOOL_OPTIONS']='-XX:ActiveProcessorCount=2 -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts'+(f' -Dhttps.proxyHost={u.hostname} -Dhttps.proxyPort={u.port} -Dhttp.proxyHost={u.hostname} -Dhttp.proxyPort={u.port}' if u.hostname else '')
for v in sys.argv[1:]:
 env['JAVA_HOME']=str(work/'toolchains'/('jdk-21.0.13+9' if v in ['1.20.2','1.20.3','1.20.5'] else 'jdk-25.0.5+8'))
 with (work/f'neoforge-{v}.log').open('w') as log:
  result=subprocess.run(['bash','gradlew','--no-daemon','--max-workers=2','-Porg.gradle.java.installations.paths=/workspace/work/toolchains/jdk-25.0.5+8,/workspace/work/toolchains/jdk-21.0.13+9,/workspace/work/toolchains/jdk-8,/workspace/work/toolchains/jdk-17','assemble','-x','test'],cwd=repo/'neoforge'/('judahs-throwing-discs-'+v),env=env,stdout=log,stderr=subprocess.STDOUT)
 print(v,result.returncode,flush=True)
