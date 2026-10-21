package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.world.entity.animal.frog.Tadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Tadpole.class)
public interface TadpoleEntityAccessor {
    @Accessor("age")
    int getAge();

    // Calls the real Tadpole#setAge(int), which runs the age >= ticksToBeFrog check and ageUp().
    // Do NOT add an @Accessor named setAge here: it shadows the real method and the invoker calls the field setter.
    @Invoker("setAge")
    void invokeSetAge(int age);
}
