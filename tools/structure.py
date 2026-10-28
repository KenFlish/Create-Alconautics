"""Generate the Ponder/example apparatus using standard Minecraft structure NBT."""
from pathlib import Path
import struct,gzip
root=Path(__file__).resolve().parents[1]/'src/main/resources'
def string(s):
    b=s.encode('utf-8');return struct.pack('>H',len(b))+b
def tag(t,name,payload):return bytes([t])+string(name)+payload
def compound(d):return b''.join(tag(t,k,v) for k,(t,v) in d.items())+b'\0'
def integer(i):return struct.pack('>i',i)
def ints(v):return bytes([3])+integer(len(v))+b''.join(integer(i) for i in v)
palette=[];blocks=[]
def block(x,y,z,name,props=None):
    desc=(name,tuple(sorted((props or {}).items())))
    if desc not in palette:palette.append(desc)
    blocks.append(compound({'pos':(9,ints([x,y,z])),'state':(3,integer(palette.index(desc)))}))
for x in range(6):
    for z in range(4):block(x,0,z,'minecraft:polished_andesite')
block(1,1,1,'create:blaze_burner',{'blaze':'kindled'})
block(1,2,1,'create:fluid_tank')
block(1,3,1,'alconautics:distillation_head')
block(1,2,0,'create:steam_whistle',{'facing':'south','wall':'true','powered':'false','size':'small'})
block(4,2,1,'alconautics:condenser_base')
block(2,3,1,'alconautics:dry_chamber')
for x,y,z,a,b,wet in [(1,4,1,'down','east',False),(2,4,1,'west','east',False),(3,4,1,'west','east',False),(4,4,1,'west','down',True),(4,3,1,'up','down',True)]:
    props={d:str(d in (a,b)).lower() for d in ['up','down','north','south','east','west']};props['waterlogged']=str(wet).lower()
    block(x,y,z,'create:fluid_pipe',props)
for y in [3,4]:
    for z in [0,2]:block(4,y,z,'minecraft:glass')
    block(5,y,1,'minecraft:glass')
block(3,3,1,'minecraft:glass')
block(4,5,1,'minecraft:glass')
ps=[]
for name,props in palette:
    d={'Name':(8,string(name))}
    if props:d['Properties']=(10,compound({k:(8,string(v)) for k,v in props}))
    ps.append(compound(d))
data=tag(10,'',compound({'DataVersion':(3,integer(3955)),'size':(9,ints([6,6,4])),'palette':(9,bytes([10])+integer(len(ps))+b''.join(ps)),'blocks':(9,bytes([10])+integer(len(blocks))+b''.join(blocks)),'entities':(9,bytes([10])+integer(0))}))
for path in ['data/alconautics/structure/distillery.nbt']:
    p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(gzip.compress(data))
print('Generated example structure (no Ponder scenes).')
