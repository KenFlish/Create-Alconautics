package dev.alconautics.mixin;

import dev.alconautics.HeadBE;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WhistleBlockEntity.class)
public abstract class WhistleMixin {
    @Shadow public abstract FluidTankBlockEntity getTank();
    @Inject(method="isPowered",at=@At("RETURN"),cancellable=true)
    private void alconautics$power(CallbackInfoReturnable<Boolean> ci) {
        if(ci.getReturnValueZ()) return;
        var head=HeadBE.find(getTank());
        if(head!=null && head.isWhistling()) ci.setReturnValue(true);
    }
}
