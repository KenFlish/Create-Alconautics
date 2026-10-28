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

local_create = ROOT/'libs/create-1.21.1-6.0.9.jar'
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
for name in ('golovka','holodilnik','suxharnik','samogon'):
    target = ROOT/'art/source'/f'{name}.bbmodel'
    target.parent.mkdir(parents=True, exist_ok=True)
    if not target.exists():
        raise FileNotFoundError('Missing checked-in artwork: '+str(target))
