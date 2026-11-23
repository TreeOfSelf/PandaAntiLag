package me.TreeOfSelf.PandaAntiLag.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.ThrottledMovement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

// Villagers look for a job site, a bed and a meeting point by pathfinding to the closest 5 free ones of each
// (range 48) every 1-2 seconds until they get one. A villager that can't reach any of them (trading-hall cells,
// beds behind walls) keeps doing that forever; on the lag simulation it was ~45% of all villager time.
// With poiSearchBackoffTicks set, the POIs a search failed to reach are left out of that villager's next searches for
// a while. Leaving them out before vanilla picks its 5 means a POI it hasn't tried (a new bed, a replaced job block)
// is still tried at once, and vanilla's own retry timers only count searches that really happened.
@Mixin(AcquirePoi.class)
public abstract class AcquirePoiBackoffMixin {

	@WrapOperation(method = "lambda$create$3", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;findAllClosestFirstWithType(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;"))
	private static Stream<Pair<Holder<PoiType>, BlockPos>> pandaAntiLag$leaveOutUnreachable(
			PoiManager manager, Predicate<Holder<PoiType>> type, Predicate<BlockPos> filter, BlockPos center, int range,
			PoiManager.Occupancy occupancy, Operation<Stream<Pair<Holder<PoiType>, BlockPos>>> original, @Local(argsOnly = true) PathfinderMob body) {
		if (AntiLagSettings.poiSearchBackoffTicks > 0 && body instanceof ThrottledMovement mob) {
			Long2LongOpenHashMap until = mob.pandaAntiLag$poiBackoffUntil();
			if (!until.isEmpty()) {
				long now = body.level().getGameTime();
				until.values().removeIf(time -> time <= now);
				Predicate<BlockPos> vanilla = filter;
				// Ours first: vanilla's filter marks an attempt on every POI it lets through
				filter = pos -> !until.containsKey(pos.asLong()) && vanilla.test(pos);
			}
		}
		return original.call(manager, type, filter, center, range, occupancy);
	}

	@WrapOperation(method = "lambda$create$3", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/behavior/AcquirePoi;findPathToPois(Lnet/minecraft/world/entity/Mob;Ljava/util/Set;)Lnet/minecraft/world/level/pathfinder/Path;"))
	private static Path pandaAntiLag$rememberUnreachable(Mob body, Set<Pair<Holder<PoiType>, BlockPos>> pois, Operation<Path> original) {
		Path path = original.call(body, pois);
		int backoff = AntiLagSettings.poiSearchBackoffTicks;
		if (backoff > 0 && (path == null || !path.canReach()) && body instanceof ThrottledMovement mob) {
			Long2LongOpenHashMap until = mob.pandaAntiLag$poiBackoffUntil();
			long now = body.level().getGameTime();
			for (Pair<Holder<PoiType>, BlockPos> poi : pois) {
				until.put(poi.getSecond().asLong(), now + backoff + body.getRandom().nextInt(backoff + 1));
			}
		}
		return path;
	}
}
