package dev.alconautics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.alconautics.DryBE;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;

/** The synchronized inventory is visible through the supplied glass jar model. */
public final class DryRenderer implements BlockEntityRenderer<DryBE> {
    private final ItemRenderer items;
    public DryRenderer(BlockEntityRendererProvider.Context context) { items=context.getItemRenderer(); }
    @Override public void render(DryBE jar,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var stack=jar.items.getStackInSlot(0);
        if(stack.isEmpty()) return;
        pose.pushPose();
        pose.translate(.5,.38,.5);
        pose.mulPose(Axis.YP.rotationDegrees(45));
        pose.scale(.48f,.48f,.48f);
        // Two crossed faces remain legible through the glass from all directions.
        for(int i=0;i<2;i++) {
            items.renderStatic(stack,ItemDisplayContext.FIXED,light,overlay,pose,buffers,jar.getLevel(),0);
            pose.mulPose(Axis.YP.rotationDegrees(90));
        }
        pose.popPose();
    }
}
