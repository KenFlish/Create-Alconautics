package dev.alconautics;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
/** Generated bottle centers; shared interaction box matches Brewery and Vinery. */
public final class TinctureGeometry {
    public static final String[] MODELS = {"dandelion_tincture_single","dandelion_tincture_pair_0","dandelion_tincture_pair_1","dandelion_tincture_triple_0","dandelion_tincture_triple_1","dandelion_tincture_triple_2","berry_tincture_single","berry_tincture_pair_0","berry_tincture_pair_1","tincture_mixed_0","tincture_mixed_1"};
    public static final String[] NAMES = {"dandelion_tincture_single","dandelion_tincture_pair","dandelion_tincture_triple","berry_tincture_single","berry_tincture_pair","tincture_mixed"};
    public static final float[][][] CENTERS = {{{0.53125f,0.46875f}},{{0.34375f,0.28125f},{0.65625f,0.65625f}},{{0.59375f,0.28125f},{0.71875f,0.65625f},{0.28125f,0.65625f}},{{0.5f,0.5f}},{{0.3125f,0.3125f},{0.6875f,0.625f}},{{0.71875f,0.28125f},{0.375f,0.625f}}};
    private static final VoxelShape SHAPE=Block.box(2,0,2,14,14,14);
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
    public static VoxelShape shape(int layout,Direction facing) { return SHAPE; }
    public static int selectedSlot(List<ItemStack> items,Direction facing,BlockPos pos,Vec3 hit) {
        double x=hit.x-pos.getX(),z=hit.z-pos.getZ();
        for(int i=0;i<turn(facing);i++) { double old=x;x=z;z=1-old; }
        int layout=layout(items),best=0;double distance=Double.MAX_VALUE;
        for(int i=0;i<CENTERS[layout].length;i++) { var c=CENTERS[layout][i];double d=Math.pow(x-c[0],2)+Math.pow(z-c[1],2);if(d<distance) { distance=d;best=i; } }
        return slot(items,layout,best);
    }
}
