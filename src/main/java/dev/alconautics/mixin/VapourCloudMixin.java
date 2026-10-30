package dev.alconautics.mixin;

import dev.alconautics.VapourCloud;
import net.minecraft.world.entity.AreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only our clouds skip vanilla's planar client particles; server effects still tick. */
@Mixin(AreaEffectCloud.class)
public abstract class VapourCloudMixin {
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void alconautics$volumeParticles(CallbackInfo ci) {
        var cloud=(AreaEffectCloud)(Object)this;
        if(cloud.level().isClientSide && VapourCloud.isOurs(cloud)) ci.cancel();
    }
}
