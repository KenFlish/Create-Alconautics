package dev.alconautics;

import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(Alconautics.ID)
public final class Alconautics {
    public static final String ID = "alconautics";
    public Alconautics(IEventBus bus, ModContainer container) {
        Content.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(this::capabilities);
        bus.addListener(this::tabs);
        bus.addListener(this::foods);
        bus.addListener(this::commonSetup);
    }
    private void commonSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        if(net.neoforged.fml.ModList.get().isLoaded("createbigcannons")) event.enqueueWork(dev.alconautics.compat.BigCannonsCompat::register);
    }
    private void tabs(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        if(event.getTab()==com.simibubi.create.AllCreativeModeTabs.BASE_CREATIVE_TAB.get()) {
            event.accept(Content.HEAD_ITEM.get());event.accept(Content.COOLER_ITEM.get());event.accept(Content.DRY_ITEM.get());
        }
        if(event.getTab()==net.satisfy.brewery.core.registry.TabRegistry.BREWERY_TAB.get()) {
            Content.DRINKS.forEach((drink,item) -> { if(drink.variant()==Drink.Variant.NORMAL) event.accept(item.get()); });
            Content.TINCTURE_ITEMS.values().forEach(item -> event.accept(item.get()));
        }
    }
    private void foods(net.neoforged.neoforge.event.ModifyDefaultComponentsEvent event) {
        event.modify(net.minecraft.world.item.Items.SEA_PICKLE,builder -> builder.set(net.minecraft.core.component.DataComponents.FOOD,
            new net.minecraft.world.food.FoodProperties.Builder().nutrition(1).saturationModifier(.1f).alwaysEdible().build()));
    }
    private void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Content.HEAD_BE.get(), (be, side) -> MachineBlock.isHeadOutlet(be.getBlockState(), side) ? be.outlet : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Content.COOLER_BE.get(), (be, side) -> side == Direction.UP ? be.inlet : be.outlet);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.COOLER_BE.get(), (be, side) -> be.items);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.DRY_BE.get(), (be, side) -> be.items);
    }
}
