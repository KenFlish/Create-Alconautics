package dev.alconautics.compat;

import dev.alconautics.BottleBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Keep optional interface verification behind the loaded-mod guard. */
public final class SableCompat {
    public static BottleBlock createBottle(BlockBehaviour.Properties properties) { return new SableBottleBlock(properties); }
}
