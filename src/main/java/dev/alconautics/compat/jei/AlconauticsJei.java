package dev.alconautics.compat.jei;

import dev.alconautics.*;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.*;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

/** JEI discovers this plugin only when installed; common code never loads it. */
@JeiPlugin
public final class AlconauticsJei implements IModPlugin {
    public static mezz.jei.api.runtime.IJeiRuntime runtime;
    @Override public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime value) { runtime=value; }
    @Override public void onRuntimeUnavailable() { runtime=null; }
    public record Distilling(Drink drink,boolean superheat) {}
    public record Condensing(Drink drink) {}
    public static final RecipeType<Distilling> DISTILLING=RecipeType.create(Alconautics.ID,"distilling",Distilling.class);
    public static final RecipeType<Condensing> CONDENSING=RecipeType.create(Alconautics.ID,"condensing",Condensing.class);
    @Override public ResourceLocation getPluginUid() { return ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"jei"); }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui=registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new DistillingCategory(gui),new CondensingCategory(gui));
    }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        var distilling=new ArrayList<Distilling>();var condensing=new ArrayList<Condensing>();
        for(var drink:Drink.ALL) {
            distilling.add(new Distilling(drink,false));distilling.add(new Distilling(drink,true));condensing.add(new Condensing(drink));
        }
        registration.addRecipes(DISTILLING,distilling);registration.addRecipes(CONDENSING,condensing);
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(DISTILLING,Content.HEAD_ITEM.get(),AllBlocks.FLUID_TANK.get(),AllBlocks.BLAZE_BURNER.get());
        registration.addRecipeCatalysts(CONDENSING,Content.COOLER_ITEM.get(),AllBlocks.FLUID_PIPE.get());
    }
}
