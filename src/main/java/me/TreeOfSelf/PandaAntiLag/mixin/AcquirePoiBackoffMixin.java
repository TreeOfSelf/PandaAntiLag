package me.TreeOfSelf.PandaAntiLag.mixin;

import com.mojang.datafixers.util.Pair;
import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.ThrottledMovement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

// Villagers look for a job site, a bed and a meeting point by pathfinding to the closest 5 free ones of each
// (range 48) every 1-2 seconds until they get one. Vanilla only backs off per POI, so a villager that can't reach
// any of them (trading-hall cells, bedless villages, beds behind walls) picks the next 5 and pathfinds again,
// forever. On the lag simulation that was ~45% of all villager time, and it's timed by game time, so throttling
// the villager's ticks doesn't reduce it. After a failed search, wait poiSearchBackoffTicks (+ up to the same
// again, random) before that villager searches for that kind of POI again. Successful searches are unchanged.
@Mixin(AcquirePoi.class)
public abstract class AcquirePoiBackoffMixin {

	@Unique
	private static int pandaAntiLag$kind(Set<Pair<Holder<PoiType>, BlockPos>> pois) {
		Holder<PoiType> type = pois.iterator().next().getFirst();
		if (type.is(PoiTypes.HOME)) return 0;
		if (type.is(PoiTypes.MEETING)) return 1;
		return 2;
	}

	@Inject(method = "findPathToPois", at = @At("HEAD"), cancellable = true)
	private static void pandaAntiLag$skipWhileBackingOff(Mob body, Set<Pair<Holder<PoiType>, BlockPos>> pois, CallbackInfoReturnable<Path> cir) {
		if (AntiLagSettings.poiSearchBackoffTicks <= 0 || pois.isEmpty() || !(body instanceof ThrottledMovement mob)) return;
		if (body.level().getGameTime() < mob.pandaAntiLag$poiBackoffUntil()[pandaAntiLag$kind(pois)]) cir.setReturnValue(null);
	}

	@Inject(method = "findPathToPois", at = @At("RETURN"))
	private static void pandaAntiLag$backOffOnFailure(Mob body, Set<Pair<Holder<PoiType>, BlockPos>> pois, CallbackInfoReturnable<Path> cir) {
		int backoff = AntiLagSettings.poiSearchBackoffTicks;
		if (backoff <= 0 || pois.isEmpty() || !(body instanceof ThrottledMovement mob)) return;
		Path path = cir.getReturnValue();
		long[] until = mob.pandaAntiLag$poiBackoffUntil();
		int kind = pandaAntiLag$kind(pois);
		long now = body.level().getGameTime();
		if (path != null && path.canReach()) until[kind] = 0;
		else if (now >= until[kind]) until[kind] = now + backoff + body.getRandom().nextInt(backoff + 1);
	}
}
