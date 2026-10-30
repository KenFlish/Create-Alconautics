package dev.alconautics;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue NORMAL_TICKS, SUPER_TICKS, PIPE_RANGE, BURST_TICKS;
    public static final ModConfigSpec.IntValue TINCTURE_DAYS_PER_YEAR;
    static {
        var b = new ModConfigSpec.Builder();
        NORMAL_TICKS = b.comment("Ticks for 100 mB at normal heat.").defineInRange("normalTicks", 400, 20, 72000);
        SUPER_TICKS = b.comment("Ticks for 100 mB at superheat.").defineInRange("superTicks", 260, 20, 72000);
        PIPE_RANGE = b.defineInRange("pipeRange", 64, 2, 256);
        BURST_TICKS = b.comment("Ticks without a whistle before rupture at superheat; normal heat takes twice as long.").defineInRange("burstTicks", 600, 100, 12000);
        TINCTURE_DAYS_PER_YEAR=b.comment("Minecraft days per year of bottled tincture aging. Moonshine does not age.").defineInRange("tinctureDaysPerYear",8,1,365);
        SPEC = b.build();
    }
    private Config() {}
}
