package dev.alconautics.mixin;

import dev.alconautics.HeadBE;
import com.simibubi.create.content.fluids.tank.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidTankRenderer.class)
public abstract class TankRendererMixin {
    @Shadow protected abstract void renderAsBoiler(FluidTankBlockEntity be,float ticks,PoseStack pose,MultiBufferSource buffer,int light,int overlay);
    @Inject(method="renderSafe(Lcom/simibubi/create/content/fluids/tank/FluidTankBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",at=@At("HEAD"),cancellable=true)
    private void alconautics$gauge(FluidTankBlockEntity be,float ticks,PoseStack pose,MultiBufferSource buffer,int light,int overlay,CallbackInfo ci) {
        var head=HeadBE.find(be);
        if(head!=null && be.isController()) {
            java.util.Arrays.fill(be.boiler.occludedDirections,false);
            renderAsBoiler(be,ticks,pose,buffer,light,overlay);
            ci.cancel();
        }
    }
}
