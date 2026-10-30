package dev.alconautics.client;

import dev.alconautics.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.foundation.client.render.WobbleAnimation;

public final class BottleRenderer implements BlockEntityRenderer<BottleBE> {
    public static final ModelResourceLocation SINGLE=model("moonshine_bottle_single"),FIRST=model("moonshine_bottle_pair_0"),SECOND=model("moonshine_bottle_pair_1");
    private static ModelResourceLocation model(String name) { return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"block/"+name)); }
    public BottleRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(BottleBE bottles,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var inventory=bottles.getInventory();
        int count=inventory.get(0).isEmpty()?0:inventory.get(1).isEmpty()?1:2;
        if(count==0) return;
        var mc=Minecraft.getInstance();var state=bottles.getBlockState();
        float angle=switch(state.getValue(BottleBlock.FACING)) {case EAST -> 90;case SOUTH -> 180;case WEST -> 270;default -> 0;};
        pose.pushPose();pose.translate(0,.002,0);pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-angle));pose.translate(-.5,0,-.5);
        for(int slot=0;slot<count;slot++) {
            pose.pushPose();
            float pivot=count==1?.5f:slot==0?5/16f:11/16f;
            pose.translate(pivot-.5,0,pivot-.5);
            WobbleAnimation.apply(pose,bottles.getLevel(),bottles.getWobbleStart(),partial,WobbleAnimation.seed(bottles.getBlockPos().asLong()+slot));
            pose.translate(.5-pivot,0,.5-pivot);
            var model=mc.getModelManager().getModel(count==1?SINGLE:slot==0?FIRST:SECOND);
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),state,model,1,1,1,light,overlay);
            pose.popPose();
        }
        pose.popPose();
    }
}
