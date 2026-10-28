package dev.alconautics;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemStackHandler;

final class InventoryUse {
    static boolean interact(MachineBE be,ItemStackHandler items,Player p,InteractionHand hand,boolean shiftRequired) {
        ItemStack held=p.getItemInHand(hand);
        if (held.isEmpty() && p.isShiftKeyDown()) {
            if (!be.getLevel().isClientSide) { var out=items.extractItem(0,64,false); if (!p.addItem(out)) p.drop(out,false); }
            return true;
        }
        if (held.isEmpty() || (shiftRequired && !p.isShiftKeyDown()) || !items.isItemValid(0,held)) return false;
        if (!be.getLevel().isClientSide) { var left=items.insertItem(0,held.copy(),false); if (!p.isCreative()) p.setItemInHand(hand,left); }
        return true;
    }
    static void drop(MachineBE be,ItemStackHandler items) { if (!be.getLevel().isClientSide) Block.popResource(be.getLevel(),be.getBlockPos(),items.getStackInSlot(0).copy()); }
}
