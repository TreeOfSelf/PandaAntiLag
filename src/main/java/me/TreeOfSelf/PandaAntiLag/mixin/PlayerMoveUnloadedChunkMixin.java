package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.LoadedChunks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Every position a client reports (normal movement, and the confirm after a teleport) goes through
// handlePlayerPositionChange, which collision-moves the player with player.move(). If the player's box reaches
// into a chunk that isn't loaded yet, that move loads it *blocking* and the whole server waits. On the lag
// simulation this was the remaining main-thread chunk wait once players' own ticks stopped doing it.
// Like Paper's prevent-moving-into-unloaded-chunks:
//  - right after a teleport the player is already placed at the destination: keep the position, skip the
//    collision move, and update chunk tracking so the destination starts loading.
//  - a normal move into chunks that aren't there yet holds the player where they are (one setback) until they
//    load, instead of freezing every player on the server.
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class PlayerMoveUnloadedChunkMixin {

	@Shadow
	public ServerPlayer player;

	@Shadow
	public abstract void teleport(double x, double y, double z, float yRot, float xRot);

	@Inject(method = "handlePlayerPositionChange", at = @At("HEAD"), cancellable = true)
	private void pandaAntiLag$waitForChunks(double x, double y, double z, float yRot, float xRot,
											 boolean onGround, boolean horizontalCollision, CallbackInfo ci) {
		if (!AntiLagSettings.waitForChunksBeforePlayerMove) return;
		ServerPlayer p = this.player;
		if (p.isPassenger() || p.isSpectator()) return;
		AABB here = p.getBoundingBox();
		AABB there = here.move(x - p.getX(), y - p.getY(), z - p.getZ());
		AABB swept = here.minmax(there).inflate(1.0);
		if (LoadedChunks.allReady(p.level(), swept)) return;

		double dx = x - p.getX(), dy = y - p.getY(), dz = z - p.getZ();
		if (dx * dx + dy * dy + dz * dz < 1.0E-4) {
			// Teleport confirm (or standing still): already where they should be
			p.absSnapRotationTo(Mth.wrapDegrees(yRot), Mth.wrapDegrees(xRot));
			p.level().getChunkSource().move(p);
		} else {
			this.teleport(p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot());
		}
		ci.cancel();
	}
}
