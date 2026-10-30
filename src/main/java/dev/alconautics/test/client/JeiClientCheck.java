package dev.alconautics.test.client;

import dev.alconautics.*;
import dev.alconautics.compat.jei.AlconauticsJei;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import java.util.List;

/** Exercise the real JEI runtime and category drawing, not a mock category UI. */
final class JeiClientCheck {
    private static boolean requested;private static int ticks;
    static void tick() {
        var mc=Minecraft.getInstance();
        if(!requested && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
            requested=true;String name="alconautics-jei-check";var flows=mc.createWorldOpenFlows();
            if(new java.io.File(mc.gameDirectory,"saves/"+name+"/level.dat").exists()) flows.openWorld(name,() -> {});
            else flows.createFreshLevel(name,new net.minecraft.world.level.LevelSettings(name,net.minecraft.world.level.GameType.CREATIVE,false,net.minecraft.world.Difficulty.PEACEFUL,true,new net.minecraft.world.level.GameRules(),net.minecraft.world.level.WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(42,false,false),
                registries -> registries.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).createWorldDimensions(),new TitleScreen());
            return;
        }
        var runtime=AlconauticsJei.runtime;
        if(mc.level==null || mc.player==null || runtime==null || mc.getOverlay()!=null) return;
        ticks++;
        if(ticks==1) {
            mc.getWindow().setWindowed(1920,1080);mc.setScreen(null);
            var manager=runtime.getRecipeManager();
            require(manager.createRecipeCategoryLookup().get().filter(category -> category.getRecipeType().getUid().getNamespace().equals(Alconautics.ID)).count()==1,"Only the combined distillation and cooling category should remain");
            require(manager.createRecipeLookup(AlconauticsJei.DISTILLING).includeHidden().get().count()==10,"Expected one combined recipe per drink variant");
            require(mc.getMobEffectTextures().get(Content.COURAGE).contents().name().toString().equals("alconautics:courage"),"Courage icon missing");
            runtime.getRecipesGui().showTypes(List.of(AlconauticsJei.DISTILLING));
        }
        if(ticks==40) {
            screenshot("distilling");
            runtime.getRecipesGui().showRecipes(runtime.getRecipeManager().getRecipeCategory(AlconauticsJei.DISTILLING),List.of(new AlconauticsJei.Distilling(new Drink(Drink.Kind.GRAIN,Drink.Variant.STRONG))),List.of());
        }
        if(ticks==80) { screenshot("herbs");mc.setScreen(new Icons()); }
        if(ticks==120) screenshot("icons");
        if(ticks==140) { System.out.println("ALCONAUTICS_COMPAT_CHECK: JEI 10 combined mash-to-moonshine recipes, native category screens and supplied effect icons passed");mc.stop(); }
    }
    private static void require(boolean condition,String message) { if(!condition) throw new IllegalStateException(message); }
    private static void screenshot(String suffix) {
        var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"alconautics-0.1.8-"+suffix+".png",mc.getMainRenderTarget(),message -> System.out.println("ALCONAUTICS_COMPAT_CHECK: "+message.getString()));
    }
    private static final class Icons extends Screen {
        Icons() { super(Component.literal("Supplied effect icons")); }
        @Override public void render(GuiGraphics g,int mx,int my,float partial) {
            g.fill(0,0,width,height,0xff253440);
            int x=width/2-80,y=height/2-25;
            for(var id:List.of("courage","shrinking")) {
                g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(4,4,1);
                g.blit(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"textures/mob_effect/"+id+".png"),0,0,0,0,16,16,16,16);g.pose().popPose();
                g.drawCenteredString(font,Component.translatable("effect.alconautics."+id),x+32,y+76,0xffeeeeee);x+=100;
            }
        }
    }
}
