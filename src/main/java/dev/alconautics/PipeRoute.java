package dev.alconautics;

import java.util.*;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Validates a bounded serial route, then supplies real Create pipe pressure. */
public record PipeRoute(List<Node> nodes, CoolerBE cooler, DryBE dry, String error) {
    public record Node(BlockPos pos, Direction incoming, Direction outgoing) {}
    public boolean valid() { return error.isEmpty(); }
    public static boolean isPipe(Level l, BlockPos p) { return l.getBlockState(p).getBlock() instanceof FluidPipeBlock || l.getBlockState(p).getBlock() instanceof GlassFluidPipeBlock; }
    public static boolean wet(Level l, BlockPos p) { return isPipe(l,p) && l.getBlockState(p).getOptionalValue(BlockStateProperties.WATERLOGGED).orElse(false); }
    public static PipeRoute scan(Level level, BlockPos head) {
        var state=level.getBlockState(head);
        List<Direction> outlets=java.util.Arrays.stream(Direction.values())
            .filter(d -> MachineBlock.isHeadOutlet(state,d) && level.hasChunkAt(head.relative(d)) && isPipe(level,head.relative(d)))
            .filter(d -> { var b=BlockEntityBehaviour.get(level,head.relative(d),FluidTransportBehaviour.TYPE); return b!=null && b.canHaveFlowToward(level.getBlockState(head.relative(d)),d.getOpposite()); })
            .toList();
        if(outlets.size()!=1) return bad(List.of(),outlets.isEmpty()?"route":"branch");
        return scanFrom(level,head,outlets.getFirst());
    }
    private static PipeRoute scanFrom(Level level, BlockPos head, Direction outlet) {
        var nodes = new ArrayList<Node>(); var visited = new HashSet<BlockPos>();
        BlockPos current = head.relative(outlet), previous = head; DryBE dry = null;
        for (int count=0; count<Config.PIPE_RANGE.get(); count++) {
            if (!level.hasChunkAt(current) || !isPipe(level,current) || !visited.add(current)) return bad(nodes,"route");
            var behaviour = BlockEntityBehaviour.get(level,current,FluidTransportBehaviour.TYPE);
            if (behaviour == null) return bad(nodes,"route");
            Direction incoming = direction(current,previous), outgoing = null;
            for (Direction d: Direction.values()) {
                if (d == incoming || !behaviour.canHaveFlowToward(level.getBlockState(current),d)) continue;
                if(d==Direction.DOWN && level.getBlockEntity(current.below()) instanceof DryBE) continue;
                if (outgoing != null) return bad(nodes,"branch");
                outgoing = d;
            }
            if (outgoing == null) return bad(nodes,"route");
            if (level.getBlockEntity(current.below()) instanceof DryBE jar && incoming != Direction.DOWN && outgoing != Direction.DOWN) {
                if (dry != null) return bad(nodes,"dry_count"); dry = jar;
            }
            nodes.add(new Node(current.immutable(),incoming,outgoing));
            BlockPos next = current.relative(outgoing);
            if (!level.hasChunkAt(next)) return bad(nodes,"route");
            if (level.getBlockEntity(next) instanceof CoolerBE cooler && outgoing == Direction.DOWN) {
                if (nodes.size()<2 || !wet(level,current) || !wet(level,nodes.get(nodes.size()-2).pos())) return bad(nodes,"wet_pipes");
                return new PipeRoute(List.copyOf(nodes),cooler,dry,"");
            }
            previous=current; current=next;
        }
        return bad(nodes,"range");
    }
    private static Direction direction(BlockPos a, BlockPos b) { return Direction.fromDelta(b.getX()-a.getX(),b.getY()-a.getY(),b.getZ()-a.getZ()); }
    private static PipeRoute bad(List<Node> nodes,String error) { return new PipeRoute(List.copyOf(nodes),null,null,error); }
    public void pressure(Level level, float amount) {
        for (Node node:nodes) {
            if (!level.hasChunkAt(node.pos())) continue;
            FluidTransportBehaviour b = BlockEntityBehaviour.get(level,node.pos(),FluidTransportBehaviour.TYPE);
            if (b == null) continue;
            if (amount == 0) { b.wipePressure(); continue; }
            // Set rather than add: repeated upkeep must never accumulate pressure.
            var in=b.getConnection(node.incoming());
            var out=b.getConnection(node.outgoing());
            if(in==null || out==null) continue;
            if(in.getPressure().get(true)==amount && in.getPressure().get(false)==0f
                && out.getPressure().get(false)==amount && out.getPressure().get(true)==0f) continue;
            in.getPressure().set(true,amount); in.getPressure().set(false,0f);
            out.getPressure().set(false,amount); out.getPressure().set(true,0f);
            b.blockEntity.sendData();
        }
    }
}
