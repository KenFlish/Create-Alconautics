    package dev.alconautics;

import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Alconautics.ID)
public final class Alconautics {
    public static final String ID = "alconautics";
    public Alconautics(IEventBus bus, ModContainer container) {
        Content.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(this::capabilities);
        var tabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
        tabs.register("main", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.alconautics"))
            .icon(() -> Content.HEAD_ITEM.get().getDefaultInstance()).displayItems((p, out) -> {
                out.accept(Content.HEAD_ITEM.get()); out.accept(Content.COOLER_ITEM.get()); out.accept(Content.DRY_ITEM.get());
                Content.DRINKS.forEach((drink, item) -> { if (drink.variant() == Drink.Variant.NORMAL) out.accept(item.get()); });
            }).build());
        tabs.register(bus);
    }
    private void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Content.HEAD_BE.get(), (be, side) -> MachineBlock.isHeadOutlet(be.getBlockState(), side) ? be.outlet : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Content.COOLER_BE.get(), (be, side) -> side == Direction.UP ? be.inlet : be.outlet);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.COOLER_BE.get(), (be, side) -> be.items);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.DRY_BE.get(), (be, side) -> be.items);
    }
}
