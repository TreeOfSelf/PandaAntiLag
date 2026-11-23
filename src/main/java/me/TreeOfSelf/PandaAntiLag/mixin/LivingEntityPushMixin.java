package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.ThrottledMovement;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A throttled mob's movement-only tick keeps it falling, swimming and walking its path, but skips pushing the
// mobs around it. In a dense pile that pairwise push is the expensive part of every tick (every mob against
// every neighbour), and the mobs still push each other on their full ticks.
@Mixin(LivingEntity.class)
public abstract class LivingEntityPushMixin {

	@Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
	private void pandaAntiLag$skipPushWhenMovementOnly(CallbackInfo ci) {
		if (AntiLagSettings.skipPushWhileThrottled && this instanceof ThrottledMovement mob && mob.pandaAntiLag$isMovementOnly()) ci.cancel();
	}
}
