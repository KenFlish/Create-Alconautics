package dev.alconautics;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;

public final class BreweryCompat {
    public static Optional<Holder.Reference<MobEffect>> drunkEffect() { return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("brewery","drunk")); }
    public static void drink(LivingEntity entity) {
        if (!net.satisfy.brewery.platform.PlatformHelper.isDrunkennessEnabled()
            || net.satisfy.brewery.core.registry.ArmorSetRegistry.hasHarddrinking(entity)) return;
        drunkEffect().ifPresent(effect -> {
            var old=entity.getEffect(effect);
            int amplifier=old==null?0:Math.min(5,old.getAmplifier()+1);
            // Replace the old instance: vanilla otherwise keeps hidden weaker levels for minutes.
            entity.removeEffect(effect);
            entity.addEffect(new MobEffectInstance(effect,1200,amplifier,false,false));
            entity.getPersistentData().putBoolean("AlconauticsAlcohol",true);
        });
    }
    public static void tick(LivingEntity entity) {
        if(!entity.getPersistentData().getBoolean("AlconauticsAlcohol")) return;
        drunkEffect().ifPresent(effect -> {
            var instance=entity.getEffect(effect);
            if(instance==null) { entity.getPersistentData().remove("AlconauticsAlcohol"); return; }
            if(instance.getDuration()<=1 && instance.getAmplifier()>0) {
                int next=instance.getAmplifier()-1;
                entity.removeEffect(effect);
                entity.addEffect(new MobEffectInstance(effect,1200,next,false,false));
            }
        });
    }
    /** Preserve the natural timer and never lower intoxication below level I. */
    public static void soberOneLevel(LivingEntity entity) {
        drunkEffect().ifPresent(effect -> {
            var old=entity.getEffect(effect);
            if(old==null || old.getAmplifier()==0) return;
            int duration=old.getDuration(),amplifier=old.getAmplifier()-1;
            entity.removeEffect(effect);
            entity.addEffect(new MobEffectInstance(effect,duration,amplifier,old.isAmbient(),old.isVisible(),old.showIcon()));
        });
    }
    public static boolean applyBreweryEffect(LivingEntity entity,MobEffectInstance instance) {
        if(!instance.is(MobEffects.CONFUSION)) return entity.addEffect(instance);
        long now=entity.level().getGameTime();
        var data=entity.getPersistentData();
        if(now<data.getLong("AlconauticsNextNausea")) return false;
        data.putLong("AlconauticsNextNausea",now+600);
        int level=drunkEffect().map(entity::getEffect).map(e -> e.getAmplifier()>=5?1:0).orElse(0);
        entity.removeEffect(MobEffects.CONFUSION);
        return entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,80,level,false,false));
    }
}
