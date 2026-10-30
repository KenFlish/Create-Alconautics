"""Recipes, translations and collision/selection geometry for supplied tinctures."""
from pathlib import Path
import json, math, itertools
root=Path(__file__).resolve().parents[1]
R=root/'src/main/resources'
def write(path,value):
    p=R/path;p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
layouts=[('Oduvan_nastoika','dandelion_tincture_single',1),('2Oduvan_nastoika','dandelion_tincture_pair',2),('3Oduvan_nastoika','dandelion_tincture_triple',3),('Yagod_nastoika','berry_tincture_single',1),('2Yagod_nastoika','berry_tincture_pair',2),('Oduvan_i_Yagodna_nastoika','tincture_mixed',2)]
def point(p,e):
    o=e.get('origin',[8,8,8]);p=[p[i]-o[i] for i in range(3)]
    for axis,deg in enumerate(e.get('rotation',[0,0,0])):
        c,s=math.cos(math.radians(deg)),math.sin(math.radians(deg));x,y,z=p
        p=[x,y*c-z*s,y*s+z*c] if axis==0 else [x*c+z*s,y,-x*s+z*c] if axis==1 else [x*c-y*s,x*s+y*c,z]
    return [p[i]+o[i] for i in range(3)]
shapes=[];centers=[];models=[]
for source,name,count in layouts:
    m=json.loads((root/'art/source'/f'{source}.bbmodel').read_text(encoding='utf-8'))
    boxes=[];cs=[]
    for part in range(count):
        corners=[]
        for e in m['elements'][part*4:part*4+4]:
            pts=[point(p,e) for p in itertools.product(*zip(e['from'],e['to']))];corners+=pts
            if not any(abs(a-b)<1e-6 for a,b in zip(e['from'],e['to'])):boxes.append(pts)
        cs.append([(min(p[i] for p in corners)+max(p[i] for p in corners))/32 for i in (0,2)])
        models.append(name if count==1 else name+'_'+str(part))
    centers.append(cs)
    for turn in range(4):
        entries=[]
        for corners in boxes:
            pts=[]
            for x,y,z in corners:
                for _ in range(turn):x,z=16-z,x
                pts.append((x,y,z))
            vals=[min(p[i] for p in pts) for i in range(3)]+[max(p[i] for p in pts) for i in range(3)]
            entries.append('Block.box('+','.join(str(round(v,7)) for v in vals)+')')
        shapes.append('Shapes.or('+','.join(entries)+').optimize()')
