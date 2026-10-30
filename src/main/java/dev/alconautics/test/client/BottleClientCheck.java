package dev.alconautics.test.client;

import dev.alconautics.*;
import dev.alconautics.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Real integrated-server packets and native Foundation overlay; not shipped. */
@EventBusSubscriber(modid=Alconautics.ID,value=Dist.CLIENT)
public final class BottleClientCheck {
    private static boolean requested,started;
    private static int ticks;
    private static final BlockPos POS=new BlockPos(8,-59,-5);
    private static void require(boolean value,String message) { if(!value) throw new IllegalStateException(message); }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("alconautics.bottleCheck")) return;
        var mc=Minecraft.getInstance();
        if(!requested && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
            requested=true;
            mc.options.languageCode="ru_ru";
            mc.getLanguageManager().setSelected("ru_ru");
            String name="alconautics-ponder-check";
            var flows=mc.createWorldOpenFlows();
            if(new java.io.File(mc.gameDirectory,"saves/"+name+"/level.dat").exists()) flows.openWorld(name,() -> {});
            else flows.createFreshLevel(name,new net.minecraft.world.level.LevelSettings(name,net.minecraft.world.level.GameType.CREATIVE,false,net.minecraft.world.Difficulty.PEACEFUL,true,new net.minecraft.world.level.GameRules(),net.minecraft.world.level.WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(42,false,false),
                registries -> registries.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).createWorldDimensions(),new TitleScreen());
            return;
        }
        if(mc.level==null || mc.player==null || mc.getOverlay()!=null) return;
        if(!started) {
            started=true;mc.setScreen(null);mc.getWindow().setWindowed(1280,720);
            mc.options.hideGui=false;mc.options.bobView().set(false);
            mc.getSingleplayerServer().execute(() -> {
                var server=mc.getSingleplayerServer();var level=server.overworld();
                level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
                for(int x=5;x<=11;x++) for(int z=-7;z<=0;z++) {
                    level.setBlockAndUpdate(new BlockPos(x,-60,z),Blocks.SMOOTH_STONE.defaultBlockState());
                    for(int y=-59;y<-55;y++) level.removeBlock(new BlockPos(x,y,z),false);
                }
                level.setBlockAndUpdate(POS,Content.BOTTLE.get().defaultBlockState());
                ((BottleBE)level.getBlockEntity(POS)).placeFirst(Content.DRINKS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.LONG)).get().getDefaultInstance());
                server.getPlayerList().getPlayers().forEach(player -> player.teleportTo(8.5,-60,-2.7));
            });
        }
        ticks++;
        mc.player.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(POS).add(0,.05,0));
        if(ticks==80) {
            require(mc.level.getBlockEntity(POS) instanceof BottleBE,"Bottle packet missing");
            require(!((BottleBE)mc.level.getBlockEntity(POS)).getInventory().getFirst().isEmpty(),"Single inventory not synchronized");
            for(var id:new net.minecraft.client.resources.model.ModelResourceLocation[]{BottleRenderer.SINGLE,BottleRenderer.FIRST,BottleRenderer.SECOND})
                require(mc.getModelManager().getModel(id)!=mc.getModelManager().getMissingModel(),"Missing bottle model: "+id);
            screenshot("single");
        }
        if(ticks==90) mc.getSingleplayerServer().execute(() -> ((BottleBE)mc.getSingleplayerServer().overworld().getBlockEntity(POS)).addBottle(Content.DRINKS.get(new Drink(Drink.Kind.FRUIT,Drink.Variant.STRONG)).get().getDefaultInstance()));
        if(ticks==95) {
            var bottles=(BottleBE)mc.level.getBlockEntity(POS);
            require(bottles.getInventory().get(1).getItem() instanceof MoonshineItem item && item.drink.kind()==Drink.Kind.FRUIT,"Second slot packet missing");
            require(bottles.getWobbleStart(1)>0,"Foundation placement wobble did not start");
            require(mc.level.getGameTime()-bottles.getWobbleStart(1)<20,"Stale wobble timestamp");
            require(net.satisfy.foundation.client.render.WobbleAnimation.isActive(mc.level,bottles.getWobbleStart()),"Shared wobble for both bottles missing");
            screenshot("wobble");
            System.out.println("ALCONAUTICS_BOTTLE_CHECK: live second-slot packet and native wobble received");
        }
        if(ticks==140) {
            var hit=new BlockHitResult(Vec3.atCenterOf(POS),Direction.SOUTH,POS,false);
            require(!new BottleInfo().describe(mc.level,POS,mc.level.getBlockState(POS),hit).isEmpty(),"Native overlay missing");
            var create=com.simibubi.create.AllCreativeModeTabs.BASE_CREATIVE_TAB.get();
            var brewery=net.satisfy.brewery.core.registry.TabRegistry.BREWERY_TAB.get();
            net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(mc.level.enabledFeatures(),true,mc.level.registryAccess());
            require(mc.getMobEffectTextures().get(Content.COURAGE).contents().name().toString().equals("alconautics:courage"),"Courage effect icon missing");
            require(create.contains(Content.HEAD_ITEM.get().getDefaultInstance()) && create.contains(Content.DRY_ITEM.get().getDefaultInstance()),"Machines missing from Create tab");
            Content.DRINKS.forEach((drink,item) -> require(brewery.contains(item.get().getDefaultInstance())==(drink.variant()==Drink.Variant.NORMAL),"Wrong Brewery drink tab entry: "+drink));
            screenshot("pair");
        }
        if(ticks==150) mc.getSingleplayerServer().execute(() -> ((BottleBE)mc.getSingleplayerServer().overworld().getBlockEntity(POS)).takeBottle(0));
        if(ticks==190) {
            var bottles=(BottleBE)mc.level.getBlockEntity(POS);
            require(bottles.getBlockState().getValue(BottleBlock.COUNT)==1,"Single shape not synchronized");
            require(bottles.getInventory().get(1).isEmpty() && ((MoonshineItem)bottles.getInventory().get(0).getItem()).drink.kind()==Drink.Kind.FRUIT,"Compaction packet incorrect");
            screenshot("remaining");
            System.out.println("ALCONAUTICS_BOTTLE_CHECK: single/pair/remaining models, inventory packets, overlay and creative tabs passed");
        }
        if(ticks==200) mc.getSingleplayerServer().execute(() -> ((BottleBE)mc.getSingleplayerServer().overworld().getBlockEntity(POS)).addBottle(Content.DRINKS.get(new Drink(Drink.Kind.FRUIT,Drink.Variant.NORMAL)).get().getDefaultInstance()));
        if(ticks==225) {
            var hit=new BlockHitResult(Vec3.atCenterOf(POS),Direction.SOUTH,POS,false);
            require(new BottleInfo().describe(mc.level,POS,mc.level.getBlockState(POS),hit).getFirst().rows().size()==2,"Identical drinks must show two separate bottle rows");
            screenshot("identical");
            mc.getSingleplayerServer().execute(() -> VapourCloud.spawn(mc.getSingleplayerServer().overworld(),Vec3.atCenterOf(POS).add(0,1,0),1.4f,120));
        }
        if(ticks==265) screenshot("cloud");
        if(ticks==285) mc.stop();
    }
    private static void screenshot(String name) {
        var mc=Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory,"alconautics-0.1.7-"+name+".png",mc.getMainRenderTarget(),message -> System.out.println("ALCONAUTICS_BOTTLE_CHECK: "+message.getString()));
    }
}
