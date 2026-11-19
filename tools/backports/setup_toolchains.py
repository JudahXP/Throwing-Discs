import urllib.request,json,tarfile,concurrent.futures
from pathlib import Path
root=Path('/workspace/work/toolchains')
def fetch(version):
 release,build={21:('21.0.13','9'),25:('25.0.5','8')}[version]
 url=f'https://github.com/adoptium/temurin{version}-binaries/releases/download/jdk-{release}%2B{build}/OpenJDK{version}U-jdk_x64_linux_hotspot_{release}_{build}.tar.gz';p=root/f'jdk{version}.tar.gz';urllib.request.urlretrieve(url,p)
 with tarfile.open(p) as t:t.extractall(root,filter='data')
 p.unlink();print(version,release,flush=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=2) as ex:list(ex.map(fetch,[21,25]))
