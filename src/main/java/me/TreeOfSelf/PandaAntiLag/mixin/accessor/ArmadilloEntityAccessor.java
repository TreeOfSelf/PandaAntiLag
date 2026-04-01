package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.world.entity.animal.armadillo.Armadillo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Armadillo.class)
public interface ArmadilloEntityAccessor {
    @Accessor("scuteTime")
    int getScuteTime();

    @Accessor("scuteTime")
    void setScuteTime(int time);
}
