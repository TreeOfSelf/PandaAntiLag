package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.passive.GoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GoatEntity.class)
public interface GoatEntityAccessor {
    @Accessor("preparingRam")
    boolean isPreparingRam();

    @Accessor("headPitch")
    int getHeadPitch();

    @Accessor("headPitch")
    void setHeadPitch(int headPitch);
}