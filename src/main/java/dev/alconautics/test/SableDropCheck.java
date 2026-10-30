package dev.alconautics.test;

import dev.alconautics.*;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import java.util.List;

final class SableDropCheck {
    static void run(GameTestHelper h) {
        var pos=new BlockPos(2,7,2);var absolute=h.absolutePos(pos);
        for(int x=0;x<5;x++) for(int z=0;z<5;z++) h.setBlock(new BlockPos(x,1,z),Blocks.STONE);
        h.setBlock(pos,Content.BOTTLE.get());BottleBE bottles=h.getBlockEntity(pos);
        bottles.placeFirst(Content.DRINKS.get(new Drink(Drink.Kind.GRAIN,Drink.Variant.NORMAL)).get().getDefaultInstance());
        bottles.addBottle(Content.DRINKS.get(new Drink(Drink.Kind.SUGAR,Drink.Variant.NORMAL)).get().getDefaultInstance());
        var sub=SubLevelAssemblyHelper.assembleBlocks(h.getLevel(),absolute,List.of(absolute),BoundingBox3i.from(List.of(absolute)));
        h.assertTrue(sub!=null,"Bottle assembly failed");
        var area=new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(h.absolutePos(new BlockPos(0,0,0))),net.minecraft.world.phys.Vec3.atLowerCornerOf(h.absolutePos(new BlockPos(5,10,5))));
        h.runAtTickTime(65,() -> {
            var clouds=h.getLevel().getEntitiesOfClass(AreaEffectCloud.class,area,VapourCloud::isOurs);
            h.assertTrue(!clouds.isEmpty(),"Physical bottle did not burst and create a cloud on impact");
            h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,area,e -> e.getItem().getItem() instanceof MoonshineItem).isEmpty(),"Shattered bottles must not drop intact drinks");
            h.assertTrue(h.getBlockState(new BlockPos(2,1,2)).is(Blocks.STONE),"Weak bottle explosion must preserve the floor");
            h.succeed();
        });
    }
}
