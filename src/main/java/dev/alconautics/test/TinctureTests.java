package dev.alconautics.test;
import dev.alconautics.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

@GameTestHolder("alconautics") @PrefixGameTestTemplate(false)
public final class TinctureTests {
    private static final BlockPos POS=new BlockPos(2,2,2);
    private static ItemStack item(Tincture kind) { return Content.TINCTURE_ITEMS.get(kind).get().getDefaultInstance(); }
    private static BottleBE place(GameTestHelper h,Tincture first) {
        h.setBlock(POS,Blocks.AIR);h.setBlock(POS,Content.TINCTURE_BOTTLES.get());var be=(BottleBE)h.getBlockEntity(POS);be.placeFirst(item(first));return be;
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void tincture_capacity(GameTestHelper h) {
        var be=place(h,Tincture.DANDELION);
        h.assertTrue(be.addBottle(item(Tincture.DANDELION)) && be.addBottle(item(Tincture.DANDELION)),"Three dandelion bottles should fit");
        h.assertTrue(be.getBlockState().getValue(BottleBlock.COUNT)==3 && !be.addBottle(item(Tincture.DANDELION)) && !be.addBottle(item(Tincture.BERRY)),"A triple must reject all further bottles");
        be=place(h,Tincture.BERRY);
        h.assertTrue(be.addBottle(item(Tincture.BERRY)) && !be.addBottle(item(Tincture.BERRY)) && !be.addBottle(item(Tincture.DANDELION)),"Berry capacity must be two");
        for(var first:Tincture.values()) {
            be=place(h,first);var second=first==Tincture.BERRY?Tincture.DANDELION:Tincture.BERRY;
            h.assertTrue(be.addBottle(item(second)) && TinctureGeometry.layout(be.getInventory())==5,"Mixed bottles must work in either order");
            h.assertTrue(!be.addBottle(item(Tincture.BERRY)) && !be.addBottle(item(Tincture.DANDELION)),"Mixed pair must be full");
            h.assertTrue(!be.addBottle(Content.DRINKS.values().iterator().next().get().getDefaultInstance()),"Moonshine cannot be inserted into a tincture layout");
        }
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void tincture_age_and_storage(GameTestHelper h) {
        var stack=item(Tincture.DANDELION);long now=TinctureAge.now(h.getLevel()),year=Config.TINCTURE_DAYS_PER_YEAR.get()*24000L;
        TinctureAge.update(stack,now-5*year);TinctureAge.update(stack,h.getLevel());
        h.assertTrue(TinctureAge.years(stack)==5 && TinctureAge.amplifier(stack)==1,"Five-year tincture must gain level II");
        var fresh=item(Tincture.DANDELION);TinctureAge.update(fresh,999999999L);
        h.assertTrue(TinctureAge.years(fresh)==0,"New bottles must not inherit the world's age");
        TinctureAge.update(fresh,999999999L+24000L);
        h.assertTrue(TinctureAge.years(fresh)==0 && TinctureAge.daysUntilNextLevel(fresh)==Config.TINCTURE_DAYS_PER_YEAR.get()*5-1,"Days until improvement must advance before a full year passes");
        h.assertTrue(TinctureAge.daysUntilNextLevel(stack)==-1,"Fully matured tinctures have no next effect level");
        var moonshine=Content.DRINKS.values().iterator().next().get().getDefaultInstance();TinctureAge.update(moonshine,999999999L);
        h.assertTrue(!moonshine.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA),"Moonshine must never acquire aging data");
        var be=place(h,Tincture.BERRY);be.addBottle(stack);
        var restored=new BottleBE(be.getBlockPos(),be.getBlockState());restored.loadWithComponents(be.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(TinctureAge.years(restored.getInventory().get(1))==5,"Age lost during save/load");
        var taken=be.takeBottle(1);h.assertTrue(TinctureAge.years(taken)==5 && be.getInventory().getFirst().getItem() instanceof TinctureItem item && item.kind==Tincture.BERRY,"Taking a mixed bottle must preserve its individual age and remaining kind");
        var zombie=h.spawn(EntityType.COW,new BlockPos(1,2,1));
        ((TinctureItem)taken.getItem()).finishUsingItem(taken,h.getLevel(),zombie);
        h.assertTrue(zombie.hasEffect(MobEffects.REGENERATION) && zombie.getEffect(MobEffects.REGENERATION).getAmplifier()==1,"Aged tincture must apply its displayed effect");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void tincture_recipes(GameTestHelper h) {
        var recipe=(ProcessingRecipe<?,?>)h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("alconautics:mixing/berry_tincture")).orElseThrow().value();
        for(var id:new String[]{"minecraft:sweet_berries","minecraft:glow_berries","farm_and_charm:strawberry"}) {
            var berry=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            h.assertTrue(recipe.getIngredients().stream().filter(ingredient -> ingredient.test(new ItemStack(berry))).count()==2,"Berry infusion must accept "+id);
        }
        for(var fluid:Content.SPIRITS.values()) h.assertTrue(recipe.getFluidIngredients().getFirst().test(fluid.stack(250)),"All moonshine varieties must be valid bases");
        for(var fluid:Content.MASH.values()) h.assertTrue(!recipe.getFluidIngredients().getFirst().test(fluid.stack(250)),"Mash must not replace finished moonshine");
        var dandelion=(ProcessingRecipe<?,?>)h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("alconautics:mixing/dandelion_tincture")).orElseThrow().value();
        h.assertTrue(dandelion.getIngredients().size()==6 && dandelion.getIngredients().stream().filter(i -> i.test(new ItemStack(Items.DANDELION))).count()==4 && dandelion.getIngredients().stream().filter(i -> i.test(new ItemStack(Items.SUGAR))).count()==2,"Dandelion infusion requires four flowers and two sugar");
        h.assertTrue(dandelion.getFluidIngredients().getFirst().amount()==250 && dandelion.getFluidResults().getFirst().getAmount()==250,"Dandelion infusion consumes 250 mB moonshine and produces 250 mB tincture");
        var filling=(ProcessingRecipe<?,?>)h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("alconautics:filling/dandelion_tincture")).orElseThrow().value();
        var made=filling.rollResults(h.getLevel().random).getFirst();
        h.assertTrue(made.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).contains(TinctureAge.BORN),"Automatic filling must stamp the actual bottling date before storage");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void tincture_selection_and_compaction(GameTestHelper h) {
        var be=place(h,Tincture.DANDELION);be.addBottle(item(Tincture.DANDELION));be.addBottle(item(Tincture.DANDELION));
        var brewery=net.minecraft.core.registries.BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof net.satisfy.brewery.core.block.BeverageBlock).findFirst().orElseThrow();
        var nativeBounds=brewery.defaultBlockState().getShape(h.getLevel(),be.getBlockPos()).bounds();
        for(int layout=0;layout<6;layout++) for(var facing:Direction.Plane.HORIZONTAL) {
            var state=be.getBlockState().setValue(BottleBlock.DRINK,layout).setValue(BottleBlock.FACING,facing);
            h.assertTrue(state.getShape(h.getLevel(),be.getBlockPos()).bounds().equals(nativeBounds),"Every tincture arrangement must use Brewery's shared beverage hitbox");
        }
        for(var facing:Direction.Plane.HORIZONTAL) {
            for(int part=0;part<3;part++) {
                var c=TinctureGeometry.CENTERS[2][part];double x=c[0],z=c[1];int turn=switch(facing) {case EAST -> 1;case SOUTH -> 2;case WEST -> 3;default -> 0;};
                for(int i=0;i<turn;i++) { double old=x;x=1-z;z=old; }
                int slot=TinctureGeometry.selectedSlot(be.getInventory(),facing,be.getBlockPos(),net.minecraft.world.phys.Vec3.atLowerCornerOf(be.getBlockPos()).add(x,.3,z));
                h.assertTrue(slot==part,"Rotated triple must select the bottle under the crosshair");
            }
        }
        be.takeBottle(1);h.assertTrue(be.getBlockState().getValue(BottleBlock.COUNT)==2 && be.getInventory().get(2).isEmpty(),"Removing the middle bottle must compact the remaining pair");
        be.takeBottle(0);h.assertTrue(TinctureGeometry.layout(be.getInventory())==0,"Remaining bottle must use the centered single model");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void brewery_barrel_tinctures(GameTestHelper h) {
        var state=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(ResourceLocation.parse("brewery:barrel_main")).defaultBlockState();
        var barrel=new net.satisfy.brewery.core.block.entity.BigBarrelBlockEntity(h.absolutePos(POS),state);barrel.setLevel(h.getLevel());
        long now=TinctureAge.now(h.getLevel()),cycle=net.satisfy.brewery.core.block.entity.BigBarrelBlockEntity.TICKS_PER_QUALITY;
        var d=item(Tincture.DANDELION);TinctureAge.update(d,now-cycle);
        var b=item(Tincture.BERRY);TinctureAge.update(b,now-cycle/2);
        h.assertTrue(barrel.insert(d) && d.isEmpty() && barrel.insert(b) && b.isEmpty(),"Native Brewery barrel must accept and consume either tincture");
        h.assertTrue(!barrel.insert(Content.DRINKS.values().iterator().next().get().getDefaultInstance()),"Moonshine must not gain barrel aging");
        var saved=barrel.saveWithFullMetadata(h.getLevel().registryAccess());
        saved.putLongArray("StoredAt",new long[]{h.getLevel().getGameTime()-cycle,h.getLevel().getGameTime()-cycle/2});
        var restored=new net.satisfy.brewery.core.block.entity.BigBarrelBlockEntity(h.absolutePos(POS),state);restored.setLevel(h.getLevel());restored.loadWithComponents(saved,h.getLevel().registryAccess());
        h.assertTrue(restored.getQuality(0)==4 && restored.getAgingPercent(0)==100,"Seven days in a barrel must fully mature a fresh tincture and show the native completed overlay");
        h.assertTrue(restored.getQuality(1)==3 && restored.getAgingPercent(1)==50,"Half a barrel cycle must show half progress");
        var half=restored.takeLast();h.assertTrue(TinctureAge.years(half)==2 && TinctureAge.daysUntilNextLevel(half)==Config.TINCTURE_DAYS_PER_YEAR.get()*5/2,"Partial barrel aging must survive extraction");
        var all=restored.takeAll();var mature=all.stream().filter(s -> !s.isEmpty()).findFirst().orElseThrow();
        h.assertTrue(TinctureAge.years(mature)==5 && TinctureAge.amplifier(mature)==1 && restored.isEmpty(),"Bulk extraction must preserve mature tinctures");
        h.assertTrue(!mature.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).contains("brewery.beer_quality"),"Tinctures must retain their own aging data instead of beer quality");
        var taken=half.copy();TinctureAge.update(taken,now);h.assertTrue(TinctureAge.years(taken)==TinctureAge.years(half),"Inspecting a removed bottle must not apply barrel aging twice");
        var beer=net.minecraft.core.registries.BuiltInRegistries.ITEM.stream().filter(i -> i instanceof net.satisfy.brewery.core.item.DrinkBlockItem).findFirst().orElseThrow().getDefaultInstance();
        net.satisfy.brewery.core.item.DrinkBlockItem.addQuality(beer,2);h.assertTrue(barrel.insert(beer),"Brewery drinks must still be accepted");
        h.assertTrue(net.satisfy.brewery.core.item.DrinkBlockItem.getQuality(barrel.takeLast())==2,"Brewery beer quality must remain unchanged by tincture integration");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void cannon_drinks(GameTestHelper h) {
        if(net.neoforged.fml.ModList.get().isLoaded("createbigcannons")) BigCannonsCheck.run(h);else h.succeed();
    }
}
