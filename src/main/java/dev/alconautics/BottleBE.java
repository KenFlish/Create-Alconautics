package dev.alconautics;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.foundation.storage.StorageBlockEntity;

/** Two real item stacks; Foundation supplies inventory packets and per-slot wobble. */
public final class BottleBE extends StorageBlockEntity {
    private boolean initialized;
    public BottleBE(BlockPos pos,BlockState state) { super(Content.BOTTLE_BE.get(),pos,state,2); }
    @Override public void onLoad() { super.onLoad();ensureLegacy(); }
    private void ensureLegacy() {
        if(initialized || level==null || level.isClientSide) return;
        initialized=true;
        super.getInventory().set(0,Content.DRINKS.get(Drink.ALL.get(getBlockState().getValue(BottleBlock.DRINK))).get().getDefaultInstance());
        refresh();
    }
    @Override public NonNullList<ItemStack> getInventory() { ensureLegacy();return super.getInventory(); }
    public void placeFirst(ItemStack stack) {
        initialized=true;super.getInventory().set(0,stack.copyWithCount(1));super.getInventory().set(1,ItemStack.EMPTY);refresh();
    }
    public boolean addBottle(ItemStack stack) {
        ensureLegacy();
        if(!(stack.getItem() instanceof MoonshineItem) || !super.getInventory().get(1).isEmpty()) return false;
        initialized=true;super.getInventory().set(super.getInventory().get(0).isEmpty()?0:1,stack.copyWithCount(1));refresh();return true;
    }
    public ItemStack takeBottle(int slot) {
        ensureLegacy();var items=super.getInventory();
        if(items.get(slot).isEmpty()) return ItemStack.EMPTY;
        ItemStack result=items.get(slot).copy();items.set(slot,ItemStack.EMPTY);
        if(items.get(0).isEmpty() && !items.get(1).isEmpty()) { items.set(0,items.get(1));items.set(1,ItemStack.EMPTY); }
        refresh();return result;
    }
    private void refresh() {
        if(level!=null && !level.isClientSide) {
            var items=super.getInventory();
            if(items.get(0).isEmpty()) { level.removeBlock(worldPosition,false);return; }
            var first=(MoonshineItem)items.get(0).getItem();
            var state=getBlockState().setValue(BottleBlock.COUNT,items.get(1).isEmpty()?1:2).setValue(BottleBlock.DRINK,Drink.ALL.indexOf(first.drink));
            if(!state.equals(getBlockState())) level.setBlockAndUpdate(worldPosition,state);
        }
        super.setChanged();
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        var safe=tag.copy();safe.putInt("size",2);super.loadAdditional(safe,registries);
        initialized=tag.getBoolean("AlconauticsBottleData") || tag.contains("Items");
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        ensureLegacy();super.saveAdditional(tag,registries);tag.putBoolean("AlconauticsBottleData",true);
    }
}
