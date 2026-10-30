package dev.alconautics.compat;

import dev.alconautics.*;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Loaded only with Sable; uses the same collision dispatch as Sable TNT. */
public final class SableBottleBlock extends BottleBlock implements BlockWithSubLevelCollisionCallback {
    public SableBottleBlock(Properties properties) { super(properties); }
    private static final BlockSubLevelCollisionCallback CALLBACK=new FragileBlockCallback() {
        @Override public double getTriggerVelocity() { return 3; }
        @Override public boolean shouldTriggerFor(BlockState state) { return state.getBlock() instanceof SableBottleBlock; }
        @Override public CollisionResult onHit(ServerLevel level,BlockPos pos,BlockState state,Vector3d motion) {
            var plot=SubLevelContainer.getContainer(level).getPlot(new ChunkPos(pos));
            if(plot==null) return CollisionResult.NONE;
            Vec3 center=plot.getSubLevel().logicalPose().transformPosition(Vec3.atCenterOf(pos));
            // Consume the contents before removing the physical block: no intact bottle drops.
            if(level.getBlockEntity(pos) instanceof BottleBE bottles) bottles.clearContent();
            level.removeBlock(pos,false);
            level.getServer().execute(() -> VapourCloud.burstBottle(level,center));
            return new CollisionResult(new Vector3d(),true);
        }
    };
    @Override public BlockSubLevelCollisionCallback sable$getCallback() { return CALLBACK; }
}
