"""Bake exact axis-aligned collision boxes from the supplied Blockbench cubes."""
from pathlib import Path
import json, math, itertools
root=Path(__file__).resolve().parents[1]

def bounds(model):
    m=json.loads((root/'art/source'/f'{model}.bbmodel').read_text(encoding='utf-8'))
    # These models have unrotated groups. Reject changes rather than silently
    # generating an incorrect bounding shape for a future rotated group.
    def groups(nodes):
        for n in nodes:
            if isinstance(n,dict):
                assert not any(n.get('rotation',[0,0,0])), 'Rotated group requires transform support'
                groups(n.get('children',[]))
    groups(m.get('outliner',[]))
    result=[]
    for e in m['elements']:
        if any(abs(a-b)<1e-6 for a,b in zip(e['from'],e['to'])): continue
        angles=e.get('rotation',[0,0,0]);origin=e.get('origin',[0,0,0])
        assert all(abs(a/90-round(a/90))<1e-6 for a in angles)
        points=[]
        for p in itertools.product(*zip(e['from'],e['to'])):
            p=[p[i]-origin[i] for i in range(3)]
            for axis,deg in enumerate(angles):
                c=round(math.cos(math.radians(deg)));s=round(math.sin(math.radians(deg)));x,y,z=p
                p=([x,y*c-z*s,y*s+z*c] if axis==0 else [x*c+z*s,y,-x*s+z*c] if axis==1 else [x*c-y*s,x*s+y*c,z])
            points.append([p[i]+origin[i] for i in range(3)])
        result.append([min(p[i] for p in points) for i in range(3)]+[max(p[i] for p in points) for i in range(3)])
    return result

def shape(boxes):
    return 'Shapes.or('+','.join('Block.box('+','.join(str(round(v,5)) for v in b)+')' for b in boxes)+').optimize()'
head=bounds('golovka');dry=bounds('suxharnik')
variants=[]
for turn in range(4):
    boxes=[]
    for b in head:
        points=[]
        for x,y,z in itertools.product(*zip(b[:3],b[3:])):
            for _ in range(turn):x,z=16-z,x
            points.append((x,y,z))
        boxes.append([min(p[i] for p in points) for i in range(3)]+[max(p[i] for p in points) for i in range(3)])
    variants.append(shape(boxes))
java='''package dev.alconautics;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;

/** Generated from art/source by tools/model_shapes.py; includes cube pivots. */
final class ModelShapes {
    static final VoxelShape DRY = %s;
    private static final VoxelShape[] HEAD = {%s};
    static VoxelShape head(Direction facing) {
        return HEAD[switch(facing) { case SOUTH -> 1; case WEST -> 2; case NORTH -> 3; default -> 0; }];
    }
}
'''%(shape(dry),',\n        '.join(variants))
(root/'src/main/java/dev/alconautics/ModelShapes.java').write_text(java,encoding='utf-8')
print('Baked model shapes:',len(head),'head cubes,',len(dry),'dry chamber cubes')
