package dev.alconautics;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class CoolerBE extends MachineBE {
    private int cold;
    private boolean working;
    private BlockPos operatingHead;
    private Drink operatingDrink;
    private long activeUntil;
    public boolean isWorking() { return working; }
    public void keepOperating(BlockPos head,Drink drink) { operatingHead=head.immutable(); operatingDrink=drink; activeUntil=level.getGameTime()+20; }
    public final FluidTank input = new FluidTank(1000,s -> Content.vapour(s.getFluid()) != null) { protected void onContentsChanged() { setChanged(); } };
    public final FluidTank output = new FluidTank(4000,s -> Content.spirit(s.getFluid()) != null) { protected void onContentsChanged() { changed(); } };
    public final Ports inlet = new Ports(input,true), outlet = new Ports(output,false);
    public final ItemStackHandler items = new ItemStackHandler(1) {
        public boolean isItemValid(int i,ItemStack s) { return coldValue(s)>0; }
        protected void onContentsChanged(int i) { changed(); }
    };
    public CoolerBE(BlockPos p,BlockState s) { super(Content.COOLER_BE.get(),p,s); }
    public static int coldValue(ItemStack s) {
        if (s.is(Items.SNOWBALL)) return 250;
        if (s.is(Items.SNOW_BLOCK)) return 1000;
        if (s.is(Items.ICE)) return 2000;
        if (s.is(Items.PACKED_ICE)) return 8000;
        if (s.is(Items.BLUE_ICE)) return 32000;
        return 0;
    }
    public boolean wet() { return PipeRoute.wet(level,worldPosition.above()) && java.util.Arrays.stream(Direction.values()).filter(d -> d != Direction.DOWN).anyMatch(d -> PipeRoute.wet(level,worldPosition.above().relative(d)) && connected(worldPosition.above(),d)); }
    private boolean connected(BlockPos p,Direction d) {
        var b=com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour.get(level,p,com.simibubi.create.content.fluids.FluidTransportBehaviour.TYPE);
        var other=com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour.get(level,p.relative(d),com.simibubi.create.content.fluids.FluidTransportBehaviour.TYPE);
        return b!=null && other!=null && b.canHaveFlowToward(level.getBlockState(p),d) && other.canHaveFlowToward(level.getBlockState(p.relative(d)),d.getOpposite());
    }
    public String ready(Drink drink) {
        if (!wet()) return "wet_pipes";
        if (cold<100 && coldValue(items.getStackInSlot(0))==0) return "cold";
        if (output.fill(Content.SPIRITS.get(drink).stack(100),FluidAction.SIMULATE)<100) return "output";
        return "";
    }
    @Override public void tick() {
        super.tick();
        if (level.isClientSide) {
            if (working && level.getGameTime()%5==0) {
                double a=level.random.nextDouble()*Math.PI*2;
                double x=worldPosition.getX()+.5+Math.cos(a)*.6,z=worldPosition.getZ()+.5+Math.sin(a)*.6;
                level.addParticle(ParticleTypes.SNOWFLAKE,x,worldPosition.getY()+.4+level.random.nextDouble()*.7,z,Math.cos(a)*.012,.015,Math.sin(a)*.012);
            }
            return;
        }
        boolean was=working;
        working=operatingHead!=null && activeUntil>=level.getGameTime()
            && level.getBlockEntity(operatingHead) instanceof HeadBE head && head.operating
            && operatingDrink!=null && ready(operatingDrink).isEmpty();
        Drink drink=Content.vapour(input.getFluid().getFluid());
        if (drink!=null && wet()) {
            int amount=Math.min(100,input.getFluidAmount());
            var result=Content.SPIRITS.get(drink).stack(amount);
            if (amount>0 && output.fill(result,FluidAction.SIMULATE)==amount) {
                if (cold<amount) {
                    int value=coldValue(items.getStackInSlot(0));
                    if (value>0) { items.extractItem(0,1,false); cold+=value; }
                }
                if (cold>=amount) { input.drain(amount,FluidAction.EXECUTE); output.fill(result,FluidAction.EXECUTE); cold-=amount; working=true; setChanged(); }
            }
        }
        if (was!=working) changed();
    }
    @Override public boolean interact(Player p,InteractionHand hand) {
        var held=p.getItemInHand(hand);
        if (held.is(Items.GLASS_BOTTLE) && !p.isShiftKeyDown()) {
            var drink=Content.spirit(output.getFluid().getFluid());
            if (drink==null || output.getFluidAmount()<250) return false;
            if (!level.isClientSide) {
                output.drain(250,FluidAction.EXECUTE);
                var bottle=Content.DRINKS.get(drink).get().getDefaultInstance();
                if (!p.isCreative()) held.shrink(1);
                if (!p.addItem(bottle)) p.drop(bottle,false);
            }
            return true;
        }
        return InventoryUse.interact(this,items,p,hand,false);
    }
    @Override public void dropContents() { InventoryUse.drop(this,items); }
    @Override public boolean addToGoggleTooltip(List<Component> t,boolean s) { line(t,working?"cooler_running":"cooler_idle"); return true; }
    @Override protected void write(CompoundTag t,HolderLookup.Provider r,boolean p) { super.write(t,r,p); t.put("Input",input.writeToNBT(r,new CompoundTag())); t.put("Output",output.writeToNBT(r,new CompoundTag())); t.put("Items",items.serializeNBT(r)); t.putInt("Cold",cold); t.putBoolean("Working",working); }
    @Override protected void read(CompoundTag t,HolderLookup.Provider r,boolean p) { super.read(t,r,p); input.readFromNBT(r,t.getCompound("Input")); output.readFromNBT(r,t.getCompound("Output")); items.deserializeNBT(r,t.getCompound("Items")); cold=Math.max(0,t.getInt("Cold")); working=t.getBoolean("Working"); }
}
