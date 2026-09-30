package me.TreeOfSelf.PandaAntiLag.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.TreeOfSelf.PandaAntiLag.PortalOwnership;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;

// Pearls place their portal ticket after Entity.teleport returns, so track their source here too
@Mixin(ThrownEnderpearl.class)
public class ThrownEnderpearlPortalMixin {

    @WrapMethod(method = "teleport")
    private Entity trackTeleportSource(TeleportTransition transition, Operation<Entity> original) {
        PortalOwnership.startTeleport((Entity) (Object) this);
        try {
            return original.call(transition);
        } finally {
            PortalOwnership.endTeleport();
        }
    }
}
