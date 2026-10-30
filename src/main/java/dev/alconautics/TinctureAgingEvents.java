package dev.alconautics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=Alconautics.ID)
public final class TinctureAgingEvents {
    @SubscribeEvent public static void dropped(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item) TinctureAge.update(item.getItem(),event.getLevel());
    }
    @SubscribeEvent public static void opened(net.neoforged.neoforge.event.entity.player.PlayerContainerEvent.Open event) {
        for(var slot:event.getContainer().slots) if(TinctureAge.update(slot.getItem(),event.getEntity().level())) slot.setChanged();
    }
}
