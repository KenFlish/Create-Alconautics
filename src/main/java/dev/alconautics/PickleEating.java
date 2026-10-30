package dev.alconautics;

import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid=Alconautics.ID)
public final class PickleEating {
    @SubscribeEvent public static void finish(LivingEntityUseItemEvent.Finish event) {
        if(!event.getEntity().level().isClientSide && event.getItem().is(Items.SEA_PICKLE)) BreweryCompat.soberOneLevel(event.getEntity());
    }
}
