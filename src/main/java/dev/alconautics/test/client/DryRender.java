package dev.alconautics.test.client;

import dev.alconautics.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import net.createmod.ponder.api.registration.*;
import net.createmod.ponder.api.scene.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.gui.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Isolated promotional still; never included in the distributed mod. */
@EventBusSubscriber(modid=Alconautics.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class DryRender implements PonderPlugin {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        if(Boolean.getBoolean("alconautics.renderDry")) event.enqueueWork(() -> net.createmod.ponder.foundation.PonderIndex.addPlugin(new DryRender()));
    }
    @Override public String getModId() { return Alconautics.ID; }
    @Override public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.addStoryBoard(ResourceLocation.withDefaultNamespace("stick"),"mash_and_dry_chamber",DryRender::scene);
    }
    private static void scene(SceneBuilder s,SceneBuildingUtil u) {
        s.title("dry_chamber_render","Dry Chamber");s.configureBasePlate(0,0,5);s.scaleSceneView(.92f);s.setSceneOffsetY(-1);s.showBasePlate();
        s.world().setBlocks(u.select().fromTo(0,0,0,4,6,4),Blocks.AIR.defaultBlockState(),false);
        for(int x=0;x<5;x++) for(int z=0;z<5;z++)
            s.world().setBlock(new BlockPos(x,0,z),(x+z)%2==0?Blocks.SNOW_BLOCK.defaultBlockState():Blocks.WHITE_CONCRETE.defaultBlockState(),false);
        for(int x=0;x<5;x++) {
            var pipe=AllBlocks.FLUID_PIPE.getDefaultState();
            for(Direction direction:Direction.values()) pipe=pipe.setValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(direction),false);
            pipe=pipe.setValue(FluidPipeBlock.EAST,true).setValue(FluidPipeBlock.WEST,true);
            if(x==2) pipe=pipe.setValue(FluidPipeBlock.DOWN,true);
            s.world().setBlock(new BlockPos(x,3,2),pipe,false);
        }
        s.world().setBlock(new BlockPos(2,2,2),Content.DRY.get().defaultBlockState(),false);
        s.world().modifyBlockEntity(new BlockPos(2,2,2),DryBE.class,d -> d.items.setStackInSlot(0,new ItemStack(Herbs.ribwort())));
        for(int x:new int[]{1,3}) {
            var pos=new BlockPos(x,1,0);
            s.world().setBlock(pos,Content.BOTTLE.get().defaultBlockState().setValue(BottleBlock.COUNT,2),false);
            s.world().modifyBlockEntity(pos,BottleBE.class,b -> {
                b.placeFirst(Content.DRINKS.get(new Drink(x==1?Drink.Kind.SUGAR:Drink.Kind.GRAIN,Drink.Variant.NORMAL)).get().getDefaultInstance());
                b.addBottle(Content.DRINKS.get(new Drink(x==1?Drink.Kind.FRUIT:Drink.Kind.VEGETABLE,Drink.Variant.NORMAL)).get().getDefaultInstance());
            });
        }
        for(int x:new int[]{0,4}) {
            var pos=new BlockPos(x,1,1);
            s.world().setBlock(pos,Content.BOTTLE.get().defaultBlockState(),false);
            s.world().modifyBlockEntity(pos,BottleBE.class,b -> b.placeFirst(Content.DRINKS.get(new Drink(x==0?Drink.Kind.FRUIT:Drink.Kind.GRAIN,Drink.Variant.NORMAL)).get().getDefaultInstance()));
        }
        s.world().showSection(u.select().fromTo(0,1,0,4,4,4),Direction.DOWN);s.idle(200);s.markAsFinished();
    }
    static void cards(GuiGraphics g,Font font,int width,int height,boolean light) {
        int ink=light?0xff253d4b:0xffedf5f6,muted=light?0xff566c74:0xffa9bdc5;
        g.pose().pushPose();g.pose().translate(26,23,0);g.pose().scale(1.65f,1.65f,1.65f);
        g.drawString(font,"Dry Chamber",0,0,ink,false);g.pose().popPose();
        int x=(int)(width*.67),y=(int)(height*.31),w=width-x-25;
        label(g,font,"Herbs from Farm & Charm",x,y-22,w,muted);
        card(g,font,new ItemStack(Herbs.ribwort()),"Ribwort","Longer duration",x,y,w,ink,muted,light);
        card(g,font,new ItemStack(Herbs.nettle()),"Nettle","Stronger effect",x,y+66,w,ink,muted,light);
    }
    private static void card(GuiGraphics g,Font font,ItemStack item,String name,String effect,int x,int y,int width,int ink,int muted,boolean light) {
        g.fill(x-8,y-7,x+width,y+47,light?0x70ffffff:0x483a5461);
        g.pose().pushPose();g.pose().translate(x,y+3,0);g.pose().scale(2,2,2);g.renderItem(item,0,0);g.pose().popPose();
        label(g,font,name,x+40,y+6,width-44,ink);label(g,font,effect,x+40,y+23,width-44,muted);
    }
    private static void label(GuiGraphics g,Font font,String text,int x,int y,int width,int color) {
        float scale=Math.min(1,(float)width/font.width(text));
        g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);
        g.drawString(font,text,0,0,color,false);g.pose().popPose();
    }
}
