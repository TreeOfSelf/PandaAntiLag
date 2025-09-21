package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.passive.BeeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeeEntity.class)
public interface BeeEntityAccessor {
    @Accessor("ticksSinceSting")
    int getTicksSinceSting();

    @Accessor("ticksSinceSting")
    void setTicksSinceSting(int ticks);

    @Accessor("ticksSincePollination")
    int getTicksSincePollination();

    @Accessor("ticksSincePollination")
    void setTicksSincePollination(int ticks);

    @Accessor("cannotEnterHiveTicks")
    int getCannotEnterHiveTicks();

    @Accessor("cannotEnterHiveTicks")
    void setCannotEnterHiveTicks(int ticks);

    @Accessor("ticksLeftToFindHive")
    int getTicksLeftToFindHive();

    @Accessor("ticksLeftToFindHive")
    void setTicksLeftToFindHive(int ticks);

    @Accessor("ticksUntilCanPollinate")
    int getTicksUntilCanPollinate();

    @Accessor("ticksUntilCanPollinate")
    void setTicksUntilCanPollinate(int ticks);
}