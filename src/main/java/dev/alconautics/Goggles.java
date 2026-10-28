package dev.alconautics;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Create-style vertical bars with the requested fixed three-stage display. */
public final class Goggles {
    private Goggles() {}
    public static Component bars(int level) {
        int active=Math.clamp(level,0,3);
        var result=Component.empty();
        if(active==0) result.append(Component.literal("|").withStyle(ChatFormatting.RED));
        else result.append(Component.literal("|".repeat(active)).withStyle(ChatFormatting.GREEN));
        return result.append(Component.literal("|".repeat(3-Math.max(1,active))).withStyle(ChatFormatting.DARK_GRAY));
    }
    public static Component gauge(String label,int level) {
        return Component.translatable("tooltip.alconautics."+label).withStyle(ChatFormatting.GRAY)
            .append(Component.translatable("tooltip.alconautics."+label+"_dots").withStyle(ChatFormatting.DARK_GRAY)).append(bars(level));
    }
}
