"""Download pinned development dependencies and preserve the supplied source models."""
from pathlib import Path
import json, shutil, urllib.request, urllib.parse, zipfile, hashlib

ROOT = Path(__file__).resolve().parents[1]
def fetch(url, dest):
    dest = ROOT / dest
    dest.parent.mkdir(parents=True, exist_ok=True)
    if not dest.exists():
        req = urllib.request.Request(url, headers={'User-Agent':'Alconautics-Development/0.1'})
        dest.write_bytes(urllib.request.urlopen(req, timeout=90).read())
    return dest

for name in ('gradlew', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.jar', 'gradle/wrapper/gradle-wrapper.properties'):
    fetch('https://raw.githubusercontent.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/main/' + name, name)

local_create = ROOT/'libs/create-1.21.1-6.0.10.jar'
(ROOT/'libs').mkdir(exist_ok=True)
if not local_create.exists():
    query=urllib.parse.urlencode({'game_versions':json.dumps(['1.21.1']),'loaders':json.dumps(['neoforge'])})
    versions=json.loads(urllib.request.urlopen(urllib.request.Request('https://api.modrinth.com/v2/project/create/version?'+query,headers={'User-Agent':'Alconautics-Development/0.1'}),timeout=60).read())
    file=next(f for v in versions for f in v['files'] if f['filename']==local_create.name)
    fetch(file['url'],'libs/'+local_create.name)
    if hashlib.sha512(local_create.read_bytes()).hexdigest()!=file['hashes']['sha512']:
        raise RuntimeError('Create dependency hash mismatch')
with zipfile.ZipFile(local_create) as z:
    for name in z.namelist():
        if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
            target = ROOT/'libs/compile'/Path(name).name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(z.read(name))

lock_path = ROOT/'dependency-lock.json'
previous_lock = json.loads(lock_path.read_text(encoding='utf-8')) if lock_path.exists() else {}
lock = {}
for project in ('lets-do-brewery-farmcharm-compat', 'lets-do-farm-charm', 'architectury-api'):
    url = 'https://api.modrinth.com/v2/project/' + project + '/version?' + urllib.parse.urlencode({
        'game_versions':json.dumps(['1.21.1']), 'loaders':json.dumps(['neoforge'])})
    try:
        versions = json.loads(urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent':'Alconautics-Development/0.1'}), timeout=60).read())
        if not versions:
            print('No matching versions:', project); continue
        pinned = previous_lock.get(project, {}).get('version')
        v = next(v for v in versions if v['version_number'] == pinned) if pinned else versions[0]
        f = next((f for f in v['files'] if f['primary']), v['files'][0])
        jar=fetch(f['url'], 'libs/runtime/' + f['filename'])
        if hashlib.sha512(jar.read_bytes()).hexdigest() != f['hashes']['sha512']:
            raise RuntimeError('Dependency hash mismatch: '+f['filename'])
        lock[project] = {'version':v['version_number'], 'filename':f['filename'], 'sha512':f['hashes']['sha512']}
        print(project, v['version_number'], flush=True)
    except Exception as e:
        raise RuntimeError('Could not prepare '+project) from e
(ROOT/'dependency-lock.json').write_text(json.dumps(lock, indent=2), encoding='utf-8')
with zipfile.ZipFile(ROOT/'libs/runtime/letsdo-farm_and_charm-neoforge-1.1.29.jar') as z:
    name=next(n for n in z.namelist() if n.endswith('foundation-neoforge-1.0.7.jar'))
    target=ROOT/'libs/compile/foundation-neoforge-1.0.7.jar'
    target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read(name))
for name in ('golovka','holodilnik','suxharnik','samogon','2samodona'):
    target = ROOT/'art/source'/f'{name}.bbmodel'
    target.parent.mkdir(parents=True, exist_ok=True)
    if not target.exists():
        raise FileNotFoundError('Missing checked-in artwork: '+str(target))

# Compile-only optional integration. Sable and its libraries are never bundled.
sable=ROOT/'libs/compile/sable-neoforge-1.21.1-2.0.6.jar'
if not sable.exists():
    request=urllib.request.Request('https://api.modrinth.com/v2/project/sable/version',headers={'User-Agent':'Alconautics-Development/0.1'})
    versions=json.loads(urllib.request.urlopen(request,timeout=60).read())
    file=next(f for v in versions for f in v['files'] if f['filename']==sable.name)
    fetch(file['url'],'libs/compile/'+sable.name)
    if hashlib.sha512(sable.read_bytes()).hexdigest()!=file['hashes']['sha512']:
        raise RuntimeError('Sable dependency hash mismatch')
with zipfile.ZipFile(sable) as z:
    for name in z.namelist():
        if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
            (ROOT/'libs/compile'/Path(name).name).write_bytes(z.read(name))

# Optional APIs are development inputs; enable -PcompatCheck for runtime tests.
for project,filename in [('create-diesel-generators','createdieselgenerators-1.21.1-1.3.15.jar'),('jei','jei-1.21.1-neoforge-19.69.0.457.jar')]:
    target=ROOT/'libs/compile'/filename
    if target.exists(): continue
    request=urllib.request.Request('https://api.modrinth.com/v2/project/'+project+'/version',headers={'User-Agent':'Alconautics-Development/0.1'})
    versions=json.loads(urllib.request.urlopen(request,timeout=60).read())
    file=next(f for version in versions for f in version['files'] if f['filename']==filename)
    fetch(file['url'],'libs/compile/'+filename)
    if hashlib.sha512(target.read_bytes()).hexdigest()!=file['hashes']['sha512']:
        raise RuntimeError('Dependency hash mismatch: '+filename)
