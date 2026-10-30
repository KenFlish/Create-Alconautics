package dev.alconautics.test;

import dev.alconautics.*;
import com.jesz.createdieselgenerators.*;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.content.tools.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Loaded only by the optional-mod test path. */
final class DieselCheck {
    static void run(GameTestHelper h) {
        var fuels=h.getLevel().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE);
        for(var entry:Content.SPIRITS.values()) {
            var fuel=FuelType.getTypeFor(fuels,entry.source.get());
            h.assertTrue(fuel.normal().speed()>0 && fuel.isFlammable(),"Moonshine not recognized by Diesel Generators as combustible fuel");
            var sprayer=new ItemStack(CDGItems.CHEMICAL_SPRAYER_LIGHTER.get());
            var handler=sprayer.getCapability(Capabilities.FluidHandler.ITEM);
            h.assertTrue(handler!=null && handler.fill(entry.stack(100),FluidAction.EXECUTE)==100,"Sprayer could not be filled through its native fluid capability");
        }
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        var item=CDGItems.CHEMICAL_SPRAYER_LIGHTER.get();var stack=new ItemStack(item);
        stack.getCapability(Capabilities.FluidHandler.ITEM).fill(Content.SPIRITS.get(Drink.ALL.getFirst()).stack(100),FluidAction.EXECUTE);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        item.onUseTick(h.getLevel(),player,stack,50);
        var projectiles=h.getLevel().getEntitiesOfClass(ChemicalSprayerProjectileEntity.class,player.getBoundingBox().inflate(4));
        h.assertTrue(projectiles.stream().anyMatch(projectile -> projectile.fire),"Moonshine did not produce burning spray with the lighter");
        h.assertTrue(item.readFluid(stack).getAmount()<100,"Spraying must consume fuel");
        projectiles.forEach(net.minecraft.world.entity.Entity::discard);h.succeed();
    }
}
