package dev.alconautics.client;

import dev.alconautics.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
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
    private static void text(SceneBuilder s,SceneBuildingUtil u,String message,int x,int y,int z) {
        s.overlay().showText(100).text(message).pointAt(u.vector().centerOf(x,y,z)).placeNearTarget();
        s.idle(115);
    }
    private static void item(SceneBuilder s,SceneBuildingUtil u,ItemStack item,int x,int y,int z) {
        s.overlay().showControls(u.vector().topOf(x,y,z),Pointing.DOWN,80).rightClick().withItem(item);
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
        s.title("apparatus","The distillation apparatus and condenser");
        s.configureBasePlate(0,0,5);s.scaleSceneView(.75f);s.setSceneOffsetY(-1);s.showBasePlate();s.idle(10);
        s.world().showSection(u.select().fromTo(2,1,3,2,3,3),Direction.DOWN);s.idle(20);
        fill(s,u,2,2,3,Drink.Kind.SUGAR);
        text(s,u,"Place a fluid tank above a Blaze Burner and fill it with mash.",2,2,3);
        s.addKeyframe();s.world().showSection(u.select().position(2,4,3),Direction.DOWN);s.idle(15);
        text(s,u,"A Distillation Head turns the tank into a distillation apparatus. It also pumps the vapour.",2,4,3);
        item(s,u,AllItems.WRENCH.asStack(),2,4,3);
        text(s,u,"Connect a pipe on top or to either side outlet. Rotate the head with a wrench to aim the side outlets.",2,4,3);
        var whistle=new BlockPos(1,3,3);
        s.world().setBlock(whistle,AllBlocks.STEAM_WHISTLE.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.EAST).setValue(com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WALL,true),false);
        s.world().showSection(u.select().position(whistle),Direction.EAST);
        text(s,u,"Attach a Steam Whistle to the tank. Heating without one builds pressure and can rupture the apparatus. Superheating builds pressure faster.",1,3,3);
        s.addKeyframe();s.world().showSection(u.select().fromTo(0,1,0,4,6,2),Direction.DOWN);
        s.world().showSection(u.select().fromTo(2,5,3,2,6,3),Direction.DOWN);s.idle(20);
        text(s,u,"Lead a single continuous pipe route into the top of the Condenser Base.",3,1,1);
        item(s,u,new ItemStack(Items.WATER_BUCKET),3,2,1);
        s.world().modifyBlocks(u.select().fromTo(3,2,1,3,4,1),b -> b.setValue(BlockStateProperties.WATERLOGGED,true),false);
        text(s,u,"Waterlog at least two connected pipes immediately above the condenser. The water surrounds the pipes; vapour travels inside.",3,3,1);
        s.overlay().showControls(u.vector().topOf(3,1,1),Pointing.DOWN,80).rightClick().withItem(new ItemStack(Items.ICE));
        s.world().modifyBlockEntity(new BlockPos(3,1,1),CoolerBE.class,c -> c.items.setStackInSlot(0,new ItemStack(Items.ICE)));
        text(s,u,"Insert snow or ice into the base with right-click. Cooling consumes this supply; snowflake particles indicate operation.",3,1,1);
        s.addKeyframe();heat(s,new BlockPos(2,1,3),false);
        text(s,u,"Normal heating produces 100 mB every 20 seconds. Superheating reduces the interval to 13 seconds.",2,1,3);
        heat(s,new BlockPos(2,1,3),true);
        s.world().modifyBlockEntity(new BlockPos(3,1,1),CoolerBE.class,c -> c.output.setFluid(Content.SPIRITS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.NORMAL)).stack(500)));
        item(s,u,new ItemStack(Items.GLASS_BOTTLE),3,1,1);
        text(s,u,"Collect moonshine with glass bottles, or pump it out through any side except the top. A Spout can fill bottles automatically.",3,1,1);
        item(s,u,AllItems.GOGGLES.asStack(),2,3,3);
        text(s,u,"Engineer’s Goggles show the apparatus status, mash level, heating and mash type.",2,3,3);
        s.markAsFinished();
    }
    private static void mash(SceneBuilder s,SceneBuildingUtil u) {
        s.title("mash_and_dry_chamber","Mash varieties and the Dry Chamber");
        s.configureBasePlate(0,0,5);s.scaleSceneView(.75f);s.setSceneOffsetY(-1);s.showBasePlate();
        s.rotateCameraY(90);
        s.world().showSection(u.select().fromTo(0,1,0,4,6,4).substract(u.select().position(1,4,3)),Direction.DOWN);s.idle(20);
        fill(s,u,1,2,1,Drink.Kind.SUGAR);
        text(s,u,"Mix 1000 mB of water, yeast from Farm & Charm and four ingredients in a Basin with a Mechanical Mixer.",1,2,1);
        item(s,u,new ItemStack(Items.SUGAR),1,2,1);
        text(s,u,"Use four portions of sugar to make sugar mash.",1,2,1);
        s.addKeyframe();fill(s,u,1,2,1,Drink.Kind.FRUIT);item(s,u,new ItemStack(Items.APPLE),1,2,1);
        text(s,u,"Fruit makes fruit mash. Add four fruits, such as apples.",1,2,1);
        fill(s,u,1,2,1,Drink.Kind.VEGETABLE);item(s,u,new ItemStack(Items.POTATO),1,2,1);
        text(s,u,"Vegetables make vegetable mash. Add four vegetables: potatoes, carrots or beetroot.",1,2,1);
        fill(s,u,1,2,1,Drink.Kind.GRAIN);item(s,u,new ItemStack(Items.WHEAT),1,2,1);
        text(s,u,"Use four portions of wheat, oat or barley to make grain mash.",1,2,1);
        s.addKeyframe();s.world().showSection(u.select().position(1,4,3),Direction.UP);s.idle(15);
        text(s,u,"Attach a Dry Chamber underneath a pipe between the head and the condenser. It is an optional modifier, not a separate pipe route.",1,4,3);
        item(s,u,new ItemStack(Herbs.ribwort()),1,4,3);
        s.world().modifyBlockEntity(new BlockPos(1,4,3),DryBE.class,d -> d.items.setStackInSlot(0,new ItemStack(Herbs.ribwort())));
        text(s,u,"Ribwort extends the duration of a drink’s positive effect.",1,4,3);
        s.addKeyframe();item(s,u,new ItemStack(Herbs.nettle()),1,4,3);
        s.world().modifyBlockEntity(new BlockPos(1,4,3),DryBE.class,d -> d.items.setStackInSlot(0,new ItemStack(Herbs.nettle())));
        text(s,u,"Nettle grants effect level II for 90 seconds. Choose duration or strength; these modifiers do not stack.",1,4,3);
        text(s,u,"One herb treats 1000 mB. Vegetable moonshine has no positive effect to enhance and does not consume the modifier.",1,4,3);
        s.markAsFinished();
    }
}
