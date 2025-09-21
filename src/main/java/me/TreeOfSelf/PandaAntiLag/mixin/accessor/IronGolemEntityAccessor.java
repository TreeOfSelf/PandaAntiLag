package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.passive.IronGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IronGolemEntity.class)
public interface IronGolemEntityAccessor {
    @Accessor("attackTicksLeft")
    int getAttackTicksLeft();

    @Accessor("attackTicksLeft")
    void setAttackTicksLeft(int ticks);

    @Accessor("lookingAtVillagerTicksLeft")
    int getLookingAtVillagerTicksLeft();

    @Accessor("lookingAtVillagerTicksLeft")
    void setLookingAtVillagerTicksLeft(int ticks);
}