package dev.alconautics;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.registries.*;

public final class Content {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Alconautics.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Alconautics.ID);
    public static final DeferredRegister<BlockEntityType<?>> BES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Alconautics.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Alconautics.ID);
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Alconautics.ID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Alconautics.ID);
    private static BlockBehaviour.Properties machine() { return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3).sound(SoundType.COPPER).noOcclusion(); }
    public static final DeferredBlock<MachineBlock> HEAD = BLOCKS.register("distillation_head", () -> new MachineBlock(machine(), MachineBlock.Kind.HEAD));
    public static final DeferredBlock<MachineBlock> COOLER = BLOCKS.register("condenser_base", () -> new MachineBlock(machine(), MachineBlock.Kind.COOLER));
    public static final DeferredBlock<MachineBlock> DRY = BLOCKS.register("dry_chamber", () -> new MachineBlock(machine(), MachineBlock.Kind.DRY));
    public static final DeferredBlock<BottleBlock> BOTTLE = BLOCKS.register("moonshine_bottle", () -> new BottleBlock(BlockBehaviour.Properties.of().strength(.3f).sound(SoundType.GLASS).noOcclusion()));
    public static final DeferredItem<BlockItem> HEAD_ITEM = ITEMS.registerSimpleBlockItem(HEAD);
    public static final DeferredItem<BlockItem> COOLER_ITEM = ITEMS.registerSimpleBlockItem(COOLER);
    public static final DeferredItem<BlockItem> DRY_ITEM = ITEMS.registerSimpleBlockItem(DRY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeadBE>> HEAD_BE = BES.register("distillation_head", () -> BlockEntityType.Builder.of(HeadBE::new, HEAD.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoolerBE>> COOLER_BE = BES.register("condenser_base", () -> BlockEntityType.Builder.of(CoolerBE::new, COOLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryBE>> DRY_BE = BES.register("dry_chamber", () -> BlockEntityType.Builder.of(DryBE::new, DRY.get()).build(null));
    public static final DeferredHolder<MobEffect, MobEffect> GROW = EFFECTS.register("growth", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xe4c277) {});
    public static final DeferredHolder<MobEffect, MobEffect> SHRINK = EFFECTS.register("shrinking", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xcc6688) {});
    public static final Map<Drink.Kind, FluidSet> MASH = new EnumMap<>(Drink.Kind.class);
    public static final Map<Drink, FluidSet> VAPOUR = new LinkedHashMap<>(), SPIRITS = new LinkedHashMap<>();
    public static final Map<Drink, DeferredItem<MoonshineItem>> DRINKS = new LinkedHashMap<>();
    public static final List<FluidSet> ALL_FLUIDS = new ArrayList<>();
    static {
        for (var kind : Drink.Kind.values()) MASH.put(kind, fluid(kind.id() + "_mash", false));
        for (var drink : Drink.ALL) {
            SPIRITS.put(drink, fluid(drink.id(), false));
            VAPOUR.put(drink, fluid(drink.id() + "_vapour", true));
            DRINKS.put(drink, ITEMS.register(drink.id(), () -> new MoonshineItem(new Item.Properties().stacksTo(16), drink)));
        }
    }
    public static final class FluidSet {
        public DeferredHolder<FluidType, FluidType> type;
        public DeferredHolder<Fluid, BaseFlowingFluid.Source> source;
        public DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
        public final String id;
        FluidSet(String id) { this.id = id; }
        public FluidStack stack(int amount) { return new FluidStack(source.get(), amount); }
        public boolean matches(Fluid fluid) { return fluid == source.get() || fluid == flowing.get(); }
    }
    private static FluidSet fluid(String id, boolean gas) {
        var set = new FluidSet(id);
        set.type = TYPES.register(id, () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.alconautics." + id)
            .density(gas ? -100 : 1000).viscosity(gas ? 100 : 1000).canSwim(false).canDrown(false)) {});
        Supplier<BaseFlowingFluid.Properties> props = () -> new BaseFlowingFluid.Properties(set.type, set.source, set.flowing);
        set.source = FLUIDS.register(id, () -> new BaseFlowingFluid.Source(props.get()));
        set.flowing = FLUIDS.register("flowing_" + id, () -> new BaseFlowingFluid.Flowing(props.get()));
        ALL_FLUIDS.add(set);
        return set;
    }
    public static Drink.Kind mashKind(Fluid fluid) { return MASH.entrySet().stream().filter(e -> e.getValue().matches(fluid)).map(Map.Entry::getKey).findFirst().orElse(null); }
    public static Drink vapour(Fluid fluid) { return VAPOUR.entrySet().stream().filter(e -> e.getValue().matches(fluid)).map(Map.Entry::getKey).findFirst().orElse(null); }
    public static Drink spirit(Fluid fluid) { return SPIRITS.entrySet().stream().filter(e -> e.getValue().matches(fluid)).map(Map.Entry::getKey).findFirst().orElse(null); }
    public static void register(IEventBus bus) { BLOCKS.register(bus); ITEMS.register(bus); BES.register(bus); TYPES.register(bus); FLUIDS.register(bus); EFFECTS.register(bus); }
}
