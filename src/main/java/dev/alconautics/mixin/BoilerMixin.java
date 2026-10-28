package dev.alconautics.mixin;

import dev.alconautics.HeadBE;
import com.simibubi.create.content.fluids.tank.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoilerData.class)
public abstract class BoilerMixin {
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void alconautics$tick(FluidTankBlockEntity tank,CallbackInfo ci) {
        if(HeadBE.find(tank)!=null) { if(tank.getLevel().isClientSide) tank.boiler.gauge.tickChaser(); ci.cancel(); }
    }
}
