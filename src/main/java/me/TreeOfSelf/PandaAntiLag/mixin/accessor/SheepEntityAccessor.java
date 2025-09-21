package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SheepEntity.class)
public interface SheepEntityAccessor {
    @Accessor("eatGrassGoal")
    EatGrassGoal getEatGrassGoal();
}