package dev.alconautics.compat.jei;

import dev.alconautics.Alconautics;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.FluidRenderHelper;
import net.createmod.catnip.impl.client.render.ColoringVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import java.util.*;

/** The user's supplied assembly, rendered with the installed block models. */
final class ApparatusDisplay {
    private record Part(BlockPos pos,BlockState state,BlockEntity entity) {}
    private static List<Part> parts;
    private static net.minecraft.client.multiplayer.ClientLevel world;
    static void render(GuiGraphics g,boolean superheat) {
        var mc=Minecraft.getInstance();
        if(mc.level==null) return;
        if(parts==null || world!=mc.level) load();
        g.pose().pushPose();g.pose().translate(102,94,200);
        g.pose().mulPose(Axis.XP.rotationDegrees(-15.5f));g.pose().mulPose(Axis.YP.rotationDegrees(202.5f));
        for(var part:parts) {
            var state=part.state();var pos=part.pos();
            if(state.is(AllBlocks.BLAZE_BURNER.get())) state=state.setValue(BlazeBurnerBlock.HEAT_LEVEL,superheat?HeatLevel.SEETHING:HeatLevel.KINDLED);
            var model=part.entity()==null?GuiGameElement.of(state):GuiGameElement.of(state,part.entity());
            model.atLocal(pos.getX()-1.5,-pos.getY(),pos.getZ()-1.5).scale(19).lighting(AnimatedKinetics.DEFAULT_LIGHTING).render(g);
            if(state.is(AllBlocks.FLUID_PIPE.get())) {
                for(var side:Direction.values()) if(state.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(side)))
                    GuiGameElement.of(AllPartialModels.PIPE_ATTACHMENTS.get(ComponentPartials.CONNECTION).get(side))
                        .atLocal(pos.getX()-1.5,-pos.getY(),pos.getZ()-1.5).scale(19).lighting(AnimatedKinetics.DEFAULT_LIGHTING).render(g);
            }
        }
        g.pose().pushPose();g.pose().scale(19,19,19);g.pose().translate(-.5,-1,-.5);
        UIRenderHelper.flipForGuiRender(g.pose());
        var water=new ColoringVertexConsumer(FluidRenderHelper.getFluidBuilder(g.bufferSource()),1,1,1,.28f);
        CatnipServices.FLUID_RENDERER.renderFluidBox(Fluids.WATER.defaultFluidState(),0,0,0,1,2,1,water,g.pose(),0xf000f0,false,true);
        g.flush();g.pose().popPose();g.pose().popPose();
    }
    private static void load() {
        var mc=Minecraft.getInstance();var result=new ArrayList<Part>();
        var id=ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"jei/distillery.nbt");
        try(var input=mc.getResourceManager().open(id)) {
            var nbt=NbtIo.readCompressed(input,NbtAccounter.unlimitedHeap());
            var palette=nbt.getList("palette",Tag.TAG_COMPOUND);var blocks=nbt.getList("blocks",Tag.TAG_COMPOUND);
            for(var entry:blocks) {
                var block=(CompoundTag)entry;var coords=block.getList("pos",Tag.TAG_INT);
                var pos=new BlockPos(coords.getInt(0),coords.getInt(1),coords.getInt(2));
                var state=NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),palette.getCompound(block.getInt("state")));
                if(state.is(AllBlocks.COPYCAT_PANEL.get()) || state.is(net.minecraft.tags.BlockTags.SLABS)) continue;
                var entity=block.contains("nbt")?BlockEntity.loadStatic(pos,state,block.getCompound("nbt"),mc.level.registryAccess()):null;
                if(entity!=null) entity.setLevel(mc.level);
                result.add(new Part(pos,state,entity));
            }
        } catch(java.io.IOException e) { throw new IllegalStateException("Could not load JEI distillery schematic",e); }
        parts=List.copyOf(result);world=mc.level;
    }
}
