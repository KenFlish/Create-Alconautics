package dev.alconautics.compat.jei;

import dev.alconautics.*;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.*;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class DistillingCategory implements IRecipeCategory<AlconauticsJei.Distilling> {
    private final IDrawable icon;private final IDrawableAnimated arrow;
    public DistillingCategory(IGuiHelper gui) { icon=gui.createDrawableItemLike(Content.HEAD_ITEM.get());arrow=gui.createAnimatedRecipeArrow(60); }
    public RecipeType<AlconauticsJei.Distilling> getRecipeType() { return AlconauticsJei.DISTILLING; }
    public Component getTitle() { return Component.translatable("jei.alconautics.distilling"); }
    public IDrawable getIcon() { return icon; }
    public int getWidth() { return 180; }public int getHeight() { return 120; }
    @Override public net.minecraft.resources.ResourceLocation getRegistryName(AlconauticsJei.Distilling recipe) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"distilling/"+recipe.drink().id()+(recipe.superheat()?"/superheated":"/heated"));
    }
    public void setRecipe(IRecipeLayoutBuilder builder,AlconauticsJei.Distilling recipe,IFocusGroup focus) {
        var drink=recipe.drink();
        builder.addInputSlot(8,40).addFluidStack(Content.MASH.get(drink.kind()).source.get(),1000).setFluidRenderer(1000,false,16,16).setStandardSlotBackground();
        builder.addOutputSlot(156,40).addFluidStack(Content.VAPOUR.get(drink).source.get(),1000).setFluidRenderer(1000,false,16,16).setStandardSlotBackground();
        if(drink.variant()!=Drink.Variant.NORMAL)
            builder.addInputSlot(8,87).addItemStack(new ItemStack(drink.variant()==Drink.Variant.LONG?Herbs.ribwort():Herbs.nettle())).setStandardSlotBackground();
    }
    public void draw(AlconauticsJei.Distilling recipe,IRecipeSlotsView slots,GuiGraphics g,double mouseX,double mouseY) {
        arrow.draw(g,124,40);ApparatusDisplay.still(g,recipe.superheat());
    }
}
