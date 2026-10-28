package dev.alconautics;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Prevents automation from inserting into outputs or extracting process inputs. */
public final class Ports implements IFluidHandler {
    private final IFluidHandler tank;
    private final boolean input;
    public Ports(IFluidHandler tank, boolean input) { this.tank = tank; this.input = input; }
    public int getTanks() { return tank.getTanks(); }
    public FluidStack getFluidInTank(int i) { return tank.getFluidInTank(i); }
    public int getTankCapacity(int i) { return tank.getTankCapacity(i); }
    public boolean isFluidValid(int i, FluidStack fluid) { return input && tank.isFluidValid(i, fluid); }
    public int fill(FluidStack fluid, FluidAction action) { return input ? tank.fill(fluid, action) : 0; }
    public FluidStack drain(FluidStack fluid, FluidAction action) { return input ? FluidStack.EMPTY : tank.drain(fluid, action); }
    public FluidStack drain(int amount, FluidAction action) { return input ? FluidStack.EMPTY : tank.drain(amount, action); }
}
