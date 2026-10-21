package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.world.entity.animal.frog.Tadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Tadpole.class)
public interface TadpoleEntityAccessor {
    @Accessor("age")
    int getAge();

    @Accessor("age")
    void setAge(int age);

    // Calls Tadpole#setAge(int), which runs the age >= ticksToBeFrog check and ageUp().
    // Writing the field directly skips that conversion and strands tadpoles at Age > 24000.
    @Invoker("setAge")
    void invokeSetAge(int age);
}
