package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// Vanilla tries to spawn in every chunk near every player every tick; most attempts fail, and each one still looks
// up biome, structures and blocks. On the lag simulation that was ~10% of the main thread. While the server is over
// its tick budget (same signal as the overload mob throttle), each chunk only tries every N ticks, staggered per
// chunk so spawning stays even. Caps still fill, a little slower while the server is struggling. The pass every 400
// ticks that spawns animals and other persistent mobs is never skipped.
@Mixin(NaturalSpawner.class)
public abstract class SpawnOverloadMixin {

	@Inject(method = "spawnForChunk", at = @At("HEAD"), cancellable = true)
	private static void pandaAntiLag$throttleSpawnAttempts(ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState state,
														  List<MobCategory> spawningCategories, CallbackInfo ci) {
		int divisor = Math.min(AntiLagSettings.currentOverloadDivisor, AntiLagSettings.spawnOverloadMaxDivisor);
		if (divisor <= 1) return;
		long time = level.getGameTime();
		if (time % 400L == 0L) return;
		int x = chunk.getPos().x(), z = chunk.getPos().z();
		int stagger = (x * 31 + z) & 0x7fffffff;
		if ((time + stagger) % divisor != 0) ci.cancel();
	}
}
