package me.sebastian420.PandaAntiLag.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.EntityList;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;


import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerLevelMixin {

    @Shadow public abstract TickManager getTickManager();

    @Shadow protected abstract boolean shouldCancelSpawn(Entity entity);

    @Shadow @Final private ServerChunkManager chunkManager;

    @Shadow public abstract void tickEntity(Entity entity);

    // Store the profiler instance
    private Profiler profiler;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onTickStart(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        profiler = ((ServerWorld) (Object) this).getProfiler();
    }


    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V",
                    ordinal = 0
            )
    )
    private <T> void redirectEntityTick(EntityList instance, Consumer<Entity> action) {
        instance.forEach((entity) -> {
            if (!entity.isRemoved()) {
                if (this.shouldCancelSpawn(entity)) {
                    entity.discard();
                } else if (!getTickManager().shouldSkipTick(entity)) {
                    profiler.push("checkDespawn");
                    entity.checkDespawn();
                    profiler.pop();
                    if (this.chunkManager.chunkLoadingManager.getTicketManager().shouldTickEntities(entity.getChunkPos().toLong()) && false) {
                        Entity entity2 = entity.getVehicle();
                        if (entity2 != null) {
                            if (!entity2.isRemoved() && entity2.hasPassenger(entity)) {
                                return;
                            }

                            entity.stopRiding();
                        }

                        profiler.push("tick");
                        this.tickEntity(entity);
                        profiler.pop();
                    }
                }
            }
        });
    }

}