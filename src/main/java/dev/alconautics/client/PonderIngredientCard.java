package dev.alconautics.client;

import java.util.List;
import java.util.function.Supplier;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.AnimatedOverlayElementBase;
import net.createmod.ponder.foundation.instruction.FadeInOutInstruction;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** A single screen-space caption with an evenly spaced row of ingredient examples. */
public final class PonderIngredientCard extends AnimatedOverlayElementBase {
    private final Supplier<String> caption;
    private final List<ItemStack> icons;
    private PonderIngredientCard(Supplier<String> caption,List<ItemStack> icons) {
        this.caption=caption;this.icons=icons.stream().map(ItemStack::copy).toList();
    }
    public List<ItemStack> icons() { return icons; }
    public static void show(SceneBuilder builder,int duration,Supplier<String> caption,List<ItemStack> icons) {
        var card=new PonderIngredientCard(caption,icons);
        builder.addInstruction(new FadeInOutInstruction(duration) {
            @Override protected void show(PonderScene scene) { card.setVisible(true);scene.addElement(card); }
            @Override protected void hide(PonderScene scene) { card.setVisible(false); }
            @Override protected void applyFade(PonderScene scene,float fade) { card.setFade(fade); }
        });
    }
    @Override public void render(PonderScene scene,PonderUI ui,GuiGraphics graphics,float partial,float fade) {
        if(fade<.05f) return;
        var font=ui.getFontRenderer();
        int width=Math.min(240,ui.width-24),x=ui.width-width-16,y=24+(int)((1-fade)*5);
        var lines=font.split(Component.literal(caption.get()),width-16);
        int lineHeight=font.lineHeight+1,height=16+lines.size()*lineHeight+24;
        int alpha=(int)(255*fade);
        graphics.pose().pushPose();graphics.pose().translate(0,0,400);
        graphics.fill(x-2,y-2,x+width+2,y+height+2,alpha<<24|0x181810);
        graphics.fill(x-1,y-1,x+width+1,y+height+1,alpha<<24|0x9a946b);
        graphics.fill(x,y,x+width,y+height,alpha<<24);
        for(int i=0;i<lines.size();i++) graphics.drawString(font,lines.get(i),x+8,y+8+i*lineHeight,alpha<<24|0xffffdd,false);
        int rowWidth=icons.size()*16+(icons.size()-1)*12,start=x+(width-rowWidth)/2;
        int iconY=y+12+lines.size()*lineHeight;
        for(int i=0;i<icons.size();i++) graphics.renderItem(icons.get(i),start+i*28,iconY);
        graphics.pose().popPose();
    }
}
