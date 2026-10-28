package dev.alconautics;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class BottleBlock extends Block {
    public static final IntegerProperty DRINK=IntegerProperty.create("drink",0,6);
    public BottleBlock(Properties props) { super(props); registerDefaultState(stateDefinition.any().setValue(DRINK,0)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(DRINK); }
    private ItemStack item(BlockState state) { return Content.DRINKS.get(Drink.ALL.get(state.getValue(DRINK))).get().getDefaultInstance(); }
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder params) { return List.of(item(state)); }
    @Override public ItemStack getCloneItemStack(LevelReader level,BlockPos pos,BlockState state) { return item(state); }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c) { return Block.box(5,0,5,11,13,11); }
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p) { return l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP); }
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor l,BlockPos p,BlockPos q) {
        if(d==Direction.DOWN && !s.canSurvive(l,p)) { if(l instanceof Level level && !level.isClientSide) Block.popResource(level,p,item(s)); return Blocks.AIR.defaultBlockState(); }
        return super.updateShape(s,d,other,l,p,q);
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit) {
        if(!l.isClientSide) { var stack=item(s); l.removeBlock(pos,false); if(!p.addItem(stack)) p.drop(stack,false); }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
