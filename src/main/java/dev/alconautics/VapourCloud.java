package dev.alconautics;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class VapourCloud {
    public static final String NAME="alconautics:rupture_cloud";
    public static boolean isOurs(AreaEffectCloud cloud) { return cloud.getCustomName()!=null && cloud.getCustomName().getString().equals(NAME); }
    public static void spawn(ServerLevel level,Vec3 center,float radius,int duration) {
        var cloud=new AreaEffectCloud(level,center.x,center.y,center.z);
        cloud.setRadius(radius);cloud.setDuration(duration);cloud.setRadiusPerTick(-radius/(duration*1.5f));
        cloud.setCustomName(Component.literal(NAME));cloud.setCustomNameVisible(false);
        BreweryCompat.drunkEffect().ifPresent(effect -> cloud.addEffect(new MobEffectInstance(effect,600,0)));
        level.addFreshEntity(cloud);
    }
    public static void burstBottle(ServerLevel level,Vec3 center) {
        level.explode(null,center.x,center.y,center.z,.45f,net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        level.playSound(null,center.x,center.y,center.z,net.minecraft.sounds.SoundEvents.GLASS_BREAK,net.minecraft.sounds.SoundSource.BLOCKS,.7f,1.25f);
        spawn(level,center,1.4f,100);
    }
}
