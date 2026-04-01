package me.TreeOfSelf.PandaAntiLag.mixin.accessor;

import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Villager.class)
public interface VillagerEntityAccessor {
    @Accessor("updateMerchantTimer")
    int getUpdateMerchantTimer();

    @Accessor("updateMerchantTimer")
    void setUpdateMerchantTimer(int timer);
}
