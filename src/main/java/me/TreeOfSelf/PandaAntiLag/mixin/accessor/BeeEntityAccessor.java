package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.world.entity.animal.bee.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Bee.class)
public interface BeeEntityAccessor {
    @Accessor("timeSinceSting")
    int getTimeSinceSting();

    @Accessor("timeSinceSting")
    void setTimeSinceSting(int ticks);

    @Accessor("ticksWithoutNectarSinceExitingHive")
    int getTicksWithoutNectarSinceExitingHive();

    @Accessor("ticksWithoutNectarSinceExitingHive")
    void setTicksWithoutNectarSinceExitingHive(int ticks);

    @Accessor("stayOutOfHiveCountdown")
    int getStayOutOfHiveCountdown();

    @Accessor("stayOutOfHiveCountdown")
    void setStayOutOfHiveCountdown(int ticks);

    @Accessor("remainingCooldownBeforeLocatingNewHive")
    int getRemainingCooldownBeforeLocatingNewHive();

    @Accessor("remainingCooldownBeforeLocatingNewHive")
    void setRemainingCooldownBeforeLocatingNewHive(int ticks);

    @Accessor("remainingCooldownBeforeLocatingNewFlower")
    int getRemainingCooldownBeforeLocatingNewFlower();

    @Accessor("remainingCooldownBeforeLocatingNewFlower")
    void setRemainingCooldownBeforeLocatingNewFlower(int ticks);
}
