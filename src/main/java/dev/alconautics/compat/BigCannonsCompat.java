package dev.alconautics.compat;

import dev.alconautics.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.FluidBlobEffectRegistry;

/** CBC's native fluid callbacks, registered only when the optional mod is present. */
public final class BigCannonsCompat {
    public static void register() {
        for(var fluid:Content.ALL_FLUIDS) if(Content.finishedDrink(fluid.source.get())) {
            for(var variant:java.util.List.of(fluid.source.get(),fluid.flowing.get())) {
                FluidBlobEffectRegistry.registerHitEntity(variant,ctx -> {
                    if(!ctx.level().isClientSide && ctx.result().getEntity() instanceof LivingEntity target) intoxicate(target);
                });
                FluidBlobEffectRegistry.registerFluidShellExplosionEffect(variant,ctx -> {
                    if(ctx.level() instanceof ServerLevel level) {
                        var center=new Vec3(ctx.x(),ctx.y(),ctx.z());
                        float radius=Math.max(1,Math.min(6,ctx.radius()));
                        for(var target:level.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(center,center).inflate(radius)))
                            if(target.distanceToSqr(center)<=radius*radius) intoxicate(target);
                        VapourCloud.spawn(level,center,radius,100);
                    }
                });
            }
        }
    }
    private static void intoxicate(LivingEntity target) {
        long now=target.level().getGameTime();var data=target.getPersistentData();
        if(data.contains("AlconauticsCannonAlcoholUntil") && now<data.getLong("AlconauticsCannonAlcoholUntil")) return;
        data.putLong("AlconauticsCannonAlcoholUntil",now+20);BreweryCompat.drink(target);
    }
}
