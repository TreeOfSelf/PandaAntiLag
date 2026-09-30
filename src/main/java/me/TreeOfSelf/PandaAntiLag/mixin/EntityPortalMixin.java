package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.PortalOwnership;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Caps how many portal-loaded areas one player can be responsible for (see PortalOwnership).
// Over the cap the entity still goes through, but no portal ticket keeps the destination loaded.
@Mixin(Entity.class)
public class EntityPortalMixin {

    @Inject(method = "teleport", at = @At("HEAD"))
    private void startTeleport(TeleportTransition transition, CallbackInfoReturnable<Entity> cir) {
        Entity self = (Entity) (Object) this;
        if (self.level() instanceof ServerLevel level && !(self instanceof Player)) {
            PortalOwnership.startTeleport(level, self.chunkPosition());
        } else {
            PortalOwnership.startTeleport(null, null);
        }
    }

    @Inject(method = "teleport", at = @At("RETURN"))
    private void endTeleport(TeleportTransition transition, CallbackInfoReturnable<Entity> cir) {
        PortalOwnership.endTeleport();
    }

    @Inject(method = "placePortalTicket", at = @At("HEAD"), cancellable = true)
    private void capPortalTicket(BlockPos ticketPosition, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level() instanceof ServerLevel level && !PortalOwnership.allowTicket(level, ChunkPos.containing(ticketPosition))) {
            ci.cancel();
        }
    }
}
