package dev.alconautics.compat.jei;

import dev.alconautics.*;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.FluidRenderHelper;
import net.createmod.catnip.impl.client.render.ColoringVertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/** Uses Create's native JEI lighting, models and animated Blaze Burner. */
final class ApparatusDisplay {
    static void still(GuiGraphics g,boolean superheat) {
        g.pose().pushPose();g.pose().translate(72,94,200);rotate(g);
        block(g,AllBlocks.FLUID_TANK.getDefaultState(),0,-1,0);
        block(g,Content.HEAD.get().defaultBlockState(),0,-2,0);
        g.pose().popPose();
        new AnimatedBlazeBurner().withHeat(superheat?HeatLevel.SEETHING:HeatLevel.KINDLED).draw(g,72,62);
    }
    static void condenser(GuiGraphics g) {
        g.pose().pushPose();g.pose().translate(75,92,200);rotate(g);
        block(g,Content.COOLER.get().defaultBlockState(),0,0,0);
        var pipe=AllBlocks.FLUID_PIPE.getDefaultState();
        for(Direction d:Direction.values()) pipe=pipe.setValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(d),d.getAxis()==Direction.Axis.Y);
        for(int y=-2;y<=-1;y++) {
            block(g,pipe,0,y,0);
            for(var side:new Direction[]{Direction.UP,Direction.DOWN})
                GuiGameElement.of(AllPartialModels.PIPE_ATTACHMENTS.get(ComponentPartials.CONNECTION).get(side)).atLocal(0,y,0).scale(21).lighting(AnimatedKinetics.DEFAULT_LIGHTING).render(g);
        }
        // Draw water after the opaque pipes, with a translucent cutaway view.
        g.pose().pushPose();g.pose().scale(21,21,21);g.pose().translate(0,-1,0);
        UIRenderHelper.flipForGuiRender(g.pose());
        var water=new ColoringVertexConsumer(FluidRenderHelper.getFluidBuilder(g.bufferSource()),1,1,1,.28f);
        CatnipServices.FLUID_RENDERER.renderFluidBox(Fluids.WATER.defaultFluidState(),0,0,0,1,2,1,water,g.pose(),0xf000f0,false,true);
        g.flush();g.pose().popPose();
        g.pose().popPose();
    }
    private static void rotate(GuiGraphics g) { g.pose().mulPose(Axis.XP.rotationDegrees(-15.5f));g.pose().mulPose(Axis.YP.rotationDegrees(22.5f)); }
    private static void block(GuiGraphics g,BlockState state,int x,int y,int z) { GuiGameElement.of(state).atLocal(x,y,z).scale(21).lighting(AnimatedKinetics.DEFAULT_LIGHTING).render(g); }
}
