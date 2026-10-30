"""Deterministic Blockbench geometry conversion and recipe/localization generation."""
from pathlib import Path
import json, math, base64, re, shutil, zipfile, itertools
R=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(path,data):
    p=R/path;p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n' if not isinstance(data,str) else data,encoding='utf-8')

names={'golovka':'distillation_head','holodilnik':'condenser_base','suxharnik':'dry_chamber','samogon':'moonshine_bottle_single','2samodona':'moonshine_bottle_pair'}
references={'engine.png':'create:block/engine','basin.png':'create:block/basin','roller_metal.png':'create:block/roller_metal','framed_glass.png':'create:block/palettes/framed_glass','item_drain_top.png':'create:block/item_drain_top','fan_blades.png':'create:block/fan_blades','velocity_sensor_2.png':'minecraft:block/copper_block'}
create_assets=zipfile.ZipFile(R.parents[2]/'libs/create-1.21.1-6.0.10.jar')
display={'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.65,.65,.65]},'ground':{'translation':[0,3,0],'scale':[.5,.5,.5]},'fixed':{'scale':[.6,.6,.6]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.375,.375,.375]},'firstperson_righthand':{'rotation':[0,45,0],'translation':[0,0,0],'scale':[.5,.5,.5]}}
def rotate(point,angles,origin):
    p=[point[i]-origin[i] for i in range(3)]
    for axis,deg in enumerate(angles):
        a=math.radians(deg);c,s=math.cos(a),math.sin(a);x,y,z=p
        p=([x,y*c-z*s,y*s+z*c] if axis==0 else [x*c+z*s,y,-x*s+z*c] if axis==1 else [x*c-y*s,x*s+y*c,z])
    return [(p[i]+origin[i])/16 for i in range(3)]
for src,dst in names.items():
    m=json.loads((R.parents[2]/'art/source'/f'{src}.bbmodel').read_text(encoding='utf-8'))
    textures={};mtl=[]
    for i,t in enumerate(m['textures']):
        name=t['name']
        embedded=base64.b64decode(t['source'].split(',',1)[1])
        reference=references.get(name)
        original_path='assets/create/textures/'+reference.split(':',1)[1]+'.png' if reference and reference.startswith('create:') else None
        if original_path and original_path in create_assets.namelist() and embedded==create_assets.read(original_path): texture=reference
        else:
            texture=f'alconautics:block/{dst}_{i}'
            p=R/f'assets/alconautics/textures/block/{dst}_{i}.png';p.parent.mkdir(parents=True,exist_ok=True)
            p.write_bytes(embedded)
        textures[str(i)]=texture
        mtl+=['newmtl tex'+str(i),'Kd 1 1 1','map_Kd #'+str(i),'']
    textures['particle']=textures['0']
    obj=['# Converted from user-provided Blockbench geometry','mtllib '+dst+'.mtl']
    index=0
    for element_number,element in enumerate(m['elements']):
        if src=='2samodona':obj+=['g bottle_'+str(element_number//4)]
        x0,y0,z0=element['from'];x1,y1,z1=element['to']
        faces={'north':[(x1,y0,z0),(x0,y0,z0),(x0,y1,z0),(x1,y1,z0)],'south':[(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)],'west':[(x0,y0,z0),(x0,y0,z1),(x0,y1,z1),(x0,y1,z0)],'east':[(x1,y0,z1),(x1,y0,z0),(x1,y1,z0),(x1,y1,z1)],'up':[(x0,y1,z1),(x1,y1,z1),(x1,y1,z0),(x0,y1,z0)],'down':[(x0,y0,z0),(x1,y0,z0),(x1,y0,z1),(x0,y0,z1)]}
        for face,props in element.get('faces',{}).items():
            tex=props.get('texture')
            if tex is None: continue
            tex=int(str(tex).lstrip('#'))
            obj+=['usemtl tex'+str(tex)]
            for v in faces[face]: obj+=['v '+' '.join(f'{a:.7f}' for a in rotate(v,element.get('rotation',[0,0,0]),element.get('origin',[8,8,8])))]
            uv=props.get('uv',[0,0,16,16]);texture=m['textures'][tex];w=texture.get('uv_width',m['resolution']['width']);h=texture.get('uv_height',m['resolution']['height'])
            coords=[(uv[0]/w,1-uv[3]/h),(uv[2]/w,1-uv[3]/h),(uv[2]/w,1-uv[1]/h),(uv[0]/w,1-uv[1]/h)]
            shift=props.get('rotation',0)//90;coords=coords[shift:]+coords[:shift]
            obj+=['vt %.7f %.7f'%c for c in coords]
            obj+=['f '+' '.join(f'{index+j}/{index+j}' for j in range(1,5))];index+=4
    write(f'assets/alconautics/models/block/{dst}.obj','\n'.join(obj)+'\n')
    write(f'assets/alconautics/models/block/{dst}.mtl','\n'.join(mtl))
    model={'loader':'neoforge:obj','model':f'alconautics:models/block/{dst}.obj','automatic_culling':False,'flip_v':True,'textures':textures,'display':display|m.get('display',{}),'render_type':'minecraft:cutout'}
    if src=='2samodona':
        for part in range(2):
            selected=False;lines=[]
            for line in obj:
                if line.startswith('g '):selected=line==f'g bottle_{part}'
                if not line.startswith('f ') or selected:lines.append(line)
            write(f'assets/alconautics/models/block/{dst}_{part}.obj','\n'.join(lines)+'\n')
            write(f'assets/alconautics/models/block/{dst}_{part}.json',model|{'model':f'alconautics:models/block/{dst}_{part}.obj'})
    if src=='suxharnik':
        elements=[]
        for i,e in enumerate(m['elements']):
            faces={}
            for direction,face in e['faces'].items():
                if face.get('texture') is None:continue
                ti=int(str(face['texture']).lstrip('#'));t=m['textures'][ti]
                w=t.get('uv_width',m['resolution']['width']);h=t.get('uv_height',m['resolution']['height'])
                uv=face['uv'];f={'texture':'#'+str(ti),'uv':[uv[0]*16/w,uv[1]*16/h,uv[2]*16/w,uv[3]*16/h]}
                if face.get('rotation'):f['rotation']=face['rotation']
                faces[direction]=f
            elements.append({'from':e['from'],'to':e['to'],'faces':faces})
        model={'textures':textures,'elements':elements,'ambientocclusion':False,'display':display|m.get('display',{}),'render_type':'minecraft:cutout'}
    write(f'assets/alconautics/models/block/{dst}.json',model)
    if src not in ('samogon','2samodona'):
        variants={f'facing={direction}':{'model':f'alconautics:block/{dst}',**({'y':angle} if src=='golovka' else {})} for direction,angle in [('east',0),('south',90),('west',180),('north',270)]}
        write(f'assets/alconautics/blockstates/{dst}.json',{'variants':variants})
        write(f'assets/alconautics/models/item/{dst}.json',{'parent':f'alconautics:block/{dst}'})
        write(f'data/alconautics/loot_table/blocks/{dst}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'alconautics:'+dst}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})

drinks=[(k,v) for k in ['sugar','fruit','vegetable','grain'] for v in (['','_long','_strong'] if k!='vegetable' else [''])]
(R/'assets/alconautics/textures/item').mkdir(parents=True,exist_ok=True)
shutil.copy2(R.parents[2]/'art/source/moonshine.png',R/'assets/alconautics/textures/item/moonshine.png')
write('assets/alconautics/blockstates/moonshine_bottle.json',{'variants':{f'drink={i}':{'model':'alconautics:block/moonshine_bottle'} for i in range(10)}})
write('assets/alconautics/models/block/moonshine_bottle.json',{'textures':{'particle':'alconautics:block/moonshine_bottle_single_0'},'elements':[]})
# Collision bounds use transformed corners, including the pair's arbitrary Y rotations.
variants=[]
for src in ['samogon','2samodona']:
    m=json.loads((R.parents[2]/'art/source'/f'{src}.bbmodel').read_text(encoding='utf-8'))
    boxes=[]
    for e in m['elements']:
        if any(abs(a-b)<1e-6 for a,b in zip(e['from'],e['to'])):continue
        points=[rotate(p,e.get('rotation',[0,0,0]),e.get('origin',[8,8,8])) for p in itertools.product(*zip(e['from'],e['to']))]
        boxes.append([min(p[i]*16 for p in points) for i in range(3)]+[max(p[i]*16 for p in points) for i in range(3)])
    for turns in range(4):
        rotated=[]
        for b in boxes:
            points=[]
            for x,y,z in itertools.product(*zip(b[:3],b[3:])):
                for _ in range(turns):x,z=16-z,x
                points.append((x,y,z))
            coords=[min(p[i] for p in points) for i in range(3)]+[max(p[i] for p in points) for i in range(3)]
            rotated.append('Block.box('+','.join(str(round(v,7)) for v in coords)+')')
        variants.append('Shapes.or('+','.join(rotated)+').optimize()')
(R.parents[2]/'src/main/java/dev/alconautics/BottleShapes.java').write_text('''package dev.alconautics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;
/** Generated from the two supplied Blockbench models by tools/resources.py. */
final class BottleShapes {
    private static final VoxelShape[] SHAPES = {%s};
    static VoxelShape get(int count,Direction facing) {
        int turn=switch(facing) {case EAST -> 1;case SOUTH -> 2;case WEST -> 3;default -> 0;};
        return SHAPES[(count-1)*4+turn];
    }
}
'''%(',\n'.join(variants)),encoding='utf-8')
for kind,var in drinks:
    name=kind+'_moonshine'+var
    write(f'assets/alconautics/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'alconautics:item/moonshine'}})
    write(f'data/alconautics/recipe/filling/{name}.json',{'type':'create:filling','ingredients':[{'item':'minecraft:glass_bottle'},{'type':'neoforge:single','fluid':'alconautics:'+name,'amount':250}],'results':[{'id':'alconautics:'+name}]})
    write(f'data/alconautics/recipe/emptying/{name}.json',{'type':'create:emptying','ingredients':[{'item':'alconautics:'+name}],'results':[{'id':'minecraft:glass_bottle'},{'id':'alconautics:'+name,'amount':250}]})
for kind,ingredient in [('sugar','minecraft:sugar'),('fruit','#alconautics:fruits'),('vegetable','#alconautics:vegetables'),('grain','#alconautics:grains')]:
    ing={'tag':ingredient[1:]} if ingredient.startswith('#') else {'item':ingredient}
    write(f'data/alconautics/recipe/mixing/{kind}_mash.json',{'type':'create:mixing','ingredients':[{'type':'neoforge:single','fluid':'minecraft:water','amount':1000},{'item':'farm_and_charm:yeast'}]+[ing]*4,'results':[{'id':f'alconautics:{kind}_mash','amount':1000}]})
for tag,items in [('fruits',['minecraft:apple']),('vegetables',['minecraft:potato','minecraft:carrot','minecraft:beetroot']),('grains',['minecraft:wheat','farm_and_charm:oat','farm_and_charm:barley'])]:
    write(f'data/alconautics/tags/item/{tag}.json',{'replace':False,'values':items})
for name,pattern,key in [('distillation_head',['P','C'],{'P':'create:fluid_pipe','C':'minecraft:compass'}),('condenser_base',[' C ','IPI',' B '],{'C':'create:fluid_pipe','I':'create:iron_sheet','P':'create:basin','B':'create:copper_sheet'}),('dry_chamber',[' P ','CBC'],{'P':'create:fluid_pipe','C':'create:copper_sheet','B':'minecraft:glass_bottle'})]:
    write(f'data/alconautics/recipe/crafting/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':'alconautics:'+name,'count':1}})

ru={'itemGroup.alconautics':'Create: Alconautics','block.alconautics.distillation_head':'Перегонная головка','block.alconautics.condenser_base':'Основаніе холодильника','block.alconautics.dry_chamber':'Сухопарникъ','block.alconautics.moonshine_bottle':'Бутылка самогона','effect.alconautics.growth':'Великанъ','effect.alconautics.shrinking':'Лилипутъ',
 'tooltip.alconautics.status':'Статусъ перегоннаго аппарата: %s','tooltip.alconautics.mash':'Брага  %s','tooltip.alconautics.kind':'Типъ: %s','tooltip.alconautics.heat':'Нагрѣвъ  %s','tooltip.alconautics.dry':'Сухопарникъ','tooltip.alconautics.modifier':'Добавка: %s','tooltip.alconautics.credit':'Остатокъ обработки: %s мБ','tooltip.alconautics.cooler':'Проточный холодильникъ','tooltip.alconautics.cold_credit':'Запасъ холода: %s мБ','tooltip.alconautics.liquid':'Самогонъ: %s / %s мБ','tooltip.alconautics.wet_ok':'Трубы залиты водой','tooltip.alconautics.wet_pipes':'Нужны двѣ соединённыя трубы въ водѣ','tooltip.alconautics.intoxication':'Вызываетъ опьянѣніе','tooltip.alconautics.place':'Shift + ПКМъ — поставить','modifier.alconautics.normal':'Нѣтъ','modifier.alconautics.long':'Редстоунъ — дольше','modifier.alconautics.strong':'Свѣтокаменная пыль — сильнѣе',
 'kind.alconautics.none':'Нѣтъ','kind.alconautics.sugar':'Сахарная','kind.alconautics.fruit':'Фруктовая','kind.alconautics.vegetable':'Овощная'}
en={'itemGroup.alconautics':'Create: Alconautics','block.alconautics.distillation_head':'Distillation Head','block.alconautics.condenser_base':'Condenser Base','block.alconautics.dry_chamber':'Dry Chamber','block.alconautics.moonshine_bottle':'Moonshine Bottle','effect.alconautics.growth':'Giant','effect.alconautics.shrinking':'Lilliputian',
 'tooltip.alconautics.status':'Distillation apparatus status: %s','tooltip.alconautics.mash':'Mash  %s','tooltip.alconautics.kind':'Type: %s','tooltip.alconautics.heat':'Heat  %s','tooltip.alconautics.dry':'Dry chamber','tooltip.alconautics.modifier':'Modifier: %s','tooltip.alconautics.credit':'Treatment remaining: %s mB','tooltip.alconautics.cooler':'Condenser','tooltip.alconautics.cold_credit':'Cooling remaining: %s mB','tooltip.alconautics.liquid':'Moonshine: %s / %s mB','tooltip.alconautics.wet_ok':'Waterlogged pipes ready','tooltip.alconautics.wet_pipes':'Requires two connected waterlogged pipes','tooltip.alconautics.intoxication':'Causes intoxication','tooltip.alconautics.place':'Shift + right-click to place','modifier.alconautics.normal':'None','modifier.alconautics.long':'Redstone — longer','modifier.alconautics.strong':'Glowstone — stronger','kind.alconautics.none':'None','kind.alconautics.sugar':'Sugar','kind.alconautics.fruit':'Fruit','kind.alconautics.vegetable':'Vegetable'}
statuses={'empty':('Нѣтъ браги','No mash'),'wrong_fluid':('Неподходящая жидкость','Wrong fluid'),'heat':('Нѣтъ нагрѣва','No heat'),'running':('Работаетъ','Running'),'no_whistle':('Нѣтъ свистка — опасность!','No whistle — danger!'),'tank':('Нѣтъ бака','No tank'),'heads':('Лишняя головка','Multiple heads'),'route':('Провѣрьте трубы','Check pipe route'),'branch':('Развѣтвленіе трубы','Branched pipe route'),'dry_count':('Лишній сухопарникъ','Multiple dry chambers'),'wet_pipes':('Нужны двѣ трубы въ водѣ','Needs two waterlogged pipes'),'range':('Слишкомъ длинная труба','Pipe route too long'),'cold':('Нѣтъ холода','No coolant'),'output':('Выходъ заполненъ','Output full or different drink'),'buffer':('Ожиданіе передачи пара','Waiting for vapour transfer'),'engine':('Снимите паровой двигатель','Remove steam engine')}
for key,(r,e) in statuses.items():ru['status.alconautics.'+key]=r;en['status.alconautics.'+key]=e
for kind,var in drinks:
    ident=kind+'_moonshine'+var;rn={'sugar':'Сахарный','fruit':'Фруктовый','vegetable':'Овощной','grain':'Зерновой'}[kind]+' самогонъ'+{'':'','_long':' (долгій)','_strong':' (крѣпкій)'}[var]
    english=kind.title()+' Moonshine'+{'':'','_long':' (Extended)','_strong':' (Strong)'}[var]
    ru['item.alconautics.'+ident]='Самогонъ';en['item.alconautics.'+ident]='Moonshine'
    for suffix in ['','_vapour']:
        ru['fluid.alconautics.'+ident+suffix]=('Паръ: ' if suffix else '')+rn
        en['fluid.alconautics.'+ident+suffix]=english+(' Vapour' if suffix else '')
for kind in ['sugar','fruit','vegetable','grain']:
    ru['fluid.alconautics.'+kind+'_mash']={'sugar':'Сахарная','fruit':'Фруктовая','vegetable':'Овощная','grain':'Зерновая'}[kind]+' брага'
    en['fluid.alconautics.'+kind+'_mash']=kind.title()+' Mash'
ru.update({'tooltip.alconautics.mash':'Брага','tooltip.alconautics.mash_dots':'........ ','tooltip.alconautics.heat':'Нагрѣвъ','tooltip.alconautics.heat_dots':'....... ','tooltip.alconautics.cooler_running':'Статусъ холодильника: Работаетъ','tooltip.alconautics.cooler_idle':'Статусъ холодильника: Не работаетъ'})
ru.update({'tooltip.alconautics.drink_type':'Типъ: %s','drink_kind.alconautics.sugar':'Сахарный','drink_kind.alconautics.fruit':'Фруктовый','drink_kind.alconautics.vegetable':'Овощной'})
en.update({'tooltip.alconautics.drink_type':'Type: %s','drink_kind.alconautics.sugar':'Sugar','drink_kind.alconautics.fruit':'Fruit','drink_kind.alconautics.vegetable':'Vegetable'})
en.update({'tooltip.alconautics.mash':'Mash','tooltip.alconautics.mash_dots':'........ ','tooltip.alconautics.heat':'Heat','tooltip.alconautics.heat_dots':'........ ','tooltip.alconautics.cooler_running':'Condenser status: Running','tooltip.alconautics.cooler_idle':'Condenser status: Not running'})
def modern(text):
    text=text.translate(str.maketrans({'ѣ':'е','Ѣ':'Е','і':'и','І':'И','ѳ':'ф','Ѳ':'Ф','ѵ':'и','Ѵ':'И'}))
    text=re.sub(r'ъ\b','',text)
    return text.replace('перегоннаго','перегонного').replace('соединённыя','соединённые')
write('assets/alconautics/lang/rpr.json',ru)
write('assets/alconautics/lang/ru_ru.json',{k:modern(v) for k,v in ru.items()})
write('assets/alconautics/lang/en_us.json',en)
# New labels apply to all variants: strength/duration belongs to the effect
# tooltip, just like vanilla/Create potions, rather than the liquid's name.
for kind,var in drinks:
    for suffix in ['', '_vapour']:
        ru['fluid.alconautics.'+kind+'_moonshine'+var+suffix]=('Паръ: ' if suffix else '')+{'sugar':'Сахарный','fruit':'Фруктовый','vegetable':'Овощной','grain':'Зерновой'}[kind]+' самогонъ'
        en['fluid.alconautics.'+kind+'_moonshine'+var+suffix]=kind.title()+' Moonshine'+(' Vapour' if suffix else '')
ru.update({'block.alconautics.condenser_base':'Конденсаторъ','tooltip.alconautics.cooler':'Конденсаторъ',
    'tooltip.alconautics.cooler_running':'Статусъ конденсатора: Работаетъ','tooltip.alconautics.cooler_idle':'Статусъ конденсатора: Не работаетъ',
    'tooltip.alconautics.status_header':'Статусъ перегоннаго аппарата','tooltip.alconautics.kind_label':'Типъ: ',
    })
ru.update({'effect.alconautics.courage':'Куражъ','drink_kind.alconautics.grain':'Зерновой','kind.alconautics.grain':'Зерновая',
    'modifier.alconautics.long':'Подорожникъ — дольше','modifier.alconautics.strong':'Крапива — сильнѣе'})
en.update({'effect.alconautics.courage':'High Spirits','drink_kind.alconautics.grain':'Grain','kind.alconautics.grain':'Grain',
    'modifier.alconautics.long':'Ribwort — longer','modifier.alconautics.strong':'Nettle — stronger'})
ru.update({'jei.alconautics.distilling':'Перегонка и охлажденіе','jei.alconautics.condensing':'Конденсація',
    'jei.alconautics.heat':'Обычный нагрѣвъ','jei.alconautics.superheat':'Супернагрѣвъ','jei.alconautics.rate':'100 мБ за %s с',
    'jei.alconautics.whistle':'Установите паровой свистокъ','jei.alconautics.wet_pipes':'Залейте водой двѣ трубы',
    'jei.alconautics.cooling':'Охлажденіе снѣгомъ или льдомъ','jei.alconautics.coolant_credit':'Неизрасходованный запасъ холода сохраняется.'})
en.update({'jei.alconautics.distilling':'Distillation and Cooling','jei.alconautics.condensing':'Condensation',
    'jei.alconautics.heat':'Heated','jei.alconautics.superheat':'Superheated','jei.alconautics.rate':'100 mB every %s s',
    'jei.alconautics.whistle':'Attach a Steam Whistle','jei.alconautics.wet_pipes':'Two waterlogged pipes',
    'jei.alconautics.cooling':'Cool with snow or ice','jei.alconautics.coolant_credit':'Unused cooling capacity is retained.'})
ru['block.alconautics.moonshine_bottle']='Бутылочка самогона'
ru['tooltip.alconautics.sea_pickle']='Снижаетъ опьянѣніе на одну ступень, но не ниже I уровня'
en['block.alconautics.moonshine_bottle']='Moonshine Bottle'
en['tooltip.alconautics.sea_pickle']='Lowers intoxication by one level, but never below level I'
for kind,var in drinks:
    ru['item.alconautics.'+kind+'_moonshine'+var]='Бутылочка самогона'
    en['item.alconautics.'+kind+'_moonshine'+var]='Moonshine Bottle'
en.update({'block.alconautics.condenser_base':'Condenser','tooltip.alconautics.cooler':'Condenser',
    'tooltip.alconautics.status_header':'Distillation apparatus status','tooltip.alconautics.kind_label':'Type: ',
    })
write('assets/alconautics/lang/rpr.json',ru)
write('assets/alconautics/lang/ru_ru.json',{k:modern(v) for k,v in ru.items()})
write('assets/alconautics/lang/en_us.json',en)
write('data/c/tags/fluid/ethanol.json',{'replace':False,'values':['alconautics:'+prefix+kind+'_moonshine'+var for kind,var in drinks for prefix in ['', 'flowing_']]})
for effect,source in [('growth','growth_icon.png'),('shrinking','shrinking_icon.png'),('courage','courage_icon.png')]:
    folder=R/'assets/alconautics/textures/mob_effect';folder.mkdir(parents=True,exist_ok=True)
    shutil.copy2(R.parents[2]/'art/source'/source,folder/f'{effect}.png')
ponder=json.loads((R.parents[2]/'tools/ponder_lang.json').read_text(encoding='utf-8'))
for data in ponder.values():
    for key in ['header','rpr_header']:
        data[key]=data[key].replace('холодильникъ','конденсаторъ').replace('холодильник','конденсатор')
    for key in ['texts','rpr_texts']:
        data[key]=[s.replace('холодильникомъ','конденсаторомъ').replace('холодильником','конденсатором').replace('холодильника','конденсатора').replace('холодильником','конденсатором').replace('холодильникъ','конденсаторъ').replace('холодильник','конденсатор') for s in data[key]]
story=(R.parents[2]/'src/main/java/dev/alconautics/client/AlconauticsPonder.java').read_text(encoding='utf-8')
for locale in ['en_us','ru_ru','rpr']:
    path=R/f'assets/alconautics/lang/{locale}.json'
    lang=json.loads(path.read_text(encoding='utf-8'))
    for ident,data in ponder.items():
        body=story.split(f's.title("{ident}","',1)[1]
        header=body.split('"',1)[0]
        body=body.split('s.markAsFinished();',1)[0]
        english=re.findall(r'text\(s,u,"([^"]+)"',body)
        texts=english if locale=='en_us' else data['rpr_texts' if locale=='rpr' else 'texts']
        assert len(english)==len(texts)
        prefix=f'alconautics.ponder.{ident}.'
        lang[prefix+'header']=header if locale=='en_us' else data['rpr_header' if locale=='rpr' else 'header']
        for i,text in enumerate(texts,1):lang[prefix+f'text_{i}']=text
    write(f'assets/alconautics/lang/{locale}.json',lang)
print('Generated models, recipes and localization.')
