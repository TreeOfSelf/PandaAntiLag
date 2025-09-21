package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.entity.passive.WanderingTraderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WanderingTraderEntity.class)
public interface WanderingTraderEntityAccessor {
    @Accessor("despawnDelay")
    int getDespawnDelay();

    @Accessor("despawnDelay")
    void setDespawnDelay(int timer);
}

