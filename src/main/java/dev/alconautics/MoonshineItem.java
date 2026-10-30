package dev.alconautics;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class MoonshineItem extends Item {
    public final Drink drink;
    public MoonshineItem(Properties properties,Drink drink) { super(properties); this.drink=drink; }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) { player.startUsingItem(hand); return InteractionResultHolder.consume(player.getItemInHand(hand)); }
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity) {
        if (!level.isClientSide) {
            if(drink.kind()!=Drink.Kind.VEGETABLE) {
                var effect=drink.kind()==Drink.Kind.SUGAR?Content.GROW:Content.SHRINK;
                var opposite=drink.kind()==Drink.Kind.SUGAR?Content.SHRINK:Content.GROW;
                entity.removeEffect(opposite);
                var current=entity.getEffect(effect);
                if(current==null || current.getAmplifier()<=drink.variant().amplifier()) entity.addEffect(new MobEffectInstance(effect,drink.variant().ticks(),drink.variant().amplifier()));
            }
            BreweryCompat.drink(entity);
        }
        if(entity instanceof Player p && p.isCreative()) return stack;
        stack.shrink(1);
        if(stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
        if(entity instanceof Player p && !p.addItem(new ItemStack(Items.GLASS_BOTTLE))) p.drop(new ItemStack(Items.GLASS_BOTTLE),false);
        return stack;
    }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        var p=ctx.getPlayer();
        if(p!=null && ctx.getLevel().getBlockState(ctx.getClickedPos()).is(Content.BOTTLE.get())) {
            var level=ctx.getLevel();var pos=ctx.getClickedPos();
            if(!BottleBlock.mayUse(level,pos,p,ctx.getClickedFace(),ctx.getItemInHand())) return InteractionResult.FAIL;
            if(!level.isClientSide && level.getBlockEntity(pos) instanceof BottleBE bottles && bottles.addBottle(ctx.getItemInHand()) && !p.isCreative()) ctx.getItemInHand().shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(p==null || !p.isShiftKeyDown() || ctx.getClickedFace()!=Direction.UP) return InteractionResult.PASS;
        var pos=ctx.getClickedPos().above(); var level=ctx.getLevel();
        if(!level.mayInteract(p,pos) || !p.mayUseItemAt(pos,Direction.UP,ctx.getItemInHand()) || !level.getBlockState(pos).canBeReplaced()) return InteractionResult.FAIL;
        BlockState state=Content.BOTTLE.get().defaultBlockState().setValue(BottleBlock.DRINK,Drink.ALL.indexOf(drink)).setValue(BottleBlock.FACING,p.getDirection().getOpposite());
        if(!state.canSurvive(level,pos)) return InteractionResult.FAIL;
        if(!level.isClientSide) { level.setBlockAndUpdate(pos,state); if(level.getBlockEntity(pos) instanceof BottleBE bottles) bottles.placeFirst(ctx.getItemInHand()); if(!p.isCreative()) ctx.getItemInHand().shrink(1); }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext ctx,List<Component> out,TooltipFlag flag) {
        out.add(Component.translatable("drink_kind.alconautics."+drink.kind().id()).withStyle(net.minecraft.ChatFormatting.GRAY));
        DrinkTooltip.append(drink,out::add,ctx.tickRate());
    }
}
