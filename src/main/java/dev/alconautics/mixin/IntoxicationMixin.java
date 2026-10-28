package dev.alconautics.mixin;

import dev.alconautics.BreweryCompat;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.satisfy.brewery.core.effect.IntoxicationEffect.class)
public abstract class IntoxicationMixin {
    @Redirect(method="applyEffectTick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean alconautics$intermittentNausea(LivingEntity entity,MobEffectInstance effect) { return BreweryCompat.applyBreweryEffect(entity,effect); }
}
