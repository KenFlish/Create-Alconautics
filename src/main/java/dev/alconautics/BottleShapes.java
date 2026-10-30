package dev.alconautics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;
/** Generated from the two supplied Blockbench models by tools/resources.py. */
public final class BottleShapes {
    public static final float[][][] CENTERS = {{{0.5f,0.5f}},{{0.375f,0.3125f},{0.6875f,0.75f}}};
    private static final VoxelShape[] SHAPES = {Shapes.or(Block.box(5.0,0.0,5.0,11.0,10.0,11.0),Block.box(5.5,0.0,5.5,10.5,9.0,10.5),Block.box(5.0,0.0,5.0,11.0,10.0,11.0),Block.box(7.0,10.0,7.0,9.0,13.0,9.0)).optimize(),
Shapes.or(Block.box(5.0,0.0,5.0,11.0,10.0,11.0),Block.box(5.5,0.0,5.5,10.5,9.0,10.5),Block.box(5.0,0.0,5.0,11.0,10.0,11.0),Block.box(7.0,10.0,7.0,9.0,13.0,9.0)).optimize(),
Shapes.or(Block.box(5.0,0.0,5.0,11.0,10.0,11.0),Block.box(5.5,0.0,5.5,10.5,9.0,10.5),Block.box(5.0,0.0,5.0,11.0,10.0,11.0),Block.box(7.0,10.0,7.0,9.0,13.0,9.0)).optimize(),
Shapes.or(Block.box(5.0,0.0,5.0,11.0,10.0,11.0),Block.box(5.5,0.0,5.5,10.5,9.0,10.5),Block.box(5.0,0.0,5.0,11.0,10.0,11.0),Block.box(7.0,10.0,7.0,9.0,13.0,9.0)).optimize(),
Shapes.or(Block.box(3.0,0.0,2.0,9.0,10.0,8.0),Block.box(3.5,0.0,2.5,8.5,9.0,7.5),Block.box(3.0,0.0,2.0,9.0,10.0,8.0),Block.box(5.0,10.0,4.0,7.0,13.0,6.0),Block.box(7.4217931,0.0,8.4217931,14.5782069,10.0,15.5782069),Block.box(8.0181609,0.0,9.0181609,13.9818391,9.0,14.9818391),Block.box(7.4217931,0.0,8.4217931,14.5782069,10.0,15.5782069),Block.box(9.8072644,10.0,10.8072644,12.1927356,13.0,13.1927356)).optimize(),
Shapes.or(Block.box(8.0,0.0,3.0,14.0,10.0,9.0),Block.box(8.5,0.0,3.5,13.5,9.0,8.5),Block.box(8.0,0.0,3.0,14.0,10.0,9.0),Block.box(10.0,10.0,5.0,12.0,13.0,7.0),Block.box(0.4217931,0.0,7.4217931,7.5782069,10.0,14.5782069),Block.box(1.0181609,0.0,8.0181609,6.9818391,9.0,13.9818391),Block.box(0.4217931,0.0,7.4217931,7.5782069,10.0,14.5782069),Block.box(2.8072644,10.0,9.8072644,5.1927356,13.0,12.1927356)).optimize(),
Shapes.or(Block.box(7.0,0.0,8.0,13.0,10.0,14.0),Block.box(7.5,0.0,8.5,12.5,9.0,13.5),Block.box(7.0,0.0,8.0,13.0,10.0,14.0),Block.box(9.0,10.0,10.0,11.0,13.0,12.0),Block.box(1.4217931,0.0,0.4217931,8.5782069,10.0,7.5782069),Block.box(2.0181609,0.0,1.0181609,7.9818391,9.0,6.9818391),Block.box(1.4217931,0.0,0.4217931,8.5782069,10.0,7.5782069),Block.box(3.8072644,10.0,2.8072644,6.1927356,13.0,5.1927356)).optimize(),
Shapes.or(Block.box(2.0,0.0,7.0,8.0,10.0,13.0),Block.box(2.5,0.0,7.5,7.5,9.0,12.5),Block.box(2.0,0.0,7.0,8.0,10.0,13.0),Block.box(4.0,10.0,9.0,6.0,13.0,11.0),Block.box(8.4217931,0.0,1.4217931,15.5782069,10.0,8.5782069),Block.box(9.0181609,0.0,2.0181609,14.9818391,9.0,7.9818391),Block.box(8.4217931,0.0,1.4217931,15.5782069,10.0,8.5782069),Block.box(10.8072644,10.0,3.8072644,13.1927356,13.0,6.1927356)).optimize()};
    static VoxelShape get(int count,Direction facing) {
        int turn=switch(facing) {case EAST -> 1;case SOUTH -> 2;case WEST -> 3;default -> 0;};
        return SHAPES[(count-1)*4+turn];
    }
}
