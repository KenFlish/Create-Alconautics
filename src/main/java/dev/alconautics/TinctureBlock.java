package dev.alconautics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.*;
public final class TinctureBlock extends BottleBlock {
    public TinctureBlock(Properties properties) { super(properties); }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        int layout=Math.min(5,state.getValue(DRINK));
        return TinctureGeometry.shape(layout,state.getValue(FACING));
    }
    @Override public ItemStack getCloneItemStack(LevelReader level,BlockPos pos,BlockState state) {
        if(level.getBlockEntity(pos) instanceof BottleBE bottles && !bottles.getInventory().getFirst().isEmpty()) return bottles.getInventory().getFirst().copy();
        return Content.TINCTURE_ITEMS.get(Tincture.DANDELION).get().getDefaultInstance();
    }
}
