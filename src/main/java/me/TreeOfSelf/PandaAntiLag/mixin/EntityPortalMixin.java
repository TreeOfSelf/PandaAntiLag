package me.TreeOfSelf.PandaAntiLag.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.TreeOfSelf.PandaAntiLag.PortalOwnership;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Caps how many portal-loaded areas one player can be responsible for (see PortalOwnership).
// Every portal ticket in vanilla goes through placePortalTicket; over the cap the entity still
// goes through, but no ticket keeps the destination loaded.
@Mixin(Entity.class)
public class EntityPortalMixin {

    // Remember where the entity is teleporting from, for the ticket placed during the teleport
    @WrapMethod(method = "teleport")
    private Entity trackTeleportSource(TeleportTransition transition, Operation<Entity> original) {
        PortalOwnership.startTeleport((Entity) (Object) this);
        try {
            return original.call(transition);
        } finally {
            PortalOwnership.endTeleport();
        }
    }

    @Inject(method = "placePortalTicket", at = @At("HEAD"), cancellable = true)
    private void capPortalTicket(BlockPos ticketPosition, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level() instanceof ServerLevel level && !PortalOwnership.allowTicket(level, ChunkPos.containing(ticketPosition))) {
            ci.cancel();
        }
    }
}
