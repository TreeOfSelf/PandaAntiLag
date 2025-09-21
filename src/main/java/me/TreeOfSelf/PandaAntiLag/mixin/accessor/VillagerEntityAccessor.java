package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor("levelUpTimer")
    int getLevelUpTimer();

    @Accessor("levelUpTimer")
    void setLevelUpTimer(int timer);

    @Accessor("levelingUp")
    boolean isLevelingUp();

    @Accessor("levelingUp")
    void setLevelingUp(boolean levelingUp);
}