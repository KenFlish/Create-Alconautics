package dev.alconautics.client;

import dev.alconautics.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.registration.*;
import net.createmod.ponder.api.scene.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Tutorials built around the two structures supplied by the author. */
public final class AlconauticsPonder implements PonderPlugin {
    @Override public String getModId() { return Alconautics.ID; }
    @Override public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        for(String block:new String[]{"distillation_head","condenser_base"})
            helper.addStoryBoard(id(block),"apparatus",AlconauticsPonder::apparatus);
        helper.addStoryBoard(id("distillation_head"),"mash_and_dry_chamber",AlconauticsPonder::mash);
        helper.addStoryBoard(id("dry_chamber"),"mash_and_dry_chamber",AlconauticsPonder::mash);
    }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(Alconautics.ID,path); }
    private static void text(SceneBuilder s,SceneBuildingUtil u,String message,int x,int y,int z,ItemStack... icons) {
        var caption=s.overlay().showText(80).text(message);
        if(x<0) caption.independent();
        else caption.pointAt(u.vector().centerOf(x,y,z)).placeNearTarget();
        // Create's standard item-only input bubbles, aligned along the screen-facing diagonal.
        // Two blocks between anchors keep their native 1.5x item renderings apart.
        for(int i=0;i<icons.length;i++) {
            double offset=2*i-1;
            s.overlay().showControls(u.vector().of(offset,.5,offset),Pointing.DOWN,80).withItem(icons[i]);
        }
        s.idle(90);
    }
    private static void item(SceneBuilder s,SceneBuildingUtil u,ItemStack item,int x,int y,int z) {
        // An ingredient icon is not an instruction to use that item on the tank.
        s.overlay().showControls(u.vector().topOf(x,y,z),Pointing.DOWN,70).withItem(item);
    }
    private static void use(SceneBuilder s,SceneBuildingUtil u,ItemStack item,int x,int y,int z) {
        s.overlay().showControls(u.vector().topOf(x,y,z),Pointing.DOWN,70).rightClick().withItem(item);
    }
    private static void fill(SceneBuilder s,SceneBuildingUtil u,int x,int y,int z,Drink.Kind kind) {
        // Use scene-local coordinates; saved world controller positions are stripped on import.
        var base=new BlockPos(x,y,z);
        s.world().modifyBlockEntityNBT(u.select().position(base),FluidTankBlockEntity.class,tag -> {
            tag.putInt("Size",1);tag.putInt("Height",2);tag.put("LastKnownPos",net.minecraft.nbt.NbtUtils.writeBlockPos(base));tag.remove("Controller");
            tag.put("TankContent",Content.MASH.get(kind).stack(8000).save(s.world().getHolderLookupProvider()));
        });
        s.world().modifyBlockEntityNBT(u.select().position(base.above()),FluidTankBlockEntity.class,tag -> {
            tag.put("Controller",net.minecraft.nbt.NbtUtils.writeBlockPos(base));tag.put("LastKnownPos",net.minecraft.nbt.NbtUtils.writeBlockPos(base.above()));
        });
        s.world().modifyBlockEntity(base,FluidTankBlockEntity.class,tank -> {
            tank.setWidth(1);tank.setHeight(2);tank.applyFluidTankSize(2);
            tank.getTankInventory().setFluid(Content.MASH.get(kind).stack(8000));
        });
    }
    private static void heat(SceneBuilder s,BlockPos pos,boolean strong) {
        s.world().modifyBlock(pos,b -> b.setValue(BlazeBurnerBlock.HEAT_LEVEL,strong?BlazeBurnerBlock.HeatLevel.SEETHING:BlazeBurnerBlock.HeatLevel.KINDLED),false);
        s.world().modifyBlockEntity(pos.above(3),HeadBE.class,head -> {
            head.heat=strong?2:1;head.operating=true;head.whistle=true;head.status="running";
        });
    }
    private static void apparatus(SceneBuilder s,SceneBuildingUtil u) {
        s.title("apparatus","Distillation and cooling");
        s.configureBasePlate(0,0,5);s.scaleSceneView(.75f);s.setSceneOffsetY(-1);s.showBasePlate();s.idle(10);
        s.world().showSection(u.select().fromTo(2,1,3,2,3,3),Direction.DOWN);s.idle(20);
        // Stage 1: assemble the boiler. No tiny introductory chapter on the timeline.
        s.world().showSection(u.select().position(2,4,3),Direction.DOWN);s.idle(10);
        s.world().modifyBlock(new BlockPos(2,1,3),b -> b.setValue(BlazeBurnerBlock.HEAT_LEVEL,BlazeBurnerBlock.HeatLevel.SMOULDERING),false);
        s.world().modifyBlocks(u.select().fromTo(3,2,1,3,4,1),b -> b.setValue(BlockStateProperties.WATERLOGGED,false),false);
        text(s,u,"Place a Distillation Head on a tank above a Blaze Burner.",2,4,3);
        var whistle=new BlockPos(1,3,3);
        s.world().setBlock(whistle,AllBlocks.STEAM_WHISTLE.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.EAST).setValue(com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WALL,true),false);
        s.world().showSection(u.select().position(whistle),Direction.EAST);
        fill(s,u,2,2,3,Drink.Kind.SUGAR);
        text(s,u,"Pump mash into the tank. Add a Steam Whistle to prevent a pressure explosion.",1,3,3);
        // Stage 2: connect and waterlog the cooling pipes.
        s.addKeyframe();s.world().showSection(u.select().fromTo(0,1,0,4,6,2),Direction.DOWN);
        s.world().showSection(u.select().fromTo(2,5,3,2,6,3),Direction.DOWN);s.idle(20);
        text(s,u,"Connect the head to the top of the condenser with pipes. The head pumps the vapour.",3,1,1);
        use(s,u,new ItemStack(Items.WATER_BUCKET),3,2,1);
        s.world().modifyBlocks(u.select().fromTo(3,2,1,3,4,1),b -> b.setValue(BlockStateProperties.WATERLOGGED,true),false);
        text(s,u,"Waterlog at least two connected pipes above the condenser. Water stays outside the pipes.",3,3,1);
        // Stage 3: supply coolant through the actual block interaction.
        s.addKeyframe();use(s,u,new ItemStack(Items.ICE),3,1,1);
        s.world().modifyBlockEntity(new BlockPos(3,1,1),CoolerBE.class,c -> c.items.setStackInSlot(0,new ItemStack(Items.ICE)));
        text(s,u,"Right-click the condenser with snow or ice to supply cooling.",3,1,1);
        // Stage 4: run and collect. Both rates belong to this same stage.
        s.addKeyframe();heat(s,new BlockPos(2,1,3),false);
        text(s,u,"Heat the burner: 100 mB every 20 seconds, or every 13 seconds when superheated.",2,1,3);
        heat(s,new BlockPos(2,1,3),true);
        s.world().modifyBlockEntity(new BlockPos(3,1,1),CoolerBE.class,c -> c.output.setFluid(Content.SPIRITS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.NORMAL)).stack(500)));
        use(s,u,new ItemStack(Items.GLASS_BOTTLE),3,1,1);
        text(s,u,"Collect with glass bottles, or pump moonshine out through the sides or bottom.",3,1,1);
        s.markAsFinished();
    }
    private static void mash(SceneBuilder s,SceneBuildingUtil u) {
        s.title("mash_and_dry_chamber","Mash and herbs");
        s.configureBasePlate(0,0,5);s.scaleSceneView(.75f);s.setSceneOffsetY(-1);s.showBasePlate();
        s.rotateCameraY(90);
        s.world().showSection(u.select().fromTo(0,1,0,4,6,4).substract(u.select().position(1,4,3)),Direction.DOWN);s.idle(20);
        fill(s,u,1,2,1,Drink.Kind.SUGAR);
        // Stage 1: recipe information is independent, never a click hint on a tank.
        text(s,u,"Mix 1000 mB water, yeast and 4 ingredients in a Basin with a Mechanical Mixer.",-1,0,0);
        text(s,u,"Use sugar, fruit, vegetables or grain. Grain mash accepts wheat, oat or barley.",-1,0,0,
            new ItemStack(Items.SUGAR),new ItemStack(Items.APPLE),new ItemStack(Items.POTATO),new ItemStack(Items.WHEAT));
        // Stage 2: attach the modifier to the existing route.
        s.addKeyframe();s.world().showSection(u.select().position(1,4,3),Direction.UP);s.idle(15);
        text(s,u,"Attach a Dry Chamber underneath a pipe between the head and the condenser.",1,4,3);
        // Stage 3: demonstrate both mutually exclusive herbs without splitting the alternatives.
        s.addKeyframe();use(s,u,new ItemStack(Herbs.ribwort()),1,4,3);
        s.world().modifyBlockEntity(new BlockPos(1,4,3),DryBE.class,d -> d.items.setStackInSlot(0,new ItemStack(Herbs.ribwort())));
        text(s,u,"Insert ribwort for a longer effect, or nettle for a stronger one.",1,4,3);
        // Show the alternative; right-click cannot replace an occupied herb slot.
        item(s,u,new ItemStack(Herbs.nettle()),1,4,3);
        s.world().modifyBlockEntity(new BlockPos(1,4,3),DryBE.class,d -> d.items.setStackInSlot(0,new ItemStack(Herbs.nettle())));
        text(s,u,"One herb treats 1000 mB. The modifiers cannot be combined.",1,4,3);
        s.markAsFinished();
    }
}
