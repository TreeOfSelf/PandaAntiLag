package me.TreeOfSelf.PandaAntiLag.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
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
}
