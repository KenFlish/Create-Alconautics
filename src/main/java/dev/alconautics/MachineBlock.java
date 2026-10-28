package dev.alconautics;

import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

public class MachineBlock extends Block implements EntityBlock, com.simibubi.create.content.equipment.wrench.IWrenchable {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
    public enum Kind { HEAD, COOLER, DRY }
    public final Kind kind;
    public MachineBlock(Properties props, Kind kind) { super(props); this.kind = kind; registerDefaultState(stateDefinition.any().setValue(FACING, Direction.EAST)); }
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    public static boolean isHeadOutlet(BlockState state, @Nullable Direction side) { return side != null && (side == Direction.UP || side.getAxis() == state.getValue(FACING).getAxis()); }
    @Override public BlockState getRotatedBlockState(BlockState state, Direction clickedFace) { return state.setValue(FACING,state.getValue(FACING).getClockWise()); }
    @Override public InteractionResult onWrenched(BlockState state, net.minecraft.world.item.context.UseOnContext context) {
        var level=context.getLevel();var pos=context.getClickedPos();
        if(!level.isClientSide) {
            if(level.getBlockEntity(pos) instanceof HeadBE head) head.dropContents();
            level.setBlockAndUpdate(pos,getRotatedBlockState(state,Direction.UP));
            if(level.getBlockEntity(pos) instanceof MachineBE be) { be.invalidateCapabilities(); be.changed(); }
            level.updateNeighborsAt(pos,this);
            com.simibubi.create.content.equipment.wrench.IWrenchable.playRotateSound(level,pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) { case HEAD -> new HeadBE(pos,state); case COOLER -> new CoolerBE(pos,state); case DRY -> new DryBE(pos,state); };
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (l,p,s,be) -> { if (be instanceof MachineBE m) m.tick(); };
    }
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (kind == Kind.HEAD) {
            if (!(ctx.getLevel().getBlockEntity(ctx.getClickedPos().below()) instanceof FluidTankBlockEntity tank)) return null;
            var controller = tank.getControllerBE();
            if (controller == null || ctx.getClickedPos().getY() != controller.getBlockPos().getY() + controller.getHeight()) return null;
            if (HeadBE.find(controller) != null) return null;
        }
        return defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof MachineBE be && be.interact(player, hand)) return ItemInteractionResult.sidedSuccess(level.isClientSide);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return level.getBlockEntity(pos) instanceof MachineBE be && be.interact(player, InteractionHand.MAIN_HAND) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof MachineBE be) be.dropContents();
        super.onRemove(state, level, pos, next, moving);
    }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return switch (kind) { case DRY -> ModelShapes.DRY; case HEAD -> ModelShapes.head(s.getValue(FACING)); case COOLER -> Shapes.block(); };
    }
}
