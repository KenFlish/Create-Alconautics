package dev.alconautics;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

public class BottleBlock extends Block implements EntityBlock {
    /** Retained to migrate bottles placed by releases up to 0.1.5. */
    public static final IntegerProperty DRINK=IntegerProperty.create("drink",0,9);
    public static final IntegerProperty COUNT=IntegerProperty.create("bottles",1,3);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public BottleBlock(Properties props) { super(props);registerDefaultState(stateDefinition.any().setValue(DRINK,0).setValue(COUNT,1).setValue(FACING,Direction.NORTH)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(DRINK,COUNT,FACING); }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new BottleBE(pos,state); }
    @Override public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level,BlockState state,net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return !level.isClientSide && type==Content.BOTTLE_BE.get()?(world,pos,s,be) -> { if(world.getGameTime()%200==0) ((BottleBE)be).ageBottles(); }:null;
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override protected BlockState rotate(BlockState s,Rotation r) { return s.setValue(FACING,r.rotate(s.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState s,Mirror m) { return s.rotate(m.getRotation(s.getValue(FACING))); }
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder p) { return List.of(); }
    @Override public ItemStack getCloneItemStack(LevelReader l,BlockPos p,BlockState s) {
        if(l.getBlockEntity(p) instanceof BottleBE bottles && !bottles.getInventory().getFirst().isEmpty()) return bottles.getInventory().getFirst().copy();
        return Content.DRINKS.get(Drink.ALL.get(s.getValue(DRINK))).get().getDefaultInstance();
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c) { return BottleShapes.get(Math.min(2,s.getValue(COUNT)),s.getValue(FACING)); }
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p) { return true; }
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor l,BlockPos p,BlockPos q) {
        return super.updateShape(s,d,other,l,p,q);
    }
    public static boolean mayUse(Level level,BlockPos pos,Player player,Direction face,ItemStack stack) {
        return level.mayInteract(player,pos) && player.mayUseItemAt(pos,face,stack);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit) {
        if(!(stack.getItem() instanceof MoonshineItem) && !(stack.getItem() instanceof TinctureItem)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!mayUse(l,pos,p,hit.getDirection(),stack)) return ItemInteractionResult.FAIL;
        if(l.getBlockEntity(pos) instanceof BottleBE bottles && !l.isClientSide && bottles.addBottle(stack)) {
            if(!p.isCreative()) stack.shrink(1);
            placedSound(l,pos);
        }
        return ItemInteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit) {
        if(!p.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if(!mayUse(l,pos,p,hit.getDirection(),ItemStack.EMPTY)) return InteractionResult.FAIL;
        if(!l.isClientSide && l.getBlockEntity(pos) instanceof BottleBE bottles) {
            ItemStack result=bottles.takeBottle(this instanceof TinctureBlock?TinctureGeometry.selectedSlot(bottles.getInventory(),s.getValue(FACING),pos,hit.getLocation()):selectedSlot(s,pos,hit.getLocation()));
            if(!result.isEmpty() && !p.addItem(result)) p.drop(result,false);
            l.playSound(null,pos,net.minecraft.sounds.SoundEvents.BOTTLE_EMPTY,net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    public static void placedSound(Level level,BlockPos pos) {
        level.playSound(null,pos,net.minecraft.sounds.SoundEvents.BOTTLE_FILL,net.minecraft.sounds.SoundSource.BLOCKS,1,1);
    }
    public static int selectedSlot(BlockState state,BlockPos pos,Vec3 hit) {
        if(state.getValue(COUNT)==1) return 0;
        double x=(hit.x-pos.getX())*16,z=(hit.z-pos.getZ())*16;
        int turns=switch(state.getValue(FACING)) {case EAST -> 1;case SOUTH -> 2;case WEST -> 3;default -> 0;};
        for(int i=0;i<turns;i++) { double old=x;x=z;z=16-old; }
        var centers=BottleShapes.CENTERS[1];
        return Math.pow(x/16-centers[0][0],2)+Math.pow(z/16-centers[0][1],2)<=Math.pow(x/16-centers[1][0],2)+Math.pow(z/16-centers[1][1],2)?0:1;
    }
    @Override protected void onRemove(BlockState old,Level level,BlockPos pos,BlockState next,boolean moving) {
        if(!old.is(next.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof BottleBE bottles) {
            for(ItemStack stack:bottles.getInventory()) if(!stack.isEmpty()) Block.popResource(level,pos,stack.copy());
            bottles.clearContent();
        }
        super.onRemove(old,level,pos,next,moving);
    }
}
