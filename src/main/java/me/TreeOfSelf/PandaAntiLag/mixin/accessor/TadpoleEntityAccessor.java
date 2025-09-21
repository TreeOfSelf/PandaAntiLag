package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.passive.TadpoleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TadpoleEntity.class)
public interface TadpoleEntityAccessor {
    @Accessor("tadpoleAge")
    int getTadpoleAge();

    @Accessor("tadpoleAge")
    void setTadpoleAge(int age);
}