from pathlib import Path
import urllib.request, urllib.parse,json
root=Path(__file__).resolve().parents[1]
def get(url): return json.loads(urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'Alconautics-Dev/0.1'}),timeout=60).read())
v=get('https://api.modrinth.com/v2/project/lets-do-brewery-farmcharm-compat/version?'+urllib.parse.urlencode({'game_versions':'["1.21.1"]','loaders':'["neoforge"]'}))[0]
print(v['dependencies'],flush=True)
for dep in v['dependencies']:
    if dep['dependency_type']!='required': continue
    project=dep['project_id']; info=get('https://api.modrinth.com/v2/project/'+project)
    print(info['slug'],flush=True)
    if 'foundation' not in info['slug']: continue
    versions=get('https://api.modrinth.com/v2/project/'+project+'/version?'+urllib.parse.urlencode({'game_versions':'["1.21.1"]','loaders':'["neoforge"]'}))
    f=next(f for f in versions[0]['files'] if f['primary'])
    (root/'libs/runtime'/f['filename']).write_bytes(urllib.request.urlopen(f['url']).read())
    print(f['filename'],flush=True)
