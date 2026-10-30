package dev.alconautics;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Strength, haste and a finite pool of absorption in a single effect. */
@EventBusSubscriber(modid=Alconautics.ID)
public final class CourageEffect extends MobEffect {
    public CourageEffect() {
        super(MobEffectCategory.BENEFICIAL,0xe3b65d);
        addAttributeModifier(Attributes.ATTACK_DAMAGE,ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"courage_strength"),3,AttributeModifier.Operation.ADD_VALUE);
        addAttributeModifier(Attributes.MAX_ABSORPTION,ResourceLocation.fromNamespaceAndPath(Alconautics.ID,"courage_absorption"),4,AttributeModifier.Operation.ADD_VALUE);
    }
    @Override public void onEffectStarted(LivingEntity entity,int amplifier) {
        super.onEffectStarted(entity,amplifier);
        entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(),4*(amplifier+1)));
    }
    @SubscribeEvent public static void mining(PlayerEvent.BreakSpeed event) {
        var effect=event.getEntity().getEffect(Content.COURAGE);
        if(effect!=null) event.setNewSpeed(event.getNewSpeed()*(1+.2f*(effect.getAmplifier()+1)));
    }
}
