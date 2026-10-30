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
    public record Distilling(Drink drink) {}
    public static final RecipeType<Distilling> DISTILLING=RecipeType.create(Alconautics.ID,"distilling",Distilling.class);
    @Override public ResourceLocation getPluginUid() { return ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"jei"); }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui=registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new DistillingCategory(gui));
    }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        var distilling=new ArrayList<Distilling>();
        for(var drink:Drink.ALL) {
            distilling.add(new Distilling(drink));
        }
        registration.addRecipes(DISTILLING,distilling);
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(DISTILLING,Content.HEAD_ITEM.get(),AllBlocks.FLUID_TANK.get(),AllBlocks.BLAZE_BURNER.get(),Content.COOLER_ITEM.get());
    }
}
