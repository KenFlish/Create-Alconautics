package dev.alconautics.mixin;
import dev.alconautics.TinctureAge;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
@Mixin(ProcessingRecipe.class)
public abstract class TinctureRecipeMixin {
    @Inject(method={"rollResults(Lnet/minecraft/util/RandomSource;)Ljava/util/List;","rollResults(Ljava/util/List;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"},at=@At("RETURN"))
    private void alconautics$birth(CallbackInfoReturnable<List<ItemStack>> cir) {
        var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if(server!=null && server.isSameThread()) for(var stack:cir.getReturnValue()) TinctureAge.update(stack,server.overworld());
    }
}
