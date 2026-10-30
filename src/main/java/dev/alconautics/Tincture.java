package dev.alconautics;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.*;
public enum Tincture {
    DANDELION, BERRY;
    public String id() { return name().toLowerCase(java.util.Locale.ROOT)+"_tincture"; }
    public Holder<MobEffect> effect() { return this==DANDELION?MobEffects.REGENERATION:MobEffects.MOVEMENT_SPEED; }
    public int baseDuration() { return this==DANDELION?400:1200; }
}
