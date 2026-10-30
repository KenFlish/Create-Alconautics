"""Prepare user structures for isolated Ponder worlds; no world coordinates/owners."""
from pathlib import Path
import gzip, struct, io
root=Path(__file__).resolve().parents[1]

def prepare(source,target,keep_copycat=False):
    s=io.BytesIO(gzip.decompress(source.read_bytes()))
    def num(fmt):return struct.unpack('>'+fmt,s.read(struct.calcsize('>'+fmt)))[0]
    def string():return s.read(num('H')).decode('utf-8')
    def read(t):
        if t in range(1,7):return num({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[t])
        if t==8:return string()
        if t==9:
            typ=num('b');return typ,[read(typ) for _ in range(num('i'))]
        if t==10:
            d={}
            while True:
                typ=num('b')
                if not typ:return d
                key=string();d[key]=(typ,read(typ))
        if t in [7,11,12]:return [num({7:'b',11:'i',12:'q'}[t]) for _ in range(num('i'))]
        raise ValueError(t)
    def pack(fmt,n):return struct.pack('>'+fmt,n)
    def text(v):
        b=v.encode('utf-8');return pack('H',len(b))+b
    def write(t,v):
        if t in range(1,7):return pack({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[t],v)
        if t==8:return text(v)
        if t==9:return bytes([v[0]])+pack('i',len(v[1]))+b''.join(write(v[0],i) for i in v[1])
        if t==10:return b''.join(bytes([typ])+text(k)+write(typ,val) for k,(typ,val) in v.items())+b'\0'
        if t in [7,11,12]:return pack('i',len(v))+b''.join(pack({7:'b',11:'i',12:'q'}[t],n) for n in v)
    t=num('b');name=string();d=read(t)
    for b in d['blocks'][1][1]:
        if 'nbt' in b:
            old=b['nbt'][1]
            keys=('id','Material','Item','EnableCT') if keep_copycat and old['id'][1]=='create:copycat' else ('id',)
            b['nbt']=(10,{k:old[k] for k in keys if k in old})
    target.parent.mkdir(parents=True,exist_ok=True)
    target.write_bytes(gzip.compress(bytes([t])+text(name)+write(t,d),mtime=0))

for source,target in [('ponder1_1','apparatus'),('ponder2','mash_and_dry_chamber')]:
    prepare(root/'art/source'/f'{source}.nbt',root/'src/main/resources/assets/alconautics/ponder'/f'{target}.nbt')
print('Prepared two Ponder structures, retaining the supplied block layouts.')
prepare(root/'art/source/jei_distillery.nbt',root/'src/main/resources/assets/alconautics/jei/distillery.nbt',keep_copycat=True)
