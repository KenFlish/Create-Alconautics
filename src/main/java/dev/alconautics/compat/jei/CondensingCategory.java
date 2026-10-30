package dev.alconautics.compat.jei;

import dev.alconautics.*;
import java.util.List;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.*;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class CondensingCategory implements IRecipeCategory<AlconauticsJei.Condensing> {
    private final IDrawable icon;private final IDrawableAnimated arrow;
    public CondensingCategory(IGuiHelper gui) { icon=gui.createDrawableItemLike(Content.COOLER_ITEM.get());arrow=gui.createAnimatedRecipeArrow(40); }
    public RecipeType<AlconauticsJei.Condensing> getRecipeType() { return AlconauticsJei.CONDENSING; }
    public Component getTitle() { return Component.translatable("jei.alconautics.condensing"); }
    public IDrawable getIcon() { return icon; }
    public int getWidth() { return 180; }public int getHeight() { return 120; }
    @Override public net.minecraft.resources.ResourceLocation getRegistryName(AlconauticsJei.Condensing recipe) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"condensing/"+recipe.drink().id());
    }
    public void setRecipe(IRecipeLayoutBuilder builder,AlconauticsJei.Condensing recipe,IFocusGroup focus) {
        builder.addInputSlot(8,40).addFluidStack(Content.VAPOUR.get(recipe.drink()).source.get(),1000).setFluidRenderer(1000,false,16,16).setStandardSlotBackground();
        builder.addOutputSlot(156,40).addFluidStack(Content.SPIRITS.get(recipe.drink()).source.get(),1000).setFluidRenderer(1000,false,16,16).setStandardSlotBackground();
        builder.addInputSlot(8,87).addItemStacks(List.of(new ItemStack(Items.SNOWBALL,4),new ItemStack(Items.SNOW_BLOCK),new ItemStack(Items.ICE),new ItemStack(Items.PACKED_ICE),new ItemStack(Items.BLUE_ICE)))
            .setStandardSlotBackground();
    }
    public void draw(AlconauticsJei.Condensing recipe,IRecipeSlotsView slots,GuiGraphics g,double mouseX,double mouseY) {
        arrow.draw(g,124,40);ApparatusDisplay.condenser(g);
    }
}