geometry='''package dev.alconautics;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
/** Generated from supplied single, paired, triple and mixed Blockbench geometry. */
public final class TinctureGeometry {
    public static final String[] MODELS = %s;
    public static final String[] NAMES = %s;
    public static final float[][][] CENTERS = %s;
    private static final VoxelShape[] SHAPES = {%s};
    public static int layout(List<ItemStack> items) {
        int d=0,b=0;for(var stack:items) if(stack.getItem() instanceof TinctureItem item) { if(item.kind==Tincture.DANDELION) d++;else b++; }
        return d>0 && b>0?5:b>0?(b==1?3:4):Math.max(0,Math.min(2,d-1));
    }
    public static int slot(List<ItemStack> items,int layout,int part) {
        if(layout!=5) return part;
        var kind=part==0?Tincture.DANDELION:Tincture.BERRY;
        for(int i=0;i<items.size();i++) if(items.get(i).getItem() instanceof TinctureItem item && item.kind==kind) return i;
        return 0;
    }
    public static String model(int layout,int part) { return NAMES[layout]+(CENTERS[layout].length==1?"":"_"+part); }
    private static int turn(Direction direction) { return switch(direction) {case EAST -> 1;case SOUTH -> 2;case WEST -> 3;default -> 0;}; }
    public static VoxelShape shape(int layout,Direction facing) { return SHAPES[layout*4+turn(facing)]; }
    public static int selectedSlot(List<ItemStack> items,Direction facing,BlockPos pos,Vec3 hit) {
        double x=hit.x-pos.getX(),z=hit.z-pos.getZ();
        for(int i=0;i<turn(facing);i++) { double old=x;x=z;z=1-old; }
        int layout=layout(items),best=0;double distance=Double.MAX_VALUE;
        for(int i=0;i<CENTERS[layout].length;i++) { var c=CENTERS[layout][i];double d=Math.pow(x-c[0],2)+Math.pow(z-c[1],2);if(d<distance) { distance=d;best=i; } }
        return slot(items,layout,best);
    }
}
'''
array=lambda values:'{'+','.join(json.dumps(v) for v in values)+'}'
centerjava='{'+','.join('{'+','.join('{'+','.join(str(round(v,7))+'f' for v in pair)+'}' for pair in layout)+'}' for layout in centers)+'}'
(root/'src/main/java/dev/alconautics/TinctureGeometry.java').write_text(geometry%(array(models),array([n for _,n,_ in layouts]),centerjava,',\n'.join(shapes)),encoding='utf-8')
write('assets/alconautics/blockstates/tincture_bottles.json',{'variants':{'':{'model':'alconautics:block/tincture_bottles'}}})
write('assets/alconautics/models/block/tincture_bottles.json',{'textures':{'particle':'alconautics:block/dandelion_tincture_single_0'},'elements':[]})
moonshine=['alconautics:'+kind+'_moonshine'+variant for kind in ('sugar','fruit','vegetable','grain') for variant in (('','_long','_strong') if kind!='vegetable' else ('',))]
write('data/alconautics/tags/fluid/moonshine.json',{'replace':False,'values':moonshine})
write('data/alconautics/tags/item/berries.json',{'replace':False,'values':['minecraft:sweet_berries','minecraft:glow_berries','farm_and_charm:strawberry',{'id':'#c:foods/berry','required':False},{'id':'#c:foods/berries','required':False},{'id':'#c:berries','required':False}]})
for kind,ingredient in [('dandelion',{'item':'minecraft:dandelion'}),('berry',{'tag':'alconautics:berries'})]:
    name=kind+'_tincture'
    write(f'assets/alconautics/models/item/{name}.json',{'parent':f'alconautics:block/{name}_single'})
    write(f'data/alconautics/recipe/mixing/{name}.json',{'type':'create:mixing','ingredients':[{'type':'neoforge:tag','tag':'alconautics:moonshine','amount':250}]+[ingredient]*(2 if kind=='berry' else 1),'results':[{'id':'alconautics:'+name,'amount':250}],'processingTime':200})
    write(f'data/alconautics/recipe/filling/{name}.json',{'type':'create:filling','ingredients':[{'item':'minecraft:glass_bottle'},{'type':'neoforge:single','fluid':'alconautics:'+name,'amount':250}],'results':[{'id':'alconautics:'+name}]})
    write(f'data/alconautics/recipe/emptying/{name}.json',{'type':'create:emptying','ingredients':[{'item':'alconautics:'+name}],'results':[{'id':'minecraft:glass_bottle'},{'id':'alconautics:'+name,'amount':250}]})
for locale,labels in {
    'en_us':['Dandelion Tincture','Berry Tincture','Tincture Bottles','Aged: %s years'],
    'ru_ru':['Одуванчиковая настойка','Ягодная настойка','Бутылочки настойки','Выдержка: %s г.'],
    'rpr':['Одуванчиковая настойка','Ягодная настойка','Бутылочки настойки','Выдержка: %s лѣтъ']}.items():
    path=f'assets/alconautics/lang/{locale}.json';lang=json.loads((R/path).read_text(encoding='utf-8'))
    for kind,label in zip(('dandelion','berry'),labels):
        lang['item.alconautics.'+kind+'_tincture']=label
        lang['fluid.alconautics.'+kind+'_tincture']=label
    lang['block.alconautics.tincture_bottles']=labels[2];lang['tooltip.alconautics.tincture_age']=labels[3];write(path,lang)
print('Generated tincture recipes, models manifest and hitboxes.')
