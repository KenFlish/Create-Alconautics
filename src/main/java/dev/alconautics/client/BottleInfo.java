package dev.alconautics.client;

import java.util.*;
import dev.alconautics.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.satisfy.foundation.overlay.*;

public final class BottleInfo implements BlockInfoProvider {
    @Override public List<InfoSection> describe(net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.phys.BlockHitResult hit) {
        if(!(level.getBlockEntity(pos) instanceof BottleBE bottles)) return List.of();
        var counts=new LinkedHashMap<Drink.Kind,Integer>();var icons=new EnumMap<Drink.Kind,ItemStack>(Drink.Kind.class);
        for(var stack:bottles.getInventory()) if(stack.getItem() instanceof MoonshineItem item) {
            counts.merge(item.drink.kind(),1,Integer::sum);icons.putIfAbsent(item.drink.kind(),stack);
        }
        var rows=new ArrayList<InfoSection.Row>();
        counts.forEach((kind,count) -> {
            var text=Component.translatable("drink_kind.alconautics."+kind.id());
            if(count>1) text.append(" ×"+count);
            rows.add(InfoSection.Row.item(icons.get(kind),text));
        });
        return List.of(InfoSection.rows(Component.translatable("block.alconautics.moonshine_bottle"),rows));
    }
}
