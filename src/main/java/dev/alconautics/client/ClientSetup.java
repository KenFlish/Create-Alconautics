package dev.alconautics.client;

import dev.alconautics.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid=Alconautics.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Content.DRY_BE.get(),DryRenderer::new);
        event.registerBlockEntityRenderer(Content.BOTTLE_BE.get(),BottleRenderer::new);
    }
    @SubscribeEvent public static void models(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
        event.register(BottleRenderer.SINGLE);event.register(BottleRenderer.FIRST);event.register(BottleRenderer.SECOND);
    }
    @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.createmod.ponder.foundation.PonderIndex.addPlugin(new AlconauticsPonder());
            net.satisfy.foundation.overlay.BlockInfoOverlay.registerProvider(new BottleInfo());
        });
    }
    @SubscribeEvent public static void fluids(RegisterClientExtensionsEvent event) {
        for(var f:Content.ALL_FLUIDS) {
            boolean vapour=f.id.endsWith("vapour"); boolean mash=f.id.endsWith("mash");
            int color=vapour?0x99f4f3ef:0xffefeee8;
            event.registerFluidType(new IClientFluidTypeExtensions() {
                public ResourceLocation getStillTexture() { return ResourceLocation.withDefaultNamespace("block/water_still"); }
                public ResourceLocation getFlowingTexture() { return ResourceLocation.withDefaultNamespace("block/water_flow"); }
                public int getTintColor() { return color; }
            },f.type.get());
        }
    }
}
