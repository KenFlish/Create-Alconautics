package dev.alconautics.mixin;

import dev.alconautics.*;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.brewery.core.block.entity.BigBarrelBlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reuses Brewery's storage, interactions, packets and overlay for tinctures. */
@Mixin(BigBarrelBlockEntity.class)
public abstract class TinctureBarrelMixin extends BlockEntity {
    @Shadow @Final private NonNullList<ItemStack> drinks;
    @Shadow @Final private long[] storedAt;
    protected TinctureBarrelMixin(BlockEntityType<?> type,BlockPos pos,BlockState state) { super(type,pos,state); }

    @Inject(method="canAge",at=@At("HEAD"),cancellable=true)
    private static void alconautics$accept(ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        if(stack.getItem() instanceof TinctureItem) cir.setReturnValue(true);
    }
    @Inject(method="insert",at=@At("HEAD"))
    private void alconautics$stamp(ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        if(level!=null) TinctureAge.update(stack,level);
    }
    @Inject(method="getQuality",at=@At("HEAD"),cancellable=true)
    private void alconautics$quality(int slot,CallbackInfoReturnable<Integer> cir) {
        if(level!=null && drinks.get(slot).getItem() instanceof TinctureItem)
            cir.setReturnValue(3+TinctureAge.amplifier(TinctureAge.inBarrel(drinks.get(slot),level,storedAt[slot])));
    }
    @Inject(method="getAgingPercent",at=@At("HEAD"),cancellable=true)
    private void alconautics$percent(int slot,CallbackInfoReturnable<Integer> cir) {
        if(level!=null && drinks.get(slot).getItem() instanceof TinctureItem)
            cir.setReturnValue(TinctureAge.maturityPercent(TinctureAge.inBarrel(drinks.get(slot),level,storedAt[slot]),TinctureAge.now(level)));
    }
    @Inject(method="aged",at=@At("HEAD"),cancellable=true)
    private void alconautics$take(int slot,CallbackInfoReturnable<ItemStack> cir) {
        if(level!=null && drinks.get(slot).getItem() instanceof TinctureItem)
            cir.setReturnValue(TinctureAge.inBarrel(drinks.get(slot),level,storedAt[slot]));
    }
}
