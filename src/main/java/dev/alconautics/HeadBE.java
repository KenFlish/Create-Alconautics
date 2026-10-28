package dev.alconautics;

import java.util.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class HeadBE extends MachineBE {
    public final FluidTank steam = new FluidTank(1000) { protected void onContentsChanged() { setChanged(); } };
    public final Ports outlet = new Ports(steam,false);
    public int heat, danger;
    public boolean whistle, operating;
    public String status="empty";
    private int progress, whistleTime, whistleBurst;
    private PipeRoute route;
    private BlockPos whistlePos;
    public HeadBE(BlockPos pos,BlockState state) { super(Content.HEAD_BE.get(),pos,state); }

    public FluidTankBlockEntity tank() {
        if (level==null || !(level.getBlockEntity(worldPosition.below()) instanceof FluidTankBlockEntity part)) return null;
        var t=part.getControllerBE();
        return t!=null && worldPosition.getY()==t.getBlockPos().getY()+t.getHeight() ? t : null;
    }
    public static HeadBE find(FluidTankBlockEntity part) {
        if (part==null || part.getLevel()==null) return null;
        var t=part.getControllerBE(); if (t==null) return null;
        BlockPos top=t.getBlockPos().above(t.getHeight());
        for (int x=0;x<t.getWidth();x++) for (int z=0;z<t.getWidth();z++) {
            BlockPos p=top.offset(x,0,z);
            if (t.getLevel().hasChunkAt(p) && t.getLevel().getBlockEntity(p) instanceof HeadBE head) return head;
        }
        return null;
    }
    @Override public void tick() {
        super.tick();
        var t=tank();
        if (level.isClientSide) {
            if(t!=null) t.boiler.gauge.chase(heat/2f,.15f,net.createmod.catnip.animation.LerpedFloat.Chaser.EXP);
            if(whistleBurst>0) whistleBurst--;
            return;
        }
        if (t==null) { stop("tank"); return; }
        if (find(t)!=this) { stop("heads"); return; }
        if (level.getGameTime()%10==0 || route==null) {
            readStructure(t);
            var next=PipeRoute.scan(level,worldPosition);
            if(route!=null && (!route.nodes().equals(next.nodes()) || route.valid()!=next.valid())) route.pressure(level,0);
            route=next;
            if(route.valid()) route.pressure(level,32);
            changed();
        }
        var mash=t.getTankInventory();
        var kind=Content.mashKind(mash.getFluid().getFluid());
        boolean boiling=heat>0 && ((kind!=null && mash.getFluidAmount()>0) || !steam.isEmpty());
        // Pressure safety depends on heated mash, including a blocked condenser.
        if (boiling && !whistle) {
            danger+=heat;
            float pressure=Math.min(1f,danger/(Config.BURST_TICKS.get()*2f));
            int interval=pressure>.75f?10:pressure>.4f?20:40;
            if (level.getGameTime()%interval==0) {
                level.playSound(null,worldPosition,SoundEvents.BEACON_AMBIENT,SoundSource.BLOCKS,.12f+pressure*.65f,.55f+pressure*.65f);
                if(pressure>.6f) level.playSound(null,worldPosition,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,pressure*.28f,.6f);
            }
            if(danger>=Config.BURST_TICKS.get()*2) { rupture(t); return; }
        } else if(danger!=0) { danger=0; setChanged(); }
        if(heat==0) { stop("heat"); return; }
        if(kind==null) { stop(mash.isEmpty()?"empty":"wrong_fluid"); return; }
        if(!route.valid()) { stop(route.error()); return; }
        if (hasEngine(t)) { stop("engine"); return; }
        var variant=route.dry()!=null && kind!=Drink.Kind.VEGETABLE ? route.dry().variant() : Drink.Variant.NORMAL;
        var drink=new Drink(kind,variant);
        String problem=route.cooler().ready(drink);
        if(!problem.isEmpty()) { stop(problem); return; }
        var vapour=Content.VAPOUR.get(drink).stack(100);
        if(steam.fill(vapour,FluidAction.SIMULATE)<100 || mash.getFluidAmount()<100) { stop("buffer"); return; }
        operating=true;
        status=whistle?"running":"no_whistle";
        route.cooler().keepOperating(worldPosition,drink);
        progress++;
        if(progress>=(heat==2?Config.SUPER_TICKS.get():Config.NORMAL_TICKS.get())) {
            progress=0;
            mash.drain(100,FluidAction.EXECUTE);
            steam.fill(vapour,FluidAction.EXECUTE);
            if(route.dry()!=null && kind!=Drink.Kind.VEGETABLE) route.dry().pay(100);
            t.notifyUpdate(); changed();
        }
        if(whistle && --whistleTime<=0) { whistleTime=600+level.random.nextInt(601); whistleBurst=90; t.notifyUpdate(); changed(); }
        if(whistleBurst>0) {
            whistleBurst--;
            if(isWhistling() && whistleBurst%10==0 && level instanceof net.minecraft.server.level.ServerLevel server)
                server.sendParticles(new com.simibubi.create.content.kinetics.steamEngine.SteamJetParticleData(.25f),whistlePos.getX()+.5,whistlePos.getY()+.8,whistlePos.getZ()+.5,1,.025,.05,.025,.025);
        }
        if(level.getGameTime()%20==0) setChanged();
    }
    public boolean isWhistling() { return whistle && operating && (whistleBurst>60 || (whistleBurst>15 && whistleBurst<=45)); }
    private void stop(String why) { if(operating || !status.equals(why)) { operating=false; status=why; changed(); } }
    private void readStructure(FluidTankBlockEntity t) {
        int previousHeat=heat;
        heat=0; whistle=false; whistlePos=worldPosition;
        for (int x=0;x<t.getWidth();x++) for(int z=0;z<t.getWidth();z++) {
            var state=level.getBlockState(t.getBlockPos().offset(x,-1,z));
            if(state.is(AllBlocks.BLAZE_BURNER.get())) {
                HeatLevel h=state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
                if(h==HeatLevel.SEETHING) heat=2;
                else if(h==HeatLevel.KINDLED || h==HeatLevel.FADING) heat=Math.max(heat,1);
            }
        }
        for(BlockPos p:positions(t)) for(Direction d:Direction.values()) {
            var state=level.getBlockState(p.relative(d));
            if(state.is(AllBlocks.STEAM_WHISTLE.get()) && WhistleBlock.getAttachedDirection(state).getOpposite()==d) { whistle=true; whistlePos=p.relative(d); }
        }
        t.boiler.activeHeat=heat; t.boiler.passiveHeat=false;
        if(previousHeat!=heat) t.notifyUpdate();
        Arrays.fill(t.boiler.occludedDirections,false);
    }
    private boolean hasEngine(FluidTankBlockEntity t) { for(BlockPos p:positions(t)) for(Direction d:Direction.values()) if(level.getBlockState(p.relative(d)).is(AllBlocks.STEAM_ENGINE.get())) return true; return false; }
    private static List<BlockPos> positions(FluidTankBlockEntity t) {
        List<BlockPos> out=new ArrayList<>();
        for(int y=0;y<t.getHeight();y++) for(int x=0;x<t.getWidth();x++) for(int z=0;z<t.getWidth();z++) out.add(t.getBlockPos().offset(x,y,z));
        return out;
    }
    private void rupture(FluidTankBlockEntity t) {
        var origin=t.getBlockPos();
        if(route!=null) route.pressure(level,0);
        var center=new net.minecraft.world.phys.Vec3(origin.getX()+.5,origin.getY()+t.getHeight()*.5,origin.getZ()+.5);
        level.explode(null,center.x,center.y,center.z,1.6f,Level.ExplosionInteraction.NONE);
        if(level instanceof net.minecraft.server.level.ServerLevel server) {
            // A filled sphere, rather than an AreaEffectCloud's flat particle disc.
            for(int i=0;i<110;i++) {
                double y=1-2*(i+.5)/110.0,a=i*2.399963229728653;
                double r=Math.sqrt(1-y*y),x=Math.cos(a)*r,z=Math.sin(a)*r;
                double radius=.4+level.random.nextDouble()*1.5;
                server.sendParticles(i%4==0?ParticleTypes.FLAME:i%3==0?ParticleTypes.CLOUD:ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    center.x+x*radius,center.y+y*radius,center.z+z*radius,0,x*.55,y*.55,z*.55,.22);
            }
        }
        var cloud=new AreaEffectCloud(level,origin.getX()+.5,origin.getY()+1,origin.getZ()+.5);
        cloud.setRadius(3); cloud.setDuration(200); cloud.setRadiusPerTick(-.01f);
        // Hide the vanilla planar particles; the client renders this cloud as a volume.
        cloud.setParticle(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT,0x00eff1ed));
        cloud.setCustomName(Component.literal("alconautics:rupture_cloud"));cloud.setCustomNameVisible(false);
        BreweryCompat.drunkEffect().ifPresent(e -> cloud.addEffect(new MobEffectInstance(e,600,0)));
        level.addFreshEntity(cloud);
        for(BlockPos p:positions(t)) level.destroyBlock(p,true);
        level.destroyBlock(worldPosition,true);
        if(level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOFIRETICK)) {
            int fires=0;
            for(BlockPos p:List.of(origin,origin.east(),origin.west(),origin.north(),origin.south())) {
                if(fires>=2) break;
                if(level.isEmptyBlock(p) && net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState().canSurvive(level,p)) {
                    level.setBlockAndUpdate(p,net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());fires++;
                }
            }
        }
    }
    @Override public void dropContents() { if(route!=null && !level.isClientSide) route.pressure(level,0); }
    @Override public boolean addToGoggleTooltip(List<Component> out,boolean sneaking) {
        var t=tank();
        line(out,"status_header");
        float fraction=t==null?0:t.getFillState();
        out.add(Component.literal("    ").append(Goggles.gauge("mash",Math.clamp((int)Math.ceil(fraction*3),0,3))));
        var kind=t==null?null:Content.mashKind(t.getTankInventory().getFluid().getFluid());
        out.add(Component.literal("    ").append(Goggles.gauge("heat",heat==0?0:heat+1)));
        out.add(Component.literal("    ").append(Component.translatable("tooltip.alconautics.kind_label").withStyle(net.minecraft.ChatFormatting.GRAY))
            .append(Component.translatable("kind.alconautics."+(kind==null?"none":kind.id())).withStyle(net.minecraft.ChatFormatting.WHITE)));
        out.add(Component.empty());
        out.add(Component.literal("    ").append(Component.translatable("status.alconautics."+status).withStyle(net.minecraft.ChatFormatting.GRAY)));
        return true;
    }
    @Override protected void write(CompoundTag tag,HolderLookup.Provider r,boolean packet) { super.write(tag,r,packet); tag.put("Steam",steam.writeToNBT(r,new CompoundTag())); tag.putInt("Progress",progress); tag.putInt("Heat",heat); tag.putInt("Danger",danger); tag.putBoolean("Whistle",whistle); tag.putBoolean("Operating",operating); tag.putString("Status",status); tag.putInt("WhistleTime",whistleTime); tag.putInt("WhistleBurst",whistleBurst); }
    @Override protected void read(CompoundTag tag,HolderLookup.Provider r,boolean packet) { super.read(tag,r,packet); steam.readFromNBT(r,tag.getCompound("Steam")); progress=Math.max(0,tag.getInt("Progress")); heat=Math.clamp(tag.getInt("Heat"),0,2); danger=Math.max(0,tag.getInt("Danger")); whistle=tag.getBoolean("Whistle"); operating=tag.getBoolean("Operating"); status=tag.getString("Status"); if(status.isEmpty()) status="empty"; whistleTime=tag.getInt("WhistleTime"); whistleBurst=Math.clamp(tag.getInt("WhistleBurst"),0,90); }
}
