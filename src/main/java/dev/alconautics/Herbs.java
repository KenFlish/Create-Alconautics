package dev.alconautics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public final class Herbs {
    public static Item ribwort() { return item("wild_ribwort"); }
    public static Item nettle() { return item("wild_nettle"); }
    private static Item item(String id) { return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("farm_and_charm",id)); }
}
