package dev.alconautics.test;

import dev.alconautics.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("alconautics")
@PrefixGameTestTemplate(false)
public class DistilleryTests {
    @GameTest(template="empty",timeoutTicks=1400)
    public static void normal_heat_pressure_and_rupture(GameTestHelper h) {
        setup(h,false);
        h.runAtTickTime(650,() -> {
            HeadBE head=h.getBlockEntity(HEAD);
            h.assertTrue(head!=null && head.danger>500,"Normal heating must accumulate pressure without immediate rupture");
        });
        h.runAtTickTime(1300,() -> {
            h.assertTrue(!h.getBlockState(HEAD).is(Content.HEAD.get()),"Normal heating without a whistle must eventually rupture");
            h.assertTrue(!h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.AreaEffectCloud.class,new net.minecraft.world.phys.AABB(h.absolutePos(TANK)).inflate(4)).isEmpty(),"Rupture must release an intoxicating cloud");
            h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void hand_loading_and_compact_tooltip(GameTestHelper h) {
        setup(h,false);
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.ICE,3));
        CoolerBE cooler=h.getBlockEntity(COOLER);
        h.assertTrue(!player.isShiftKeyDown() && cooler.interact(player,net.minecraft.world.InteractionHand.MAIN_HAND),"Condenser must accept hand loading without Shift");
        h.assertTrue(cooler.items.getStackInSlot(0).getCount()==3 && player.getMainHandItem().isEmpty(),"Hand loading must transfer the ice");
        h.runAtTickTime(30,() -> {
            HeadBE head=h.getBlockEntity(HEAD);
            var lines=new java.util.ArrayList<net.minecraft.network.chat.Component>();head.addToGoggleTooltip(lines,false);
            h.assertTrue(lines.size()==6 && lines.get(4).getString().isEmpty(),"Status hint must be separated at the bottom");
            var hint=lines.getLast().getSiblings().getFirst();
            h.assertTrue(hint.getStyle().getColor().equals(net.minecraft.network.chat.TextColor.fromLegacyFormat(net.minecraft.ChatFormatting.GRAY)),"Bottom hint must be gray");
            h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void liquid_effect_details(GameTestHelper h) {
        h.setBlock(TANK,AllBlocks.FLUID_TANK.get());
        h.runAtTickTime(15,() -> {
            FluidTankBlockEntity tank=h.getBlockEntity(TANK);
            tank.getTankInventory().fill(Content.SPIRITS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.STRONG)).stack(1000),FluidAction.EXECUTE);
            var lines=new java.util.ArrayList<net.minecraft.network.chat.Component>();DrinkTooltip.append(new Drink(Drink.Kind.SUGAR,Drink.Variant.STRONG),lines::add,20);
            h.assertTrue(lines.stream().anyMatch(c -> c.getString().contains("II")),"Stored moonshine must show its effect level using potion formatting");
            h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=10)
    public static void model_collision_shapes(GameTestHelper h) {
        var context=net.minecraft.world.phys.shapes.CollisionContext.empty();
        h.setBlock(1,2,1,Content.DRY.get());
        var pos=h.absolutePos(new BlockPos(1,2,1));
        var dry=h.getLevel().getBlockState(pos);
        var shape=dry.getShape(h.getLevel(),pos,context);
        h.assertTrue(!touches(shape,3.5,15,3.5),"Dry shoulder must not fill empty model space");
        h.assertTrue(touches(shape,8,15,8),"Dry neck must have a hitbox");
        h.assertTrue(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(shape,dry.getCollisionShape(h.getLevel(),pos,context),net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME),"Selection and collision must match");
        for(Direction facing:Direction.Plane.HORIZONTAL) {
            var head=Content.HEAD.get().defaultBlockState().setValue(MachineBlock.FACING,facing);
            shape=head.getShape(h.getLevel(),pos,context);
            double x=6.5,z=15;
            for(int n=0;n<switch(facing) {case SOUTH -> 1;case WEST -> 2;case NORTH -> 3;default -> 0;};n++) {double old=x;x=16-z;z=old;}
            h.assertTrue(touches(shape,x,6.5,z),"Rotated gauge protrusion missing: "+facing);
            h.assertTrue(touches(shape,8,16.5,8),"Upper head socket missing");
            h.assertTrue(!touches(shape,1.5,12,1.5),"Head hitbox fills empty corners");
        }
        h.succeed();
    }
    private static boolean touches(net.minecraft.world.phys.shapes.VoxelShape shape,double x,double y,double z) {
        return net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(shape,net.minecraft.world.phys.shapes.Shapes.box((x-.01)/16,(y-.01)/16,(z-.01)/16,(x+.01)/16,(y+.01)/16,(z+.01)/16),net.minecraft.world.phys.shapes.BooleanOp.AND);
    }
    private static final BlockPos TANK=new BlockPos(1,2,1), HEAD=TANK.above(), COOLER=new BlockPos(4,2,1);
    private static void setup(GameTestHelper h,boolean superheated) {
        h.setBlock(TANK.below(),AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL,superheated?BlazeBurnerBlock.HeatLevel.SEETHING:BlazeBurnerBlock.HeatLevel.KINDLED));
        ((com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity)h.getBlockEntity(TANK.below())).isCreative=true;
        h.setBlock(TANK,AllBlocks.FLUID_TANK.get()); h.setBlock(HEAD,Content.HEAD.get()); h.setBlock(COOLER,Content.COOLER.get());
        h.setBlock(1,4,1,pipe(Direction.DOWN,Direction.EAST,false));
        h.setBlock(2,4,1,pipe(Direction.WEST,Direction.EAST,false));
        h.setBlock(3,4,1,pipe(Direction.WEST,Direction.EAST,false));
        h.setBlock(4,4,1,pipe(Direction.WEST,Direction.DOWN,true));
        h.setBlock(4,3,1,pipe(Direction.UP,Direction.DOWN,true));
        h.runAtTickTime(10,() -> {
            FluidTankBlockEntity tank=h.getBlockEntity(TANK);
            tank.getTankInventory().fill(Content.MASH.get(Drink.Kind.SUGAR).stack(1000),FluidAction.EXECUTE);
            CoolerBE cooler=h.getBlockEntity(COOLER);cooler.items.setStackInSlot(0,new ItemStack(Items.ICE));
        });
    }
    private static net.minecraft.world.level.block.state.BlockState pipe(Direction a,Direction b,boolean wet) {
        var s=AllBlocks.FLUID_PIPE.getDefaultState();
        for(Direction d:Direction.values()) s=s.setValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(d),d==a || d==b);
        return s.setValue(BlockStateProperties.WATERLOGGED,wet);
    }
    @GameTest(template="empty",timeoutTicks=650)
    public static void normal_transport(GameTestHelper h) {
        setup(h,false);
        h.runAtTickTime(250,() -> { CoolerBE c=h.getBlockEntity(COOLER); h.assertTrue(c.output.isEmpty(),"Must not produce before 20 seconds"); });
        h.runAtTickTime(580,() -> {
            HeadBE head=h.getBlockEntity(HEAD); CoolerBE cooler=h.getBlockEntity(COOLER); FluidTankBlockEntity tank=h.getBlockEntity(TANK);
            h.assertTrue(cooler.output.getFluidAmount()==100,"Expected 100 mB through real Create pipes, got "+cooler.output.getFluidAmount()+"; status="+head.status+"; steam="+head.steam.getFluidAmount()+"; input="+cooler.input.getFluidAmount());
            h.assertTrue(tank.getTankInventory().getFluidAmount()==900,"Mash must be consumed 1:1");
            h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=650)
    public static void side_outlet_transport(GameTestHelper h) {
        setup(h,false);
        for(int x=1;x<=4;x++) h.setBlock(x,4,1,Blocks.AIR);
        h.setBlock(2,3,1,pipe(Direction.WEST,Direction.EAST,false));
        h.setBlock(3,3,1,pipe(Direction.WEST,Direction.EAST,true));
        h.setBlock(4,3,1,pipe(Direction.WEST,Direction.DOWN,true));
        h.runAtTickTime(580,() -> {
            HeadBE head=h.getBlockEntity(HEAD);CoolerBE cooler=h.getBlockEntity(COOLER);
            h.assertTrue(cooler.output.getFluidAmount()==100,"Side outlet did not supply Create pipes: "+head.status);
            h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void head_wrench_rotates_ports(GameTestHelper h) {
        h.setBlock(TANK,AllBlocks.FLUID_TANK.get());h.setBlock(HEAD,Content.HEAD.get());
        var level=h.getLevel();var pos=h.absolutePos(HEAD);
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        var context=new net.minecraft.world.item.context.UseOnContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,
            new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),Direction.UP,pos,false));
        for(int i=0;i<4;i++) {
            var state=level.getBlockState(pos);var face=state.getValue(MachineBlock.FACING);
            h.assertTrue(level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,pos,face)!=null,"Facing outlet unavailable");
            h.assertTrue(level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,pos,face.getClockWise())==null,"Closed side accepts pipe");
            Content.HEAD.get().onWrenched(state,context);
            h.assertTrue(level.getBlockState(pos).getValue(MachineBlock.FACING)==face.getClockWise(),"Wrench did not rotate around Y");
        }
        h.assertTrue(Goggles.bars(2).getString().equals("|||"),"Goggles must have exactly three divisions");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=500)
    public static void superheated_transport(GameTestHelper h) {
        setup(h,true);
        h.runAtTickTime(430,() -> { CoolerBE c=h.getBlockEntity(COOLER); h.assertTrue(c.output.getFluidAmount()==100,"Superheat must produce 100 mB in 13 seconds plus pipe transit"); h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=600)
    public static void dry_chamber_modifier(GameTestHelper h) {
        setup(h,false);h.setBlock(2,3,1,Content.DRY.get());
        DryBE jar=h.getBlockEntity(new BlockPos(2,3,1)); jar.items.setStackInSlot(0,new ItemStack(Herbs.nettle()));
        h.runAtTickTime(30,() -> h.assertTrue(h.getBlockState(new BlockPos(2,4,1)).getValue(FluidPipeBlock.DOWN),"Pipe must connect visually to dry chamber top"));
        h.runAtTickTime(580,() -> { CoolerBE c=h.getBlockEntity(COOLER); h.assertTrue(new Drink(Drink.Kind.SUGAR,Drink.Variant.STRONG).equals(Content.spirit(c.output.getFluid().getFluid())),"Glowstone modifier lost in transport"); h.assertTrue(jar.items.getStackInSlot(0).isEmpty(),"Modifier not consumed"); h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=600)
    public static void needs_two_waterlogged_pipes(GameTestHelper h) {
        setup(h,false);
        h.runAtTickTime(30,() -> h.setBlock(4,4,1,pipe(Direction.WEST,Direction.DOWN,false)));
        h.runAtTickTime(550,() -> { CoolerBE c=h.getBlockEntity(COOLER);h.assertTrue(c.output.isEmpty(),"Dry pipe must stop condensation");h.assertTrue(c.items.getStackInSlot(0).getCount()==1,"Idle condenser consumed ice"); h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=850)
    public static void local_rupture(GameTestHelper h) {
        setup(h,true);h.setBlock(2,2,2,Blocks.DIAMOND_BLOCK);
        h.runAtTickTime(760,() -> { h.assertTrue(!h.getBlockState(TANK).is(AllBlocks.FLUID_TANK.get()),"Superheated tank without whistle must rupture");h.assertTrue(h.getBlockState(new BlockPos(2,2,2)).is(Blocks.DIAMOND_BLOCK),"Rupture damaged surrounding building"); h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void cooling_persistence(GameTestHelper h) {
        h.setBlock(COOLER,Content.COOLER.get());
        h.setBlock(4,3,1,pipe(Direction.UP,Direction.DOWN,true));
        h.setBlock(4,4,1,pipe(Direction.WEST,Direction.DOWN,true));
        CoolerBE c=h.getBlockEntity(COOLER);
        c.items.setStackInSlot(0,new ItemStack(Items.ICE));
        var drink=new Drink(Drink.Kind.FRUIT,Drink.Variant.LONG);
        c.input.fill(Content.VAPOUR.get(drink).stack(100),FluidAction.EXECUTE);
        c.tick();
        var saved=c.saveWithoutMetadata(h.getLevel().registryAccess());
        var restored=new CoolerBE(h.absolutePos(COOLER),Content.COOLER.get().defaultBlockState());
        restored.loadWithComponents(saved,h.getLevel().registryAccess());
        h.assertTrue(restored.output.getFluidAmount()==100,"Output volume lost on reload");
        h.assertTrue(drink.equals(Content.spirit(restored.output.getFluid().getFluid())),"Drink variant lost on reload");
        h.assertTrue(restored.saveWithoutMetadata(h.getLevel().registryAccess()).getInt("Cold")==1900,"Cold credit lost or spent while idle");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200)
    public static void size_and_drinking(GameTestHelper h) {
        var entity=h.spawn(net.minecraft.world.entity.EntityType.COW,new BlockPos(3,2,3));entity.setNoAi(true);entity.setNoGravity(true);
        float baseHealth=entity.getMaxHealth();
        var bottle=Content.DRINKS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.NORMAL)).get();
        bottle.finishUsingItem(bottle.getDefaultInstance(),h.getLevel(),entity);
        h.runAtTickTime(10,() -> {
            double scale=entity.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
            h.assertTrue(scale>1 && scale<1.5,"Growth must be gradual");
            h.assertTrue(entity.hasEffect(BreweryCompat.drunkEffect().orElseThrow()),"Brewery intoxication missing");
        });
        h.runAtTickTime(60,() -> {
            h.assertTrue(Math.abs(entity.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE)-1.5)<.001,"Growth did not reach target");
            h.assertTrue(Math.abs(entity.getMaxHealth()-baseHealth*1.125)<.01,"Growth health bonus missing");
            entity.removeEffect(Content.GROW);
        });
        h.runAtTickTime(110,() -> {h.assertTrue(Math.abs(entity.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE)-1)<.001,"Scale did not reset");h.assertTrue(Math.abs(entity.getMaxHealth()-baseHealth)<.001,"Health modifier leaked");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=850)
    public static void whistle_prevents_rupture(GameTestHelper h) {
        setup(h,true);
        h.setBlock(1,2,0,AllBlocks.STEAM_WHISTLE.getDefaultState().setValue(com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.FACING,Direction.SOUTH).setValue(com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WALL,true));
        h.runAtTickTime(760,() -> { HeadBE head=h.getBlockEntity(HEAD);h.assertTrue(head!=null && head.whistle,"Whistle not detected");h.assertTrue(head.danger==0,"Safety whistle did not vent pressure");h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=200)
    public static void condenser_stays_active_between_batches(GameTestHelper h) {
        setup(h,false);
        h.runAtTickTime(100,() -> { CoolerBE c=h.getBlockEntity(COOLER);h.assertTrue(c.isWorking(),"Condenser must stay active while awaiting next batch");h.assertTrue(c.output.isEmpty(),"Test must check the interval before production");h.setBlock(HEAD,Blocks.AIR); });
        h.runAtTickTime(150,() -> {CoolerBE c=h.getBlockEntity(COOLER);h.assertTrue(!c.isWorking(),"Condenser must stop after head removal");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=250)
    public static void brewery_nausea_is_intermittent(GameTestHelper h) {
        var entity=h.spawn(net.minecraft.world.entity.EntityType.COW,new BlockPos(3,2,3));entity.setNoAi(true);entity.setNoGravity(true);
        var effect=BreweryCompat.drunkEffect().orElseThrow();
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect,1000,4));
        entity.tickCount=40;
        effect.value().applyEffectTick(entity,4);
        var nausea=entity.getEffect(net.minecraft.world.effect.MobEffects.CONFUSION);
        h.assertTrue(nausea!=null && nausea.getAmplifier()<=1 && nausea.getDuration()<=80,"Brewery nausea must be short and at most level II");
        entity.removeEffect(net.minecraft.world.effect.MobEffects.CONFUSION);
        effect.value().applyEffectTick(entity,4);
        h.assertTrue(!entity.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION),"Repeated Brewery tick must respect nausea cooldown");
        h.runAtTickTime(160,() -> {h.assertTrue(!entity.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION),"There must be a sober interval between nausea bursts");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void intoxication_decays_without_hidden_levels(GameTestHelper h) {
        var entity=h.spawn(net.minecraft.world.entity.EntityType.COW,new BlockPos(3,2,3));entity.setNoAi(true);entity.setNoGravity(true);
        BreweryCompat.drink(entity);BreweryCompat.drink(entity);BreweryCompat.drink(entity);
        var effect=BreweryCompat.drunkEffect().orElseThrow();
        h.assertTrue(entity.getEffect(effect).getAmplifier()==2,"Three drinks must produce intoxication III");
        entity.removeEffect(effect);entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect,2,2));
        h.runAtTickTime(5,() -> {var current=entity.getEffect(effect);h.assertTrue(current!=null && current.getAmplifier()==1,"Intoxication must decay by one level");h.assertTrue(current.getDuration()>1100,"Lower level must get a new sobering interval");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=850)
    public static void residual_vapour_still_builds_pressure(GameTestHelper h) {
        setup(h,true);
        h.runAtTickTime(20,() -> {
            FluidTankBlockEntity tank=h.getBlockEntity(TANK);tank.getTankInventory().drain(1000,FluidAction.EXECUTE);
            HeadBE head=h.getBlockEntity(HEAD);head.steam.fill(Content.VAPOUR.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.NORMAL)).stack(100),FluidAction.EXECUTE);
            h.setBlock(1,4,1,Blocks.AIR);
        });
        h.runAtTickTime(760,() -> {h.assertTrue(!h.getBlockState(HEAD).is(Content.HEAD.get()),"Retained hot vapour must rupture an unvented apparatus even after mash runs out");h.succeed();});
    }
}
