package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.world.entity.animal.frog.Tadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tadpole.class)
public interface TadpoleEntityAccessor {
    @Accessor("age")
    int getAge();

    @Accessor("age")
    void setAge(int age);
}
