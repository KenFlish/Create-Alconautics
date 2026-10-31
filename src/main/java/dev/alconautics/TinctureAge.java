package dev.alconautics;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** Per-bottle server clock: changing the daylight time never changes its age. */
public final class TinctureAge {
    public static final String BORN="alconautics.tincture_born",YEARS="alconautics.tincture_years",YEAR_LENGTH="alconautics.tincture_year_length",DAYS="alconautics.tincture_days";
    public static long now(Level level) { return level.getServer()==null?level.getGameTime():level.getServer().overworld().getGameTime(); }
    public static boolean update(ItemStack stack,Level level) {
        if(!(stack.getItem() instanceof TinctureItem) || level.isClientSide) return false;
        return update(stack,now(level));
    }
    public static boolean update(ItemStack stack,long now) {
        if(!(stack.getItem() instanceof TinctureItem)) return false;
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        boolean fresh=!data.contains(BORN);
        if(fresh) { data.putLong(BORN,now);data.putLong(YEAR_LENGTH,Config.TINCTURE_DAYS_PER_YEAR.get()*24000L); }
        long year=Math.max(24000,data.getLong(YEAR_LENGTH));
        int age=(int)Math.min(10000,Math.max(0,now-data.getLong(BORN))/year);
        int days=(int)Math.min(Integer.MAX_VALUE,Math.max(0,now-data.getLong(BORN))/24000L);
        if(!fresh && age==data.getInt(YEARS) && data.contains(DAYS) && days==data.getInt(DAYS)) return false;
        data.putInt(YEARS,age);data.putInt(DAYS,days);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));return true;
    }
    public static int years(ItemStack stack) { return Math.max(0,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt(YEARS)); }
    public static int duration(ItemStack stack,Tincture kind) { return kind.baseDuration()*(10+Math.min(30,years(stack)))/10; }
    public static int amplifier(ItemStack stack) { return years(stack)>=5?1:0; }
    /** Project barrel aging onto a copy; Brewery persists the original stack and entry time. */
    public static ItemStack inBarrel(ItemStack stack,Level level,long storedAt) {
        var copy=stack.copy();long time=now(level);
        update(copy,time);
        var data=copy.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        long target=5*Math.max(24000,data.getLong(YEAR_LENGTH));
        long cycle=net.satisfy.brewery.core.block.entity.BigBarrelBlockEntity.TICKS_PER_QUALITY;
        long elapsed=Math.min(cycle,Math.max(0,level.getGameTime()-storedAt));
        long natural=Math.max(0,time-data.getLong(BORN));
        long bonus=Math.min(Math.max(0,target-natural),elapsed*Math.max(0,target-cycle)/cycle);
        data.putLong(BORN,data.getLong(BORN)-bonus);
        copy.set(DataComponents.CUSTOM_DATA,CustomData.of(data));update(copy,time);
        return copy;
    }
    public static int maturityPercent(ItemStack stack,long time) {
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        long target=5*Math.max(24000,data.getLong(YEAR_LENGTH));
        return (int)(Math.min(target,Math.max(0,time-data.getLong(BORN)))*100/target);
    }
    public static int daysUntilNextLevel(ItemStack stack) {
        if(years(stack)>=5) return -1;
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        long year=data.contains(YEAR_LENGTH)?Math.max(24000,data.getLong(YEAR_LENGTH)):Config.TINCTURE_DAYS_PER_YEAR.get()*24000L;
        long days=data.contains(DAYS)?data.getInt(DAYS):years(stack)*(year/24000L);
        return (int)Math.max(1,(5*year)/24000L-days);
    }
}
