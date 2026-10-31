package dev.alconautics.test.client;

import dev.alconautics.*;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Development-only model render check; excluded from the released JAR. */
@EventBusSubscriber(modid=Alconautics.ID,value=Dist.CLIENT)
public final class VisualCheck {
    private static boolean started;
    private static boolean worldRequested;
    private static int ticks;
    private static List<Component> machineTooltip=List.of();
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("alconautics.visualCheck")) return;
        var mc=Minecraft.getInstance();
        boolean ponder=Boolean.getBoolean("alconautics.ponderCheck");
        if(ponder && !worldRequested && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
            worldRequested=true;
            String name="alconautics-ponder-check";
            var flows=mc.createWorldOpenFlows();
            if(new java.io.File(mc.gameDirectory,"saves/"+name+"/level.dat").exists()) flows.openWorld(name,() -> {});
            else flows.createFreshLevel(name,new net.minecraft.world.level.LevelSettings(name,net.minecraft.world.level.GameType.CREATIVE,false,net.minecraft.world.Difficulty.PEACEFUL,true,new net.minecraft.world.level.GameRules(),net.minecraft.world.level.WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(42,false,false),
                registries -> registries.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).createWorldDimensions(),new TitleScreen());
            return;
        }
        if(ponder && !started && mc.level!=null && mc.player!=null && mc.getOverlay()==null) {
            started=true;
            var ui=net.createmod.ponder.foundation.ui.PonderUI.of(Boolean.getBoolean("alconautics.renderDry")?net.minecraft.world.item.Items.STICK.getDefaultInstance():Content.COOLER_ITEM.get().getDefaultInstance());
            if(Boolean.getBoolean("alconautics.renderScene")) {
                mc.getWindow().setWindowed(1920,1080);
                mc.setScreen(new CleanRender(ui.getActiveScene()));
            } else mc.setScreen(ui);
        }
        if(!ponder && !started && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
            started=true;
            mc.setScreen(new Preview());
        }
        if(started && Boolean.getBoolean("alconautics.renderScene")) {
            ticks++;
            var ui=(CleanRender)mc.screen;
            if(ticks==5) ui.seekToTime(Boolean.getBoolean("alconautics.renderDry")?100:1100);
            String name=Boolean.getBoolean("alconautics.renderDry")?"dry-chamber":"apparatus";
            if(ticks==60) Screenshot.grab(mc.gameDirectory,"alconautics-"+name+"-dark.png",mc.getMainRenderTarget(),message -> System.out.println("ALCONAUTICS_RENDER: "+message.getString()));
            if(ticks==80) ui.light=true;
            if(ticks==110) Screenshot.grab(mc.gameDirectory,"alconautics-"+name+"-light.png",mc.getMainRenderTarget(),message -> System.out.println("ALCONAUTICS_RENDER: "+message.getString()));
            if(ticks==140) mc.stop();
            return;
        }
        if(started && Boolean.getBoolean("alconautics.ponderCheck")) {
            ticks++;
            if(ticks>380) {
                if(ticks==400) Screenshot.grab(mc.gameDirectory,"alconautics-0.1.13-details.png",mc.getMainRenderTarget(),message -> System.out.println("ALCONAUTICS_VISUAL_CHECK: "+message.getString()));
                if(ticks==420) { System.out.println("ALCONAUTICS_PONDER_CHECK: Both scenes and details completed");mc.stop(); }
                return;
            }
            if(!(mc.screen instanceof net.createmod.ponder.foundation.ui.PonderUI ui)) throw new IllegalStateException("Ponder UI did not open");
            if(ticks%10==0 && ticks<180) ui.seekToTime(Math.min(ticks*8,ui.getActiveScene().getTotalTime()-1));
            if(ticks==180) ui.seekToTime(ui.getActiveScene().getKeyframeTime(1)+25);
            if(ticks==185) Screenshot.grab(mc.gameDirectory,"alconautics-0.1.13-apparatus.png",mc.getMainRenderTarget(),message -> System.out.println("ALCONAUTICS_PONDER_CHECK: "+message.getString()));
            if(ticks==185) {
                var scene=ui.getActiveScene();
                checkChapters(ui,3,750);
                var head=(HeadBE)scene.getWorld().getBlockEntity(new net.minecraft.core.BlockPos(2,4,3));
                if(head.status.isEmpty()) throw new IllegalStateException("Empty apparatus status in Ponder");
                if(head.tank()==null) {
                    var bottom=(com.simibubi.create.content.fluids.tank.FluidTankBlockEntity)scene.getWorld().getBlockEntity(new net.minecraft.core.BlockPos(2,2,3));
                    var top=(com.simibubi.create.content.fluids.tank.FluidTankBlockEntity)scene.getWorld().getBlockEntity(new net.minecraft.core.BlockPos(2,3,3));
                    throw new IllegalStateException("Ponder tank controller invalid: bottom height="+bottom.getHeight()+" top controller="+top.getController()+" bottom position="+bottom.getBlockPos());
                }
                var info=new java.util.ArrayList<Component>();head.addToGoggleTooltip(info,false);machineTooltip=info;
                if(!scene.getWorld().getBlockState(new net.minecraft.core.BlockPos(3,2,1)).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) throw new IllegalStateException("Ponder cooling pipes were not waterlogged");
                if(scene.getWorld().getBlockEntity(new net.minecraft.core.BlockPos(3,1,1)) instanceof CoolerBE c && c.output.getFluidAmount()!=500) throw new IllegalStateException("Ponder output demonstration failed");
                System.out.println("ALCONAUTICS_PONDER_CHECK: "+scene.getTitle()+"; duration="+scene.getTotalTime());
            }
            if(ticks==200) mc.setScreen(net.createmod.ponder.foundation.ui.PonderUI.of(Content.DRY_ITEM.get().getDefaultInstance()));
            if(ticks>200 && ticks%10==0 && ticks<350) ui.seekToTime(Math.min((ticks-200)*8,ui.getActiveScene().getTotalTime()-1));
            if(ticks==300) ui.seekToTime(140);
            if(ticks==305) {
                checkRecipeHint(ui.getActiveScene());
                Screenshot.grab(mc.gameDirectory,"alconautics-0.1.13-mash.png",mc.getMainRenderTarget(),message -> System.out.println("ALCONAUTICS_PONDER_CHECK: "+message.getString()));
            }
            if(ticks==355) {
                var scene=ui.getActiveScene();
                checkChapters(ui,2,550);
                if(!(scene.getWorld().getBlockEntity(new net.minecraft.core.BlockPos(1,4,3)) instanceof DryBE d) || !d.items.getStackInSlot(0).is(Herbs.nettle())) throw new IllegalStateException("Ponder dry chamber demonstration failed");
                System.out.println("ALCONAUTICS_PONDER_CHECK: "+scene.getTitle()+"; duration="+scene.getTotalTime());
            }
            if(ticks==380) mc.setScreen(new DetailsPreview());
            return;
        }
        if(started && mc.screen instanceof Preview) {
            ticks++;
            if(ticks==60) Screenshot.grab(mc.gameDirectory,"alconautics-0.1.2-models.png",mc.getMainRenderTarget(),message -> System.out.println("ALCONAUTICS_VISUAL_CHECK: "+message.getString()));
            if(ticks==100) mc.stop();
        }
    }
    private static void checkChapters(net.createmod.ponder.foundation.ui.PonderUI ui,int separators,int maximumDuration) {
        var scene=ui.getActiveScene();
        if(scene.getKeyframeCount()!=separators || scene.getTotalTime()>maximumDuration) throw new IllegalStateException("Incorrect Ponder stages: "+scene.getKeyframeCount()+", duration="+scene.getTotalTime());
        int previous=0;
        for(int i=0;i<separators;i++) {
            int time=scene.getKeyframeTime(i);
            if(time-previous<90 || scene.getTotalTime()-time<90) throw new IllegalStateException("Ponder has an empty or crowded stage");
            ui.seekToTime(time+1);previous=time;
        }
        for(int i=separators-1;i>=0;i--) ui.seekToTime(scene.getKeyframeTime(i)+1);
        ui.seekToTime(scene.getTotalTime()-1);
    }
    private static void checkRecipeHint(net.createmod.ponder.foundation.PonderScene scene) {
        if(scene.getElements().stream().anyMatch(e -> e instanceof net.createmod.ponder.foundation.element.InputWindowElement))
            throw new IllegalStateException("Mash recipe must not suggest clicking items onto the tank");
        var cards=scene.getElements().stream().filter(e -> e instanceof dev.alconautics.client.PonderIngredientCard && e.isVisible()).map(e -> (dev.alconautics.client.PonderIngredientCard)e).toList();
        if(cards.size()!=1 || cards.getFirst().icons().size()!=4) throw new IllegalStateException("Mash must show one shared card containing four ingredients");
        var expected=List.of(net.minecraft.world.item.Items.SUGAR,net.minecraft.world.item.Items.APPLE,net.minecraft.world.item.Items.POTATO,net.minecraft.world.item.Items.WHEAT);
        for(int i=0;i<expected.size();i++) if(!cards.getFirst().icons().get(i).is(expected.get(i))) throw new IllegalStateException("Ingredient card has the wrong example order");
    }
    /** Native scene rendering, with no Ponder widgets, captions or game backdrop. */
    private static final class CleanRender extends net.createmod.ponder.foundation.ui.PonderUI {
        boolean light;
        CleanRender(net.createmod.ponder.foundation.PonderScene scene) { super(List.of(scene)); }
        @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial) {
            g.fillGradient(0,0,width,height,light?0xffe9eff2:0xff273b4c,light?0xffc9d6df:0xff101b25);
            g.pose().pushPose();
            boolean dry=Boolean.getBoolean("alconautics.renderDry");
            g.pose().translate(dry?-width*.17:0,height*.055,0);
            g.pose().translate(width/2.0,height/2.0,0);
            g.pose().scale(1.13f,1.13f,1.13f);
            g.pose().translate(-width/2.0,-height/2.0,0);
            renderScene(g,0,0,0,partial);
            g.pose().popPose();
            if(dry) DryRender.cards(g,font,width,height,light);
        }
    }
    private static final class DetailsPreview extends Screen {
        DetailsPreview() { super(Component.literal("Alconautics details check")); }
        @Override public void render(GuiGraphics g,int mx,int my,float partial) {
            g.fill(0,0,width,height,0xff303238);
            g.drawCenteredString(font,"Create: Alconautics 0.1.13",width/2,12,0xffffffff);
            g.renderComponentTooltip(font,machineTooltip,20,40);
            var drink=Content.DRINKS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.STRONG)).get();
            var lines=new java.util.ArrayList<Component>();lines.add(drink.getDefaultInstance().getHoverName());
            drink.appendHoverText(drink.getDefaultInstance(),net.minecraft.world.item.Item.TooltipContext.of(Minecraft.getInstance().level),lines,net.minecraft.world.item.TooltipFlag.NORMAL);
            g.renderComponentTooltip(font,lines,20,180);
            for(int i=0;i<2;i++) {
                var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("alconautics","textures/mob_effect/"+(i==0?"growth":"shrinking")+".png");
                g.blit(id,width-70+i*25,180,0,0,16,16,16,16);
            }
        }
    }
    private static final class Preview extends Screen {
        Preview() { super(Component.literal("Alconautics model check")); }
        @Override public void render(GuiGraphics g,int mx,int my,float partial) {
            g.fill(0,0,width,height,0xff303238);
            g.drawCenteredString(font,"Create: Alconautics 0.1.2 — model check",width/2,12,0xffffffff);
            var items=List.of(Content.HEAD_ITEM.get().getDefaultInstance(),Content.COOLER_ITEM.get().getDefaultInstance(),Content.DRY_ITEM.get().getDefaultInstance(),
                Content.DRINKS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.NORMAL)).get().getDefaultInstance(),
                Content.DRINKS.get(new Drink(Drink.Kind.FRUIT,Drink.Variant.NORMAL)).get().getDefaultInstance(),
                Content.DRINKS.get(new Drink(Drink.Kind.VEGETABLE,Drink.Variant.NORMAL)).get().getDefaultInstance());
            for(int i=0;i<items.size();i++) {
                int x=width/2-120+(i%3)*100,y=height/2-85+(i/3)*95;
                g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(3,3,3);g.renderItem(items.get(i),0,0);g.pose().popPose();
                g.drawCenteredString(font,items.get(i).getHoverName(),x+24,y+58,0xffeeeeee);
            }
        }
    }
}
