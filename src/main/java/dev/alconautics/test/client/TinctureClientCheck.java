package dev.alconautics.test.client;
import dev.alconautics.*;
import dev.alconautics.client.BottleInfo;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=Alconautics.ID,value=Dist.CLIENT)
public final class TinctureClientCheck {
    private static boolean requested,started;private static int ticks;
    private static BlockPos pos(int i) { return new BlockPos(8+2*i,-59,-5); }
    private static void require(boolean value,String message) { if(!value) throw new IllegalStateException(message); }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("alconautics.tinctureCheck")) return;
        var mc=Minecraft.getInstance();
        if(!requested && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
            requested=true;var flows=mc.createWorldOpenFlows();String name="alconautics-tincture-check";
            if(new java.io.File(mc.gameDirectory,"saves/"+name+"/level.dat").exists()) flows.openWorld(name,() -> {});
            else flows.createFreshLevel(name,new net.minecraft.world.level.LevelSettings(name,net.minecraft.world.level.GameType.CREATIVE,false,net.minecraft.world.Difficulty.PEACEFUL,true,new net.minecraft.world.level.GameRules(),net.minecraft.world.level.WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(42,false,false),registries -> registries.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).createWorldDimensions(),new TitleScreen());
            return;
        }
        if(mc.level==null || mc.player==null || mc.getOverlay()!=null) return;
        if(!started) {
            started=true;mc.getWindow().setWindowed(1920,1080);mc.setScreen(null);
            mc.getSingleplayerServer().execute(() -> {
                var level=mc.getSingleplayerServer().overworld();
                mc.getSingleplayerServer().getPlayerList().getPlayers().forEach(player -> player.teleportTo(12.5,-60,-2));
                for(int i=0;i<6;i++) {
                    var position=pos(i);level.setBlockAndUpdate(position,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(position,Content.TINCTURE_BOTTLES.get().defaultBlockState());
                    var be=(BottleBE)level.getBlockEntity(position);var kind=i<3?Tincture.DANDELION:Tincture.BERRY;
                    var first=Content.TINCTURE_ITEMS.get(kind).get().getDefaultInstance();
                    if(i==0) TinctureAge.update(first,TinctureAge.now(level)-Config.TINCTURE_DAYS_PER_YEAR.get()*24000L*5);
                    be.placeFirst(first);
                    int count=i<3?i+1:i==3?1:2;
                    for(int j=1;j<count;j++) be.addBottle(Content.TINCTURE_ITEMS.get(i==5?Tincture.DANDELION:kind).get().getDefaultInstance());
                }
            });
        }
        ticks++;
        if(ticks==70) {
            for(var name:TinctureGeometry.MODELS) require(mc.getModelManager().getModel(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"block/"+name)))!=mc.getModelManager().getMissingModel(),"Missing model "+name);
            for(int i=0;i<6;i++) {
                require(mc.level.getBlockEntity(pos(i)) instanceof BottleBE,"Missing tincture packet "+i);
                var be=(BottleBE)mc.level.getBlockEntity(pos(i));require(TinctureGeometry.layout(be.getInventory())==i,"Incorrect layout packet "+i);
                var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos(i)),Direction.SOUTH,pos(i),false);
                require(new BottleInfo().describe(mc.level,pos(i),be.getBlockState(),hit).getFirst().rows().size()==TinctureGeometry.CENTERS[i].length,"Overlay must show every bottle separately");
            }
            require(TinctureAge.years(((BottleBE)mc.level.getBlockEntity(pos(0))).getInventory().getFirst())==5,"Age not synchronized");
            mc.setScreen(new Preview());
        }
        if(ticks==100) Screenshot.grab(mc.gameDirectory,"alconautics-0.1.9-tinctures.png",mc.getMainRenderTarget(),message -> System.out.println(message.getString()));
        if(ticks==120) { System.out.println("ALCONAUTICS_TINCTURE_CHECK: all six layouts, 11 models, per-bottle overlays and server age packets passed");mc.stop(); }
    }
    private static final class Preview extends Screen {
        Preview() { super(Component.literal("Tinctures")); }
        @Override public boolean isPauseScreen() { return false; }
        @Override public void render(GuiGraphics g,int mx,int my,float partial) {
            g.fill(0,0,width,height,0xff253440);var mc=Minecraft.getInstance();
            for(int i=0;i<6;i++) {
                int x=width/2-150+(i%3)*120,y=height/2-25+(i/3)*105;
                var be=(BottleBE)mc.level.getBlockEntity(pos(i));
                GuiGameElement.of(be).scale(65).rotate(-15,25,0).at(x-18,y,200).render(g);
                g.drawCenteredString(font,Component.literal(new String[]{"Dandelion: 1","Dandelion: 2","Dandelion: 3","Berry: 1","Berry: 2","Mixed: 1 + 1"}[i]),x+12,y+20,0xffeeeeee);
            }
        }
    }
}
