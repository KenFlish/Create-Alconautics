package dev.alconautics.test.client;

import dev.alconautics.Alconautics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=Alconautics.ID,value=Dist.CLIENT)
public final class CompatClientCheck {
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(Boolean.getBoolean("alconautics.jeiCheck")) JeiClientCheck.tick();
    }
}
