package dev.alconautics;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;

/** Generated from art/source by tools/model_shapes.py; includes cube pivots. */
final class ModelShapes {
    static final VoxelShape DRY = Shapes.or(Block.box(3,0,3,13,14,13),Block.box(5,13,5,11,16,11)).optimize();
    private static final VoxelShape[] HEAD = {Shapes.or(Block.box(1,0,1,15,3,15),Block.box(3,2,3,13,14,13),Block.box(4,14,4,12,17,12),Block.box(5,5,13,11,11,15),Block.box(6.0,6.0,14.5,7.0,7.0,15.5)).optimize(),
        Shapes.or(Block.box(1,0,1,15,3,15),Block.box(3,2,3,13,14,13),Block.box(4,14,4,12,17,12),Block.box(1,5,5,3,11,11),Block.box(0.5,6.0,6.0,1.5,7.0,7.0)).optimize(),
        Shapes.or(Block.box(1,0,1,15,3,15),Block.box(3,2,3,13,14,13),Block.box(4,14,4,12,17,12),Block.box(5,5,1,11,11,3),Block.box(9.0,6.0,0.5,10.0,7.0,1.5)).optimize(),
        Shapes.or(Block.box(1,0,1,15,3,15),Block.box(3,2,3,13,14,13),Block.box(4,14,4,12,17,12),Block.box(13,5,5,15,11,11),Block.box(14.5,6.0,9.0,15.5,7.0,10.0)).optimize()};
    static VoxelShape head(Direction facing) {
        return HEAD[switch(facing) { case SOUTH -> 1; case WEST -> 2; case NORTH -> 3; default -> 0; }];
    }
}
