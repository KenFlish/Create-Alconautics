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
        var filling=(ProcessingRecipe<?,?>)h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("alconautics:filling/dandelion_tincture")).orElseThrow().value();
        var made=filling.rollResults(h.getLevel().random).getFirst();
        h.assertTrue(made.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).contains(TinctureAge.BORN),"Automatic filling must stamp the actual bottling date before storage");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void tincture_selection_and_compaction(GameTestHelper h) {
        var be=place(h,Tincture.DANDELION);be.addBottle(item(Tincture.DANDELION));be.addBottle(item(Tincture.DANDELION));
        h.assertTrue(be.getBlockState().getValue(BottleBlock.DRINK)==2,"Triple layout must be encoded in the block state for Minecraft's shape cache");
        h.assertTrue(be.getBlockState().getShape(h.getLevel(),be.getBlockPos()).bounds().equals(TinctureGeometry.shape(2,Direction.NORTH).bounds()),"Cached collision bounds must match all three bottles");
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
    public static void cannon_drinks(GameTestHelper h) {
        if(net.neoforged.fml.ModList.get().isLoaded("createbigcannons")) BigCannonsCheck.run(h);else h.succeed();
    }
}
