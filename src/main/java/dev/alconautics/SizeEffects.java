package dev.alconautics;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid=Alconautics.ID)
public final class SizeEffects {
    private static final ResourceLocation SCALE=id("size"), REACH=id("reach"), SPEED=id("speed"), HEALTH=id("health");
    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(Alconautics.ID,name); }
    @SubscribeEvent public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity e)) return;
        if(e.level().isClientSide) return;
        BreweryCompat.tick(e);
        var grow=e.getEffect(Content.GROW); var shrink=e.getEffect(Content.SHRINK);
        double target=grow!=null?(grow.getAmplifier()>0?2:1.5):shrink!=null?(shrink.getAmplifier()>0?.5:2d/3):1;
        var scale=e.getAttribute(Attributes.SCALE); if(scale==null) return;
        var old=scale.getModifier(SCALE);
        double current=old==null?1:1+old.amount();
        // Fixed maximum step gives a visible transition in both directions, also on expiry.
        target=current+Math.clamp(target-current,-1d/60,1d/60);
        if(target>current) {
            AABB b=e.getBoundingBox();
            double ratio=target/current;
            double halfX=b.getXsize()*ratio/2,halfZ=b.getZsize()*ratio/2;
            if(!e.level().noCollision(e,new AABB(e.getX()-halfX,b.minY,e.getZ()-halfZ,e.getX()+halfX,b.minY+b.getYsize()*ratio,e.getZ()+halfZ))) target=current;
        }
        set(e,Attributes.SCALE,SCALE,target-1);
        double growth=Math.max(0,target-1);
        set(e,Attributes.BLOCK_INTERACTION_RANGE,REACH,growth);
        set(e,Attributes.ENTITY_INTERACTION_RANGE,REACH,growth);
        set(e,Attributes.MOVEMENT_SPEED,SPEED,growth*.15);
        set(e,Attributes.MAX_HEALTH,HEALTH,growth*.25);
        if(e.getHealth()>e.getMaxHealth()) e.setHealth(e.getMaxHealth());
    }
    private static void set(LivingEntity e,net.minecraft.core.Holder<Attribute> attribute,ResourceLocation id,double amount) {
        var a=e.getAttribute(attribute); if(a==null) return;
        var old=a.getModifier(id);
        if(old!=null && Math.abs(old.amount()-amount)<.00001) return;
        if(old!=null) a.removeModifier(id);
        if(Math.abs(amount)>.00001) a.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
