package dev.alconautics;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Old chunks contain bottle block states, but no bottle entity NBT. */
@EventBusSubscriber(modid=Alconautics.ID)
public final class BottleMigration {
    @SubscribeEvent public static void loaded(ChunkEvent.Load event) {
        if(event.getChunk() instanceof LevelChunk chunk && event.getLevel() instanceof ServerLevel level)
            level.getServer().execute(() -> {
                if(level.getChunkSource().getChunkNow(chunk.getPos().x,chunk.getPos().z)==chunk) repair(chunk);
            });
    }
    public static void repair(LevelChunk chunk) {
        var sections=chunk.getSections();var origin=chunk.getPos().getWorldPosition();
        for(int index=0;index<sections.length;index++) {
            var section=sections[index];
            if(!section.maybeHas(state -> state.is(Content.BOTTLE.get()))) continue;
            int bottom=chunk.getSectionYFromSectionIndex(index)*16;
            for(int x=0;x<16;x++) for(int y=0;y<16;y++) for(int z=0;z<16;z++) {
                if(!section.getBlockState(x,y,z).is(Content.BOTTLE.get())) continue;
                var pos=new BlockPos(origin.getX()+x,bottom+y,origin.getZ()+z);
                if(chunk.getBlockEntity(pos,LevelChunk.EntityCreationType.CHECK)==null
                    && chunk.getBlockEntity(pos,LevelChunk.EntityCreationType.IMMEDIATE) instanceof BottleBE bottles)
                    bottles.getInventory();
            }
        }
    }
}
