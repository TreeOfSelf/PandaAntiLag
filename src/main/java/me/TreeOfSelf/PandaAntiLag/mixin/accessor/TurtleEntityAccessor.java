package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.world.entity.animal.turtle.Turtle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Turtle.class)
public interface TurtleEntityAccessor {
    @Accessor("layEggCounter")
    int getLayEggCounter();

    @Accessor("layEggCounter")
    void setLayEggCounter(int counter);
}
