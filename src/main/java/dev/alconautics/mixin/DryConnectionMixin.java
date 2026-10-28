package dev.alconautics.mixin;

import dev.alconautics.Content;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(com.simibubi.create.content.fluids.pipes.FluidPipeBlock.class)
public abstract class DryConnectionMixin {
    @Inject(method="canConnectTo",at=@At("HEAD"),cancellable=true)
    private static void alconautics$dryTap(BlockAndTintGetter level,BlockPos neighbourPos,BlockState neighbour,Direction direction,CallbackInfoReturnable<Boolean> ci) {
        if(neighbour.is(Content.DRY.get())) ci.setReturnValue(direction==Direction.DOWN);
    }
}
