package dev.alconautics;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
/** Generated from supplied single, paired, triple and mixed Blockbench geometry. */
public final class TinctureGeometry {
    public static final String[] MODELS = {"dandelion_tincture_single","dandelion_tincture_pair_0","dandelion_tincture_pair_1","dandelion_tincture_triple_0","dandelion_tincture_triple_1","dandelion_tincture_triple_2","berry_tincture_single","berry_tincture_pair_0","berry_tincture_pair_1","tincture_mixed_0","tincture_mixed_1"};
    public static final String[] NAMES = {"dandelion_tincture_single","dandelion_tincture_pair","dandelion_tincture_triple","berry_tincture_single","berry_tincture_pair","tincture_mixed"};
    public static final float[][][] CENTERS = {{{0.53125f,0.46875f}},{{0.3895336f,0.6777889f},{0.59375f,0.34375f}},{{0.3270336f,0.3027889f},{0.71875f,0.46875f},{0.3314603f,0.7360978f}},{{0.5f,0.5f}},{{0.625f,0.3125f},{0.3125f,0.6875f}},{{0.65625f,0.28125f},{0.375f,0.6875f}}};
    private static final VoxelShape[] SHAPES = {Shapes.or(Block.box(6.0,0.0,5.0,11.0,10.0,10.0),Block.box(6.5,0.0,5.5,10.5,9.0,9.5),Block.box(7.5,10.0,6.5,9.5,13.0,8.5)).optimize(),
Shapes.or(Block.box(6.0,0.0,6.0,11.0,10.0,11.0),Block.box(6.5,0.0,6.5,10.5,9.0,10.5),Block.box(7.5,10.0,7.5,9.5,13.0,9.5)).optimize(),
Shapes.or(Block.box(5.0,0.0,6.0,10.0,10.0,11.0),Block.box(5.5,0.0,6.5,9.5,9.0,10.5),Block.box(6.5,10.0,7.5,8.5,13.0,9.5)).optimize(),
Shapes.or(Block.box(5.0,0.0,5.0,10.0,10.0,10.0),Block.box(5.5,0.0,5.5,9.5,9.0,9.5),Block.box(6.5,10.0,6.5,8.5,13.0,8.5)).optimize(),
Shapes.or(Block.box(2.9661304,0.0,7.5782158,9.4989452,10.0,14.1110306),Block.box(3.6194119,0.0,8.2314973,8.8456637,9.0,13.4577491),Block.box(5.193437,10.0,9.193437,7.806563,13.0,11.806563),Block.box(7.0,0.0,3.0,12.0,10.0,8.0),Block.box(7.5,0.0,3.5,11.5,9.0,7.5),Block.box(8.5,10.0,4.5,10.5,13.0,6.5)).optimize(),
Shapes.or(Block.box(1.8889694,0.0,2.9661304,8.4217842,10.0,9.4989452),Block.box(2.5422509,0.0,3.6194119,7.7685027,9.0,8.8456637),Block.box(4.193437,10.0,5.193437,6.806563,13.0,7.806563),Block.box(8.0,0.0,7.0,13.0,10.0,12.0),Block.box(8.5,0.0,7.5,12.5,9.0,11.5),Block.box(9.5,10.0,8.5,11.5,13.0,10.5)).optimize(),
Shapes.or(Block.box(6.5010548,0.0,1.8889694,13.0338696,10.0,8.4217842),Block.box(7.1543363,0.0,2.5422509,12.3805881,9.0,7.7685027),Block.box(8.193437,10.0,4.193437,10.806563,13.0,6.806563),Block.box(4.0,0.0,8.0,9.0,10.0,13.0),Block.box(4.5,0.0,8.5,8.5,9.0,12.5),Block.box(5.5,10.0,9.5,7.5,13.0,11.5)).optimize(),
Shapes.or(Block.box(7.5782158,0.0,6.5010548,14.1110306,10.0,13.0338696),Block.box(8.2314973,0.0,7.1543363,13.4577491,9.0,12.3805881),Block.box(9.193437,10.0,8.193437,11.806563,13.0,10.806563),Block.box(3.0,0.0,4.0,8.0,10.0,9.0),Block.box(3.5,0.0,4.5,7.5,9.0,8.5),Block.box(4.5,10.0,5.5,6.5,13.0,7.5)).optimize(),
Shapes.or(Block.box(1.9661304,0.0,1.5782158,8.4989452,10.0,8.1110306),Block.box(2.6194119,0.0,2.2314973,7.8456637,9.0,7.4577491),Block.box(4.193437,10.0,3.193437,6.806563,13.0,5.806563),Block.box(9.0,0.0,5.0,14.0,10.0,10.0),Block.box(9.5,0.0,5.5,13.5,9.0,9.5),Block.box(10.5,10.0,6.5,12.5,13.0,8.5),Block.box(2.1673072,0.0,8.6415074,8.4394209,10.0,14.9136212),Block.box(2.7945186,0.0,9.2687188,7.8122096,9.0,14.2864098),Block.box(4.2455772,10.0,10.2455772,6.7544228,13.0,12.7544228)).optimize(),
Shapes.or(Block.box(7.8889694,0.0,1.9661304,14.4217842,10.0,8.4989452),Block.box(8.5422509,0.0,2.6194119,13.7685027,9.0,7.8456637),Block.box(10.193437,10.0,4.193437,12.806563,13.0,6.806563),Block.box(6.0,0.0,9.0,11.0,10.0,14.0),Block.box(6.5,0.0,9.5,10.5,9.0,13.5),Block.box(7.5,10.0,10.5,9.5,13.0,12.5),Block.box(1.0863788,0.0,2.1673072,7.3584926,10.0,8.4394209),Block.box(1.7135902,0.0,2.7945186,6.7312812,9.0,7.8122096),Block.box(3.2455772,10.0,4.2455772,5.7544228,13.0,6.7544228)).optimize(),
Shapes.or(Block.box(7.5010548,0.0,7.8889694,14.0338696,10.0,14.4217842),Block.box(8.1543363,0.0,8.5422509,13.3805881,9.0,13.7685027),Block.box(9.193437,10.0,10.193437,11.806563,13.0,12.806563),Block.box(2.0,0.0,6.0,7.0,10.0,11.0),Block.box(2.5,0.0,6.5,6.5,9.0,10.5),Block.box(3.5,10.0,7.5,5.5,13.0,9.5),Block.box(7.5605791,0.0,1.0863788,13.8326928,10.0,7.3584926),Block.box(8.1877904,0.0,1.7135902,13.2054814,9.0,6.7312812),Block.box(9.2455772,10.0,3.2455772,11.7544228,13.0,5.7544228)).optimize(),
Shapes.or(Block.box(1.5782158,0.0,7.5010548,8.1110306,10.0,14.0338696),Block.box(2.2314973,0.0,8.1543363,7.4577491,9.0,13.3805881),Block.box(3.193437,10.0,9.193437,5.806563,13.0,11.806563),Block.box(5.0,0.0,2.0,10.0,10.0,7.0),Block.box(5.5,0.0,2.5,9.5,9.0,6.5),Block.box(6.5,10.0,3.5,8.5,13.0,5.5),Block.box(8.6415074,0.0,7.5605791,14.9136212,10.0,13.8326928),Block.box(9.2687188,0.0,8.1877904,14.2864098,9.0,13.2054814),Block.box(10.2455772,10.0,9.2455772,12.7544228,13.0,11.7544228)).optimize(),
Shapes.or(Block.box(5.0,0.0,5.0,11.0,7.0,11.0),Block.box(5.5,0.0,5.5,10.5,6.0,10.5),Block.box(7.0,7.0,7.0,9.0,10.0,9.0)).optimize(),
Shapes.or(Block.box(5.0,0.0,5.0,11.0,7.0,11.0),Block.box(5.5,0.0,5.5,10.5,6.0,10.5),Block.box(7.0,7.0,7.0,9.0,10.0,9.0)).optimize(),
Shapes.or(Block.box(5.0,0.0,5.0,11.0,7.0,11.0),Block.box(5.5,0.0,5.5,10.5,6.0,10.5),Block.box(7.0,7.0,7.0,9.0,10.0,9.0)).optimize(),
Shapes.or(Block.box(5.0,0.0,5.0,11.0,7.0,11.0),Block.box(5.5,0.0,5.5,10.5,6.0,10.5),Block.box(7.0,7.0,7.0,9.0,10.0,9.0)).optimize(),
Shapes.or(Block.box(7.0,0.0,2.0,13.0,7.0,8.0),Block.box(7.5,0.0,2.5,12.5,6.0,7.5),Block.box(9.0,7.0,4.0,11.0,10.0,6.0),Block.box(0.7936557,0.0,6.7936557,9.2063443,7.0,15.2063443),Block.box(1.4947131,0.0,7.4947131,8.5052869,6.0,14.5052869),Block.box(3.5978852,7.0,9.5978852,6.4021148,10.0,12.4021148)).optimize(),
Shapes.or(Block.box(8.0,0.0,7.0,14.0,7.0,13.0),Block.box(8.5,0.0,7.5,13.5,6.0,12.5),Block.box(10.0,7.0,9.0,12.0,10.0,11.0),Block.box(0.7936557,0.0,0.7936557,9.2063443,7.0,9.2063443),Block.box(1.4947131,0.0,1.4947131,8.5052869,6.0,8.5052869),Block.box(3.5978852,7.0,3.5978852,6.4021148,10.0,6.4021148)).optimize(),
Shapes.or(Block.box(3.0,0.0,8.0,9.0,7.0,14.0),Block.box(3.5,0.0,8.5,8.5,6.0,13.5),Block.box(5.0,7.0,10.0,7.0,10.0,12.0),Block.box(6.7936557,0.0,0.7936557,15.2063443,7.0,9.2063443),Block.box(7.4947131,0.0,1.4947131,14.5052869,6.0,8.5052869),Block.box(9.5978852,7.0,3.5978852,12.4021148,10.0,6.4021148)).optimize(),
Shapes.or(Block.box(2.0,0.0,3.0,8.0,7.0,9.0),Block.box(2.5,0.0,3.5,7.5,6.0,8.5),Block.box(4.0,7.0,5.0,6.0,10.0,7.0),Block.box(6.7936557,0.0,6.7936557,15.2063443,7.0,15.2063443),Block.box(7.4947131,0.0,7.4947131,14.5052869,6.0,14.5052869),Block.box(9.5978852,7.0,9.5978852,12.4021148,10.0,12.4021148)).optimize(),
Shapes.or(Block.box(8.0,0.0,2.0,13.0,10.0,7.0),Block.box(8.5,0.0,2.5,12.5,9.0,6.5),Block.box(9.5,10.0,3.5,11.5,13.0,5.5),Block.box(2.2367317,0.0,7.2367317,9.7632683,7.0,14.7632683),Block.box(2.8639431,0.0,7.8639431,9.1360569,6.0,14.1360569),Block.box(4.7455772,7.0,9.7455772,7.2544228,10.0,12.2544228)).optimize(),
Shapes.or(Block.box(9.0,0.0,8.0,14.0,10.0,13.0),Block.box(9.5,0.0,8.5,13.5,9.0,12.5),Block.box(10.5,10.0,9.5,12.5,13.0,11.5),Block.box(1.2367317,0.0,2.2367317,8.7632683,7.0,9.7632683),Block.box(1.8639431,0.0,2.8639431,8.1360569,6.0,9.1360569),Block.box(3.7455772,7.0,4.7455772,6.2544228,10.0,7.2544228)).optimize(),
Shapes.or(Block.box(3.0,0.0,9.0,8.0,10.0,14.0),Block.box(3.5,0.0,9.5,7.5,9.0,13.5),Block.box(4.5,10.0,10.5,6.5,13.0,12.5),Block.box(6.2367317,0.0,1.2367317,13.7632683,7.0,8.7632683),Block.box(6.8639431,0.0,1.8639431,13.1360569,6.0,8.1360569),Block.box(8.7455772,7.0,3.7455772,11.2544228,10.0,6.2544228)).optimize(),
Shapes.or(Block.box(2.0,0.0,3.0,7.0,10.0,8.0),Block.box(2.5,0.0,3.5,6.5,9.0,7.5),Block.box(3.5,10.0,4.5,5.5,13.0,6.5),Block.box(7.2367317,0.0,6.2367317,14.7632683,7.0,13.7632683),Block.box(7.8639431,0.0,6.8639431,14.1360569,6.0,13.1360569),Block.box(9.7455772,7.0,8.7455772,12.2544228,10.0,11.2544228)).optimize()};
    public static int layout(List<ItemStack> items) {
        int d=0,b=0;for(var stack:items) if(stack.getItem() instanceof TinctureItem item) { if(item.kind==Tincture.DANDELION) d++;else b++; }
        return d>0 && b>0?5:b>0?(b==1?3:4):Math.max(0,Math.min(2,d-1));
    }
    public static int slot(List<ItemStack> items,int layout,int part) {
        if(layout!=5) return part;
        var kind=part==0?Tincture.DANDELION:Tincture.BERRY;
        for(int i=0;i<items.size();i++) if(items.get(i).getItem() instanceof TinctureItem item && item.kind==kind) return i;
        return 0;
    }
    public static String model(int layout,int part) { return NAMES[layout]+(CENTERS[layout].length==1?"":"_"+part); }
    private static int turn(Direction direction) { return switch(direction) {case EAST -> 1;case SOUTH -> 2;case WEST -> 3;default -> 0;}; }
    public static VoxelShape shape(int layout,Direction facing) { return SHAPES[layout*4+turn(facing)]; }
    public static int selectedSlot(List<ItemStack> items,Direction facing,BlockPos pos,Vec3 hit) {
        double x=hit.x-pos.getX(),z=hit.z-pos.getZ();
        for(int i=0;i<turn(facing);i++) { double old=x;x=z;z=1-old; }
        int layout=layout(items),best=0;double distance=Double.MAX_VALUE;
        for(int i=0;i<CENTERS[layout].length;i++) { var c=CENTERS[layout][i];double d=Math.pow(x-c[0],2)+Math.pow(z-c[1],2);if(d<distance) { distance=d;best=i; } }
        return slot(items,layout,best);
    }
}
