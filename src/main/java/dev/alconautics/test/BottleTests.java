package dev.alconautics.test;

import dev.alconautics.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("alconautics")
@PrefixGameTestTemplate(false)
public final class BottleTests {
    private static final BlockPos POS=new BlockPos(2,2,2);
    private static ItemStack bottle(Drink.Kind kind,Drink.Variant variant) { return Content.DRINKS.get(new Drink(kind,variant)).get().getDefaultInstance(); }
    private static BottleBE setup(GameTestHelper h,ItemStack first) {
        h.setBlock(POS.below(),Blocks.STONE);h.setBlock(POS,Content.BOTTLE.get());
        BottleBE be=h.getBlockEntity(POS);be.placeFirst(first);return be;
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void mixed_bottles_round_trip(GameTestHelper h) {
        ItemStack first=bottle(Drink.Kind.FRUIT,Drink.Variant.STRONG),second=bottle(Drink.Kind.SUGAR,Drink.Variant.LONG);
        first.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Saved bottle"));
        BottleBE be=setup(h,first);
        h.assertTrue(be.addBottle(second),"Mixed bottles must fit together");
        h.assertTrue(h.getBlockState(POS).getValue(BottleBlock.COUNT)==2,"Pair state must show two bottles");
        h.assertTrue(!be.addBottle(bottle(Drink.Kind.VEGETABLE,Drink.Variant.NORMAL)),"A third bottle must be rejected");
        var copy=new BottleBE(be.getBlockPos(),be.getBlockState());
        copy.loadWithComponents(be.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(ItemStack.isSameItemSameComponents(first,copy.getInventory().get(0)),"First item components were lost");
        h.assertTrue(ItemStack.isSameItemSameComponents(second,copy.getInventory().get(1)),"Second drink variant was lost");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void old_bottles_migrate(GameTestHelper h) {
        h.setBlock(POS.below(),Blocks.STONE);
        for(int index=0;index<Drink.ALL.size();index++) {
            h.setBlock(POS,Blocks.AIR);
            h.setBlock(POS,Content.BOTTLE.get().defaultBlockState().setValue(BottleBlock.DRINK,index));
            BottleBE be=h.getBlockEntity(POS);
            // Simulate an old chunk with only the block state and no entity NBT.
            h.getLevel().removeBlockEntity(h.absolutePos(POS));
            BottleMigration.repair(h.getLevel().getChunkAt(h.absolutePos(POS)));
            be=h.getBlockEntity(POS);
            // No saved entity data existed for bottles from 0.1.5 and earlier.
            h.assertTrue(be.getInventory().get(0).is(Content.DRINKS.get(Drink.ALL.get(index)).get()),"Legacy bottle migrated to wrong variant: "+index);
            h.assertTrue(be.getInventory().get(1).isEmpty(),"Migration must not create a second bottle");
        }
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void placement_and_pair_capacity(GameTestHelper h) {
        h.setBlock(POS.below(),Blocks.STONE);
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setShiftKeyDown(true);
        var first=bottle(Drink.Kind.FRUIT,Drink.Variant.NORMAL);first.setCount(3);
        player.setItemInHand(InteractionHand.MAIN_HAND,first);
        var floor=h.absolutePos(POS.below());
        ((MoonshineItem)first.getItem()).useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false)));
        h.assertTrue(first.getCount()==2,"Placing must consume one item");
        player.setShiftKeyDown(false);
        var point=h.absolutePos(POS);var hit=new BlockHitResult(Vec3.atCenterOf(point),Direction.NORTH,point,false);
        ((MoonshineItem)first.getItem()).useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));
        h.assertTrue(first.getCount()==1 && h.getBlockState(POS).getValue(BottleBlock.COUNT)==2,"Second placement failed");
        ((MoonshineItem)first.getItem()).useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));
        h.assertTrue(first.getCount()==1 && !player.isUsingItem(),"A full pair must neither consume nor drink the third item");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void select_take_and_no_duplicate_drop(GameTestHelper h) {
        var second=bottle(Drink.Kind.FRUIT,Drink.Variant.STRONG);
        BottleBE be=setup(h,bottle(Drink.Kind.SUGAR,Drink.Variant.LONG));be.addBottle(second);
        for(Direction face:Direction.Plane.HORIZONTAL) {
            var s=be.getBlockState().setValue(BottleBlock.FACING,face);double x=5,z=5;
            int turns=switch(face) {case EAST -> 1;case SOUTH -> 2;case WEST -> 3;default -> 0;};
            for(int i=0;i<turns;i++) { double old=x;x=16-z;z=old; }
            h.assertTrue(BottleBlock.selectedSlot(s,be.getBlockPos(),Vec3.atLowerCornerOf(be.getBlockPos()).add(x/16,.4,z/16))==0,"Hit testing must follow rotation");
        }
        h.assertTrue(be.takeBottle(0).is(Content.DRINKS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.LONG)).get()),"Wrong bottle removed");
        h.assertTrue(be.getInventory().get(0).is(second.getItem()) && be.getInventory().get(1).isEmpty(),"Remaining bottle must become the centered single bottle");
        h.assertTrue(h.getBlockState(POS).getValue(BottleBlock.COUNT)==1,"Single model state not restored");
        h.getLevel().destroyBlock(be.getBlockPos(),true);
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(POS)).inflate(1));
        h.assertTrue(drops.stream().mapToInt(e -> e.getItem().getCount()).sum()==1 && drops.getFirst().getItem().is(second.getItem()),"Breaking must drop the remaining variant once");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void support_loss_drops_both(GameTestHelper h) {
        BottleBE be=setup(h,bottle(Drink.Kind.SUGAR,Drink.Variant.NORMAL));be.addBottle(bottle(Drink.Kind.FRUIT,Drink.Variant.NORMAL));
        h.setBlock(POS.below(),Blocks.AIR);
        h.assertTrue(h.getBlockState(POS).isAir(),"Unsupported bottles must be removed");
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(POS)).inflate(1));
        h.assertTrue(drops.stream().mapToInt(e -> e.getItem().getCount()).sum()==2,"Support loss must drop exactly two bottles");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void pickle_is_food_and_lowers_one_level(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.getFoodData().setFoodLevel(20);
        var stack=new ItemStack(Items.SEA_PICKLE,2);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var effect=BreweryCompat.drunkEffect().orElseThrow();player.addEffect(new MobEffectInstance(effect,875,2));
        h.assertTrue(stack.getFoodProperties(player)!=null && stack.getFoodProperties(player).canAlwaysEat(),"Sea pickle must be edible at full hunger");
        Items.SEA_PICKLE.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(player.isUsingItem(),"Sea pickle must start eating in air");
        player.releaseUsingItem();
        h.assertTrue(player.getEffect(effect).getAmplifier()==2 && stack.getCount()==2,"Cancelled eating must not sober or consume");
        var original=stack.copy();
        var result=stack.finishUsingItem(h.getLevel(),player);
        net.neoforged.neoforge.event.EventHooks.onItemUseFinish(player,original,0,result);
        h.assertTrue(stack.getCount()==1,"Eating must consume one pickle");
        h.assertTrue(player.getEffect(effect).getAmplifier()==1 && player.getEffect(effect).getDuration()==875,"Pickle must lower exactly one level and preserve the timer");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void pickle_placement_and_creative_bottles(GameTestHelper h) {
        h.setBlock(POS.below(),Blocks.STONE);
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        var pickles=new ItemStack(Items.SEA_PICKLE,2);player.setItemInHand(InteractionHand.MAIN_HAND,pickles);
        var floor=h.absolutePos(POS.below());
        var floorHit=new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false);
        Items.SEA_PICKLE.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,floorHit));
        h.assertTrue(h.getBlockState(POS).is(Blocks.SEA_PICKLE) && pickles.getCount()==1,"Food component must preserve ordinary block placement");
        h.setBlock(POS,Blocks.AIR);
        var creative=h.makeMockPlayer(GameType.CREATIVE);creative.setShiftKeyDown(true);
        var stack=bottle(Drink.Kind.SUGAR,Drink.Variant.NORMAL);creative.setItemInHand(InteractionHand.MAIN_HAND,stack);
        ((MoonshineItem)stack.getItem()).useOn(new UseOnContext(creative,InteractionHand.MAIN_HAND,floorHit));
        var pos=h.absolutePos(POS);
        ((MoonshineItem)stack.getItem()).useOn(new UseOnContext(creative,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)));
        h.assertTrue(stack.getCount()==1 && h.getBlockState(POS).getValue(BottleBlock.COUNT)==2,"Creative placement must not consume bottles");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void pickle_floor_and_other_effects(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);var effect=BreweryCompat.drunkEffect().orElseThrow();
        player.addEffect(new MobEffectInstance(effect,777,1));player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,500,0));
        BreweryCompat.soberOneLevel(player);BreweryCompat.soberOneLevel(player);
        h.assertTrue(player.getEffect(effect).getAmplifier()==0 && player.getEffect(effect).getDuration()==777,"Pickles must leave intoxication I and its remaining time");
        h.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED),"Pickle must not remove unrelated effects");
        player.removeEffect(effect);BreweryCompat.soberOneLevel(player);
        h.assertTrue(!player.hasEffect(effect),"Eating while sober must not add intoxication");h.succeed();
    }
}
