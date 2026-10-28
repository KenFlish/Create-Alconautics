package dev.alconautics.mixin;

import java.util.List;
import dev.alconautics.HeadBE;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidTankBlockEntity.class)
public abstract class TankMixin {
    @Shadow protected IFluidHandler fluidCapability;
    @Shadow abstract void refreshCapability();
    @Unique private boolean alconautics$wasApparatus;
    @Inject(method="tick",at=@At("HEAD"))
    private void alconautics$refresh(CallbackInfo ci) {
        var tank=(FluidTankBlockEntity)(Object)this;
        var head=HeadBE.find(tank);
        if(head!=null && tank.getControllerBE()!=null) {
            if(!alconautics$wasApparatus && tank.isController() && !tank.getLevel().isClientSide) tank.setWindows(false);
            var actual=tank.getControllerBE().getTankInventory();
            if(fluidCapability!=actual) { fluidCapability=actual; tank.invalidateCapabilities(); }
            alconautics$wasApparatus=true;
        } else if(alconautics$wasApparatus) { alconautics$wasApparatus=false; refreshCapability(); if(tank.isController() && !tank.getLevel().isClientSide && !tank.boiler.isActive()) tank.setWindows(true); }
    }
    @Inject(method="handlerForCapability",at=@At("HEAD"),cancellable=true)
    private void alconautics$handler(CallbackInfoReturnable<IFluidHandler> ci) {
        var tank=(FluidTankBlockEntity)(Object)this;
        if(HeadBE.find(tank)!=null) { var controller=tank.getControllerBE(); if(controller!=null) ci.setReturnValue(controller.getTankInventory()); }
    }
    @Inject(method="addToGoggleTooltip",at=@At("HEAD"),cancellable=true)
    private void alconautics$tooltip(List<Component> tooltip,boolean sneaking,CallbackInfoReturnable<Boolean> ci) {
        var head=HeadBE.find((FluidTankBlockEntity)(Object)this);
        if(head!=null) ci.setReturnValue(head.addToGoggleTooltip(tooltip,sneaking));
    }
    @Inject(method="addToGoggleTooltip",at=@At("RETURN"))
    private void alconautics$drinkDetails(List<Component> tooltip,boolean sneaking,CallbackInfoReturnable<Boolean> ci) {
        var tank=(FluidTankBlockEntity)(Object)this;
        if(HeadBE.find(tank)!=null || tank.getControllerBE()==null) return;
        var drink=dev.alconautics.Content.spirit(tank.getControllerBE().getTankInventory().getFluid().getFluid());
        if(drink!=null) dev.alconautics.DrinkTooltip.append(drink,c -> tooltip.add(Component.literal("    ").append(c)),20);
    }
}
