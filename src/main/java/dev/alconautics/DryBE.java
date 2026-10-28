package dev.alconautics;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class DryBE extends MachineBE {
    private Drink.Variant paidVariant = Drink.Variant.NORMAL;
    private int credit;
    public final ItemStackHandler items = new ItemStackHandler(1) {
        public boolean isItemValid(int slot,ItemStack s) { return s.is(Items.REDSTONE) || s.is(Items.GLOWSTONE_DUST); }
        protected void onContentsChanged(int slot) { changed(); }
    };
    public DryBE(BlockPos p,BlockState s) { super(Content.DRY_BE.get(),p,s); }
    public Drink.Variant variant() {
        if (credit > 0) return paidVariant;
        var s=items.getStackInSlot(0);
        return s.is(Items.REDSTONE) ? Drink.Variant.LONG : s.is(Items.GLOWSTONE_DUST) ? Drink.Variant.STRONG : Drink.Variant.NORMAL;
    }
    public void pay(int amount) {
        if (variant() == Drink.Variant.NORMAL) return;
        if (credit < amount) { paidVariant=variant(); items.extractItem(0,1,false); credit+=1000; }
        credit-=amount; changed();
    }
    @Override public boolean interact(Player p,InteractionHand hand) { return InventoryUse.interact(this,items,p,hand,false); }
    @Override public void dropContents() { InventoryUse.drop(this,items); }
    @Override public boolean addToGoggleTooltip(List<Component> t,boolean sneaking) {
        line(t,"dry"); line(t,"modifier",Component.translatable("modifier.alconautics."+variant().name().toLowerCase(java.util.Locale.ROOT))); line(t,"credit",credit); return true;
    }
    @Override protected void write(CompoundTag tag,HolderLookup.Provider r,boolean packet) { super.write(tag,r,packet); tag.put("Items",items.serializeNBT(r)); tag.putInt("Credit",credit); tag.putString("Variant",paidVariant.name()); }
    @Override protected void read(CompoundTag tag,HolderLookup.Provider r,boolean packet) { super.read(tag,r,packet); items.deserializeNBT(r,tag.getCompound("Items")); credit=Math.max(0,tag.getInt("Credit")); try { paidVariant=Drink.Variant.valueOf(tag.getString("Variant")); } catch (IllegalArgumentException e) { paidVariant=Drink.Variant.NORMAL; } }
}
