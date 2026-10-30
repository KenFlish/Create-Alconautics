package dev.alconautics;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class TinctureItem extends Item {
    public final Tincture kind;
    public TinctureItem(Properties properties,Tincture kind) { super(properties);this.kind=kind; }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) { player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand)); }
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected) { TinctureAge.update(stack,level); }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity) {
        if(!level.isClientSide) {
            TinctureAge.update(stack,level);
            entity.addEffect(new MobEffectInstance(kind.effect(),TinctureAge.duration(stack,kind),TinctureAge.amplifier(stack)));
            BreweryCompat.drink(entity);
        }
        if(entity instanceof Player player && player.isCreative()) return stack;
        stack.shrink(1);
        if(stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
        if(entity instanceof Player player && !player.addItem(new ItemStack(Items.GLASS_BOTTLE))) player.drop(new ItemStack(Items.GLASS_BOTTLE),false);
        return stack;
    }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        var player=ctx.getPlayer();var level=ctx.getLevel();var clicked=ctx.getClickedPos();
        if(player==null) return InteractionResult.PASS;
        if(level.getBlockState(clicked).is(Content.TINCTURE_BOTTLES.get())) {
            if(!BottleBlock.mayUse(level,clicked,player,ctx.getClickedFace(),ctx.getItemInHand())) return InteractionResult.FAIL;
            if(!level.isClientSide && level.getBlockEntity(clicked) instanceof BottleBE bottles && bottles.addBottle(ctx.getItemInHand())) {
                if(!player.isCreative()) ctx.getItemInHand().shrink(1);BottleBlock.placedSound(level,clicked);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(!player.isShiftKeyDown()) return InteractionResult.PASS;
        var pos=clicked.relative(ctx.getClickedFace());
        if(!BottleBlock.mayUse(level,pos,player,ctx.getClickedFace(),ctx.getItemInHand()) || !level.getBlockState(pos).canBeReplaced()) return InteractionResult.FAIL;
        if(!level.isClientSide) {
            level.setBlockAndUpdate(pos,Content.TINCTURE_BOTTLES.get().defaultBlockState().setValue(BottleBlock.FACING,player.getDirection().getOpposite()));
            if(level.getBlockEntity(pos) instanceof BottleBE bottles) bottles.placeFirst(ctx.getItemInHand());
            if(!player.isCreative()) ctx.getItemInHand().shrink(1);BottleBlock.placedSound(level,pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> out,TooltipFlag flag) {
        PotionContents.addPotionTooltip(List.of(new MobEffectInstance(kind.effect(),TinctureAge.duration(stack,kind),TinctureAge.amplifier(stack))),out::add,1,context.tickRate());
        out.add(Component.empty());
        out.add(Component.translatable("tooltip.alconautics.tincture_age",TinctureAge.years(stack)).withStyle(net.minecraft.ChatFormatting.WHITE));
        int days=TinctureAge.daysUntilNextLevel(stack);
        out.add(Component.empty());
        if(days>0) {
            out.add(Component.translatable("tooltip.alconautics.tincture_next_quality",days).withStyle(style -> style.withColor(0x93C47D)));
        } else {
            out.add(Component.translatable("tooltip.alconautics.tincture_fully_aged").withStyle(net.minecraft.ChatFormatting.GOLD));
        }
    }
}
