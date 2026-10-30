package dev.alconautics;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;

/** Same effect/level/duration formatting as vanilla potions and Create's liquid potions. */
public final class DrinkTooltip {
    private DrinkTooltip() {}
    public static void append(Drink drink,Consumer<Component> out,float tickRate) {
        if(drink.kind()==Drink.Kind.VEGETABLE) return;
        PotionContents.addPotionTooltip(List.of(new MobEffectInstance(drink.kind()==Drink.Kind.SUGAR?Content.GROW:drink.kind()==Drink.Kind.GRAIN?Content.COURAGE:Content.SHRINK,
            drink.variant().ticks(),drink.variant().amplifier())),out,1,tickRate);
    }
}
