package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.passive.TurtleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TurtleEntity.class)
public interface TurtleEntityAccessor {
    @Accessor("sandDiggingCounter")
    int getSandDiggingCounter();

    @Accessor("sandDiggingCounter")
    void setSandDiggingCounter(int counter);
}



