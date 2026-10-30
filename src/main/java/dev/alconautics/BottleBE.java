package dev.alconautics;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.foundation.storage.StorageBlockEntity;

/** Up to three real item stacks; Foundation supplies inventory packets and per-slot wobble. */
public final class BottleBE extends StorageBlockEntity {
    private boolean initialized;
    public BottleBE(BlockPos pos,BlockState state) { super(Content.BOTTLE_BE.get(),pos,state,3); }
    @Override public void onLoad() { super.onLoad();ensureLegacy(); }
    private void ensureLegacy() {
        if(initialized || level==null || level.isClientSide) return;
        initialized=true;
        super.getInventory().set(0,getBlockState().getBlock() instanceof TinctureBlock?Content.TINCTURE_ITEMS.get(Tincture.DANDELION).get().getDefaultInstance():Content.DRINKS.get(Drink.ALL.get(getBlockState().getValue(BottleBlock.DRINK))).get().getDefaultInstance());
        refresh();
    }
    @Override public NonNullList<ItemStack> getInventory() { ensureLegacy();return super.getInventory(); }
    public void placeFirst(ItemStack stack) {
        initialized=true;java.util.Collections.fill(super.getInventory(),ItemStack.EMPTY);var copy=stack.copyWithCount(1);if(level!=null) TinctureAge.update(copy,level);super.getInventory().set(0,copy);refresh();
    }
    public boolean addBottle(ItemStack stack) {
        ensureLegacy();
        var items=super.getInventory();boolean tinctures=getBlockState().getBlock() instanceof TinctureBlock;
        if(tinctures) {
            if(!(stack.getItem() instanceof TinctureItem next)) return false;
            int units=next.kind==Tincture.DANDELION?2:3;
            for(var item:items) if(item.getItem() instanceof TinctureItem old) units+=old.kind==Tincture.DANDELION?2:3;
            if(units>6) return false;
        } else if(!(stack.getItem() instanceof MoonshineItem) || !items.get(1).isEmpty()) return false;
        for(int slot=0;slot<(tinctures?3:2);slot++) if(items.get(slot).isEmpty()) {
            var copy=stack.copyWithCount(1);if(level!=null) TinctureAge.update(copy,level);
            initialized=true;items.set(slot,copy);refresh();return true;
        }
        return false;
    }
    public void ageBottles() {
        if(level==null || level.isClientSide) return;
        boolean changed=false;for(var stack:getInventory()) changed|=TinctureAge.update(stack,level);
        if(changed) super.setChanged();
    }

    public ItemStack takeBottle(int slot) {
        ensureLegacy();var items=super.getInventory();
        if(items.get(slot).isEmpty()) return ItemStack.EMPTY;
        if(level!=null) TinctureAge.update(items.get(slot),level);
        ItemStack result=items.get(slot).copy();items.set(slot,ItemStack.EMPTY);
        int target=0;for(int i=0;i<items.size();i++) if(!items.get(i).isEmpty()) { if(i!=target) { items.set(target,items.get(i));items.set(i,ItemStack.EMPTY); }target++; }
        refresh();return result;
    }
    private void refresh() {
        if(level!=null && !level.isClientSide) {
            var items=super.getInventory();
            if(items.get(0).isEmpty()) { level.removeBlock(worldPosition,false);return; }
            int count=(int)items.stream().filter(item -> !item.isEmpty()).count();
            var state=getBlockState().setValue(BottleBlock.COUNT,count);
            if(items.get(0).getItem() instanceof MoonshineItem first) state=state.setValue(BottleBlock.DRINK,Drink.ALL.indexOf(first.drink));
            else state=state.setValue(BottleBlock.DRINK,TinctureGeometry.layout(items));
            if(!state.equals(getBlockState())) level.setBlockAndUpdate(worldPosition,state);
        }
        super.setChanged();
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        var safe=tag.copy();safe.putInt("size",3);super.loadAdditional(safe,registries);
        initialized=tag.getBoolean("AlconauticsBottleData") || tag.contains("Items");
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        ensureLegacy();super.saveAdditional(tag,registries);tag.putBoolean("AlconauticsBottleData",true);
    }
}
