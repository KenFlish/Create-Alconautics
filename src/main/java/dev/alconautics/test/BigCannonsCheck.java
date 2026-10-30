package dev.alconautics.test;
import dev.alconautics.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import rbasamoyai.createbigcannons.index.*;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.*;
import rbasamoyai.ritchiesprojectilelib.projectile_burst.ProjectileBurst.SubProjectile;
final class BigCannonsCheck {
    static void run(GameTestHelper h) {
        var pos=new BlockPos(2,2,2);h.setBlock(pos,CBCBlocks.FLUID_SHELL.get());
        var handler=h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,h.absolutePos(pos),Direction.UP);
        h.assertTrue(handler!=null,"CBC fluid shell tank missing");
        var target=h.spawn(EntityType.ZOMBIE,new BlockPos(1,2,1));var effect=BreweryCompat.drunkEffect().orElseThrow();
        var burst=CBCEntityTypes.FLUID_BLOB_BURST.get().create(h.getLevel());
        for(var fluid:Content.ALL_FLUIDS) if(Content.finishedDrink(fluid.source.get())) {
            handler.drain(Integer.MAX_VALUE,FluidAction.EXECUTE);
            h.assertTrue(handler.fill(fluid.stack(250),FluidAction.EXECUTE)==250,"Fluid shell rejected finished drink "+fluid.id);
            target.removeEffect(effect);target.getPersistentData().remove("AlconauticsCannonAlcoholUntil");
            burst.setFluidStack(new EndFluidStack(fluid.source.get(),250,net.minecraft.core.component.DataComponentPatch.EMPTY));
            FluidBlobEffectRegistry.effectOnHitEntity(burst,new SubProjectile(new double[3],new double[3]),new EntityHitResult(target));
            h.assertTrue(target.hasEffect(effect),"Native CBC hit callback failed for "+fluid.id);
        }
        target.removeEffect(effect);target.getPersistentData().remove("AlconauticsCannonAlcoholUntil");
        var center=target.position();
        boolean handled=FluidBlobEffectRegistry.effectOnFluidShellExplode(Content.TINCTURES.get(Tincture.BERRY).source.get(),h.getLevel(),center.x,center.y,center.z,2);
        h.assertTrue(handled && target.hasEffect(effect),"Shell burst must intoxicate nearby entities");
        h.assertTrue(h.getLevel().getEntitiesOfClass(AreaEffectCloud.class,target.getBoundingBox().inflate(3)).stream().anyMatch(VapourCloud::isOurs),"Burst must create the spherical Alconautics vapour cloud");
        burst.discard();h.succeed();
    }
}
