package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.passive.ArmadilloEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmadilloEntity.class)
public interface ArmadilloEntityAccessor {
    @Accessor("nextScuteShedCooldown")
    int getNextScuteShedCooldown();

    @Accessor("nextScuteShedCooldown")
    void setNextScuteShedCooldown(int cooldown);
}