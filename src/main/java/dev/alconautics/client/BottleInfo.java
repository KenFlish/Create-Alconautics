package dev.alconautics.client;

import java.util.*;
import dev.alconautics.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.satisfy.foundation.overlay.*;

public final class BottleInfo implements BlockInfoProvider {
    @Override public List<InfoSection> describe(net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.phys.BlockHitResult hit) {
        if(!(level.getBlockEntity(pos) instanceof BottleBE bottles)) return List.of();
        var rows=new ArrayList<InfoSection.Row>();
        for(var stack:bottles.getInventory()) {
            if(stack.getItem() instanceof MoonshineItem item) rows.add(InfoSection.Row.item(stack,Component.translatable("drink_kind.alconautics."+item.drink.kind().id())));
            else if(stack.getItem() instanceof TinctureItem) rows.add(InfoSection.Row.item(stack,stack.getHoverName().copy().append(" — ").append(Component.translatable("tooltip.alconautics.tincture_age",TinctureAge.years(stack)))));
        }
        return List.of(InfoSection.rows(Component.translatable(state.getBlock() instanceof TinctureBlock?"block.alconautics.tincture_bottles":"block.alconautics.moonshine_bottle"),rows));
    }
}
