package dev.alconautics.client;
import dev.alconautics.Alconautics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid=Alconautics.ID,value=Dist.CLIENT)
public final class PickleTooltip {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        if(event.getItemStack().is(Items.SEA_PICKLE)) event.getToolTip().add(Component.translatable("tooltip.alconautics.sea_pickle").withStyle(ChatFormatting.GRAY));
    }
}
