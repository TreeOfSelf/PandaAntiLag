package me.TreeOfSelf.PandaAntiLag.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.LoadedChunks;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Vanilla skips a player's entity tick while they touch an unloaded chunk, but only for spectators:
//     if (!this.isSpectator() || !this.touchingUnloadedChunk()) super.tick();
// For everyone else the tick runs anyway, and movement asks for the block under their feet
// (travel -> getOnPos -> getBlockState), which loads that chunk *blocking*: the whole server waits until it
// exists. Right after a player arrives somewhere (portal, login, pearl, elytra faster than chunks load), and
// with a deep chunk queue, that wait was 10+ second freezes on the lag simulation.
// Apply vanilla's spectator rule to every player: their tick waits until the chunks around them are there.
// touchingUnloadedChunk alone isn't enough: it counts a chunk that is only requested as loaded (LoadedChunks).
// The client keeps moving them; movement packets are still checked by the packet handler as usual.
@Mixin(ServerPlayer.class)
public abstract class PlayerUnloadedChunkMixin {

	@ModifyExpressionValue(
			method = "doTick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z", ordinal = 0)
	)
	private boolean pandaAntiLag$waitForChunks(boolean spectator) {
		return spectator || AntiLagSettings.waitForChunksBeforePlayerTick;
	}

	@ModifyExpressionValue(
			method = "doTick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;touchingUnloadedChunk()Z", ordinal = 0)
	)
	private boolean pandaAntiLag$requestedIsNotLoaded(boolean touchingUnloaded) {
		if (touchingUnloaded || !AntiLagSettings.waitForChunksBeforePlayerTick) return touchingUnloaded;
		ServerPlayer player = (ServerPlayer) (Object) this;
		return !LoadedChunks.allReady(player.level(), player.getBoundingBox().inflate(1.0));
	}

	// Also in doTick, outside that check: once a second the location advancements read the biome and structures where
	// the player is, which blocks the same way. Wait for the next second instead. Biomes are blurred a few blocks
	// sideways, so the neighbouring chunk can be the one that gets read.
	@WrapWithCondition(
			method = "doTick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/triggers/PlayerTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;)V")
	)
	private boolean pandaAntiLag$locationWaitsForChunks(PlayerTrigger trigger, ServerPlayer player) {
		return !AntiLagSettings.waitForChunksBeforePlayerTick
				|| LoadedChunks.allReady(player.level(), player.getBoundingBox().inflate(8.0));
	}
}
