package dev.alconautics.test;

import dev.alconautics.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("alconautics")
@PrefixGameTestTemplate(false)
public final class CompatTests {
    @GameTest(template="empty",timeoutTicks=20)
    public static void grain_ingredients(GameTestHelper h) {
        var recipe=h.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"mixing/grain_mash")).orElseThrow().value();
        for(String id:new String[]{"minecraft:wheat","farm_and_charm:oat","farm_and_charm:barley"}) {
            var item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            h.assertTrue(item!=Items.AIR,"Missing grain item: "+id);
            long slots=recipe.getIngredients().stream().filter(ingredient -> ingredient.test(new ItemStack(item))).count();
            h.assertTrue(slots==4,"All four grain slots must accept "+id);
        }
        h.assertTrue(recipe.getIngredients().stream().noneMatch(ingredient -> ingredient.test(new ItemStack(Items.WHEAT_SEEDS))),"Seeds are not harvested grain");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void moonshine_fuel(GameTestHelper h) {
        var ethanol=TagKey.create(Registries.FLUID,ResourceLocation.fromNamespaceAndPath("c","ethanol"));
        for(var fluid:Content.SPIRITS.values()) {
            h.assertTrue(fluid.source.get().is(ethanol) && fluid.flowing.get().is(ethanol),"All finished drink variants must be fuel");
        }
        for(var fluid:Content.MASH.values()) h.assertTrue(!fluid.source.get().is(ethanol),"Mash must not be usable as fuel");
        for(var fluid:Content.VAPOUR.values()) h.assertTrue(!fluid.source.get().is(ethanol),"Vapour must not be usable as liquid fuel");
        if(net.neoforged.fml.ModList.get().isLoaded("createdieselgenerators")) DieselCheck.run(h);else h.succeed();
    }
}
