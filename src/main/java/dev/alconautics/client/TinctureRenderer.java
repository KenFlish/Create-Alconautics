package dev.alconautics.client;

import dev.alconautics.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.foundation.client.render.WobbleAnimation;

final class TinctureRenderer {
    static ModelResourceLocation model(String name) { return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"block/"+name)); }
    static void render(BottleBE bottles,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var items=bottles.getInventory();if(items.getFirst().isEmpty()) return;
        int layout=TinctureGeometry.layout(items);var state=bottles.getBlockState();
        float angle=switch(state.getValue(BottleBlock.FACING)) {case EAST -> 90;case SOUTH -> 180;case WEST -> 270;default -> 0;};
        pose.pushPose();pose.translate(0,.002,0);pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-angle));pose.translate(-.5,0,-.5);
        var mc=Minecraft.getInstance();
        for(int part=0;part<TinctureGeometry.CENTERS[layout].length;part++) {
            var center=TinctureGeometry.CENTERS[layout][part];int slot=TinctureGeometry.slot(items,layout,part);
            pose.pushPose();pose.translate(center[0]-.5,0,center[1]-.5);
            WobbleAnimation.apply(pose,bottles.getLevel(),bottles.getWobbleStart(),partial,WobbleAnimation.seed(bottles.getBlockPos().asLong()+slot));
            pose.translate(.5-center[0],0,.5-center[1]);
            var baked=mc.getModelManager().getModel(model(TinctureGeometry.model(layout,part)));
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),state,baked,1,1,1,light,overlay);pose.popPose();
        }
        pose.popPose();
    }
}
