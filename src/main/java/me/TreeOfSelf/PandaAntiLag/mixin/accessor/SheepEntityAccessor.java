package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sheep.class)
public interface SheepEntityAccessor {
    @Accessor("eatBlockGoal")
    EatBlockGoal getEatBlockGoal();
}
