package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.ChunkEntityData;
import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.LagPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EntityList;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerLevelMixin {
    //TODO REDO THIS WHOLE THING ITS BROKEN REEE
    /*@Shadow public abstract TickManager getTickManager();


    @Shadow @Final private ServerChunkManager chunkManager;

    @Shadow public abstract void tickEntity(Entity entity);

    @Shadow public abstract ServerChunkManager getChunkManager();

    @Shadow public abstract boolean shouldTickChunkAt(ChunkPos pos);

    @Unique
    private final HashMap<LagPos, ChunkEntityData> chunkEntityDataMap = new HashMap<>();


    @Unique
    public String getEntityType(Entity entity){
        if(entity instanceof EnderDragonEntity) return "NULL";
        if(entity.getType().getSpawnGroup() == SpawnGroup.MONSTER){
            return "MONSTER";
        }
        if(entity.getType().getSpawnGroup() == SpawnGroup.CREATURE ||
                entity.getType().getSpawnGroup() == SpawnGroup.AXOLOTLS ||
                entity.getType().getSpawnGroup() == SpawnGroup.UNDERGROUND_WATER_CREATURE ||
                entity.getType().getSpawnGroup() == SpawnGroup.AMBIENT ||
                entity.getType().getSpawnGroup() == SpawnGroup.WATER_AMBIENT ||
                entity.getType().getSpawnGroup() == SpawnGroup.MISC ||
                entity.getType().getSpawnGroup() == SpawnGroup.WATER_CREATURE){
            return "PEACEFUL";
        }
        if(entity instanceof VillagerEntity) return "PEACEFUL";
        return "NULL";
    }



    @Unique
    public void CheckCount(ChunkEntityData chunkEntityData, ServerWorld serverWorld, LagPos lagPos, String type){
        int mobCount = serverWorld.getEntitiesByType(
                TypeFilter.instanceOf(LivingEntity.class),
                foundEntity -> {
                    LagPos entityLagPos = new LagPos(foundEntity.getChunkPos());
                    return Math.abs(entityLagPos.x - lagPos.x) < AntiLagSettings.regionBuffer &&
                            Math.abs(entityLagPos.z - lagPos.z) < AntiLagSettings.regionBuffer &&
                            type.equals(getEntityType(foundEntity));
                }
        ).size();

        if (mobCount > AntiLagSettings.minimumRegionMobs) {
            float tickTimes = serverWorld.getServer().getAverageTickTime();
            mobCount = (int) (mobCount / AntiLagSettings.mobStaggerLenience + tickTimes/AntiLagSettings.tickTimeLenience);
            if (mobCount <= 0) mobCount = 1;
            chunkEntityData.setNearbyCount(type, mobCount);
        } else chunkEntityData.setNearbyCount(type, 1);
    }


    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V",
                    ordinal = 0
            )
    )
    private void redirectEntityTick(EntityList instance, Consumer<Entity> action) {
        ServerWorld serverWorld = (ServerWorld)(Object)this;
        long currentTime = System.currentTimeMillis();
        instance.forEach((entity) -> {
            if (!entity.isRemoved()) {
                LagPos lagPos = new LagPos(entity.getChunkPos());
                ChunkEntityData chunkEntityData = chunkEntityDataMap.computeIfAbsent(lagPos, k -> new ChunkEntityData());
                if(chunkEntityData.lastCheck == 0 || currentTime - chunkEntityData.lastCheck > 0) {
                    CheckCount(chunkEntityData, serverWorld, lagPos, "PEACEFUL");
                    CheckCount(chunkEntityData, serverWorld, lagPos, "MONSTER");
                    chunkEntityData.lastCheck = System.currentTimeMillis() + AntiLagSettings.updateInterval;
                }

                boolean skip = entity.age % chunkEntityData.getNearbyCount(getEntityType(entity)) != 0;

                if (!this.getTickManager().shouldSkipTick(entity)) {
                    entity.checkDespawn();

                    if ((!skip || entity.getType() == EntityType.PLAYER) &&
                            (entity instanceof ServerPlayerEntity || this.chunkManager.chunkLoadingManager.getLevelManager().shouldTickEntities(entity.getChunkPos().toLong()))) {
                        Entity entity2 = entity.getVehicle();
                        if (entity2 != null) {
                            if (!entity2.isRemoved() && entity2.hasPassenger(entity)) {
                                return;
                            }

                            entity.stopRiding();
                        }

                        this.tickEntity(entity);
                    }
                }
            }
        });
    }*/
}