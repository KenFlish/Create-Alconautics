package dev.alconautics.client;

import dev.alconautics.Alconautics;
import dev.alconautics.VapourCloud;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=Alconautics.ID,value=Dist.CLIENT)
public final class VapourCloudParticles {
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var level=Minecraft.getInstance().level;
        if(level==null || level.getGameTime()%3!=0) return;
        for(var entity:level.entitiesForRendering()) {
            if(!(entity instanceof AreaEffectCloud cloud) || !VapourCloud.isOurs(cloud)) continue;
            for(int i=0;i<5;i++) {
                double x=level.random.nextDouble()*2-1,y=level.random.nextDouble()*2-1,z=level.random.nextDouble()*2-1;
                if(x*x+y*y+z*z>1) continue;
                double r=cloud.getRadius();
                level.addParticle(ParticleTypes.CLOUD,cloud.getX()+x*r,cloud.getY()+y*r,cloud.getZ()+z*r,x*.015,.02,z*.015);
            }
        }
    }
}
