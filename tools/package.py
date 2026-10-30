"""Package only our mod and editable sources, never third-party dependency JARs."""
from pathlib import Path
import shutil, zipfile, hashlib, json

root=Path(__file__).resolve().parents[1]
output=root/'dist';output.mkdir(exist_ok=True)
jar=root/'build/libs/create-alconautics-0.1.10.jar'
shutil.copy2(jar,output/jar.name)
archive=output/'create-alconautics-0.1.10-sources.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
    for folder in ('src','art/source','gradle','docs'):
        for p in sorted((root/folder).rglob('*')):
            if p.is_file(): z.write(p,p.relative_to(root))
    for name in ('build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','README.md','LICENSE','ASSET-NOTICES.md','dependency-lock.json','.gitignore','.gitattributes'):
        z.write(root/name,name)
    for name in ('bootstrap.py','resources.py','structure.py','package.py','model_shapes.py','ponder.py','ponder_lang.json','tinctures.py','model_parts.py'):
        z.write(root/'tools'/name,'tools/'+name)
with zipfile.ZipFile(jar) as z:
    assert sum(n.startswith('assets/alconautics/ponder/') and n.endswith('.nbt') for n in z.namelist())==2, 'Two Ponder structures required'
    assert not any(n.endswith(('.ogg','.mp3','.wav','.onnx')) or 'moonshiners' in n for n in z.namelist()), 'Removed music must not be bundled'
    assert not any('dev/alconautics/test/' in n for n in z.namelist()), 'Tests included in release'
    assert all(not n.endswith('.bbmodel') for n in z.namelist()), 'Source artwork included in release'
    assert all('assets/alconautics/models/block/'+name+'.obj' in z.namelist() for name in ('moonshine_bottle_single','moonshine_bottle_pair_0','moonshine_bottle_pair_1')), 'Bottle models missing'
    assert all('assets/alconautics/models/block/'+name+'.obj' in z.namelist() for name in ('dandelion_tincture_single','dandelion_tincture_pair_0','dandelion_tincture_pair_1','dandelion_tincture_triple_0','dandelion_tincture_triple_1','dandelion_tincture_triple_2','berry_tincture_single','berry_tincture_pair_0','berry_tincture_pair_1','tincture_mixed_0','tincture_mixed_1')), 'Tincture geometry missing'
    assert not any(n.endswith('.jar') for n in z.namelist()), 'Dependency JARs must not be embedded'
    for kind in ('berry','dandelion'):
        assert z.read(f'assets/alconautics/textures/item/{kind}_tincture.png') == (root/'art/source'/f'{kind}_tincture_item.png').read_bytes(), 'Supplied tincture item texture changed'
        assert json.loads(z.read(f'assets/alconautics/models/item/{kind}_tincture.json'))['parent']=='minecraft:item/generated', 'Tincture item must be 2D'
    for effect in ('courage','shrinking'):
        assert z.read('assets/alconautics/textures/mob_effect/'+effect+'.png') == (root/'art/source'/f'{effect}_icon.png').read_bytes(), 'Effect icon differs from supplied artwork'
    assert 'assets/minecraft/atlases/mob_effects.json' not in z.namelist(), 'Old Courage icon override must not return'
    assert 'assets/alconautics/jei/distillery.nbt' in z.namelist(), 'User JEI assembly missing'
    assert not any('CondensingCategory' in n for n in z.namelist()), 'Separate condenser recipe must not return'
    assert len(json.loads(z.read('data/c/tags/fluid/ethanol.json'))['values']) == 20, 'All finished moonshine fluids must be fuel'
    assert json.loads(z.read('data/alconautics/tags/item/grains.json'))['values'] == ['minecraft:wheat','farm_and_charm:oat','farm_and_charm:barley'], 'Grain ingredients missing'
(output/'SHA256SUMS.txt').write_text('\n'.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name for p in (output/jar.name,archive))+'\n',encoding='utf-8')
print('Packaged:',jar.name,archive.name)
