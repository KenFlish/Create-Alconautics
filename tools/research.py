from pathlib import Path
import urllib.request, json, urllib.parse
root=Path(__file__).resolve().parents[1]
def get(url):
    return urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'Alconautics-Dev/0.1'}),timeout=60).read()
for query in ('brewery','lets do foundation'):
    data=json.loads(get('https://api.modrinth.com/v2/search?'+urllib.parse.urlencode({'query':query,'limit':10})))
    print(query,[(x['slug'],x['project_id'],x['title']) for x in data['hits']],flush=True)
paths=['fluids/pump/PumpBlockEntity.java','fluids/FluidTransportBehaviour.java','fluids/FluidNetwork.java','fluids/FlowSource.java','fluids/tank/FluidTankBlockEntity.java','fluids/tank/FluidTankRenderer.java','fluids/tank/BoilerData.java','decoration/steamWhistle/WhistleBlockEntity.java','processing/burner/BlazeBurnerBlock.java']
for path in paths:
    target=root/'research'/Path(path).name; target.parent.mkdir(exist_ok=True)
    try: target.write_bytes(get('https://raw.githubusercontent.com/Creators-of-Create/Create/mc1.21.1/dev/src/main/java/com/simibubi/create/content/'+path))
    except Exception as e: print(path,e)
