package dev.alconautics;

import java.util.List;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MachineBE extends SmartBlockEntity implements IHaveGoggleInformation {
    protected MachineBE(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type,pos,state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
    public boolean interact(Player p, InteractionHand hand) { return false; }
    public void dropContents() {}
    public void changed() { setChanged(); notifyUpdate(); }
    protected void line(List<Component> tooltip, String key, Object... args) { tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.alconautics." + key, args))); }
}
