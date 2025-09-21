package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.ChunkEntityData;
import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.LagPos;
import me.TreeOfSelf.PandaAntiLag.mixin.accessor.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.EntityList;
import net.minecraft.world.tick.TickManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerLevelMixin {

    @Unique
    private double tickCount = 0;

    @Shadow public abstract TickManager getTickManager();

    @Shadow public abstract void tickEntity(Entity entity);

    @Shadow public abstract boolean shouldTickChunkAt(ChunkPos pos);

    @Shadow @Final private ServerChunkManager chunkManager;
    @Shadow @Final private static Logger LOGGER;
    @Unique
    private final HashMap<LagPos, ChunkEntityData> chunkEntityDataMap = new HashMap<>();
    @Unique
    private Profiler profiler;
    @Unique
    private final Map<EntityType<?>, Integer> entityTypeCache = new HashMap<>();

    @Unique  
    public int getEntityType(Entity entity) {
        if (entity instanceof VehicleEntity) return ChunkEntityData.VEHICLE_TYPE;
        if (!(entity instanceof LivingEntity)) return ChunkEntityData.NULL_TYPE;
        if (entity instanceof EnderDragonEntity) return ChunkEntityData.NULL_TYPE;
        if (entity instanceof VillagerEntity) return ChunkEntityData.PEACEFUL_TYPE;
        if (entity instanceof WanderingTraderEntity) return ChunkEntityData.PEACEFUL_TYPE;

        return entityTypeCache.computeIfAbsent(entity.getType(), type -> {
            SpawnGroup group = type.getSpawnGroup();
            if (group == SpawnGroup.MONSTER) return ChunkEntityData.MONSTER_TYPE;
            if (group == SpawnGroup.CREATURE || group == SpawnGroup.AXOLOTLS || 
                group == SpawnGroup.UNDERGROUND_WATER_CREATURE || group == SpawnGroup.AMBIENT ||
                group == SpawnGroup.WATER_AMBIENT || group == SpawnGroup.MISC ||
                group == SpawnGroup.WATER_CREATURE) return ChunkEntityData.PEACEFUL_TYPE;
            return ChunkEntityData.NULL_TYPE;
        });
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onTickStart(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        profiler =  Profilers.get();
    }
    @Unique
    public void updateEntityCounts(ChunkEntityData chunkEntityData, ServerWorld serverWorld, LagPos lagPos) {
        int[] counts = new int[4];
        
        serverWorld.getEntitiesByType(
            TypeFilter.instanceOf(Entity.class),
            foundEntity -> {
                LagPos entityLagPos = LagPos.fromChunkPos(foundEntity.getChunkPos());
                if (Math.abs(entityLagPos.x - lagPos.x) < AntiLagSettings.regionBuffer &&
                    Math.abs(entityLagPos.z - lagPos.z) < AntiLagSettings.regionBuffer) {
                    int entityType = getEntityType(foundEntity);
                    if (entityType != ChunkEntityData.NULL_TYPE) {
                        counts[entityType]++;
                    }
                }
                return false;
            }
        );

        float tickTimes = serverWorld.getServer().getAverageTickTime();
        for (int type = 1; type < 4; type++) {
            int entityCount = counts[type];
            int minimumRegion, staggerLenience;
            
            if (type == ChunkEntityData.VEHICLE_TYPE) {
                minimumRegion = AntiLagSettings.minimumRegionVehicle;
                staggerLenience = AntiLagSettings.vehicleStaggerLenience;
            } else {
                minimumRegion = AntiLagSettings.minimumRegionMobs;
                staggerLenience = AntiLagSettings.mobStaggerLenience;
            }
            
            if (entityCount > minimumRegion) {
                entityCount = (int) ((float) entityCount / staggerLenience + tickTimes/AntiLagSettings.tickTimeLenience);
                if (entityCount <= 0) entityCount = 1;
            } else {
                entityCount = 1;
            }
            chunkEntityData.setNearbyCount(type, entityCount);
        }
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
        tickCount++;
        ServerWorld serverWorld = (ServerWorld)(Object)this;
        long currentTime = System.currentTimeMillis();

        instance.forEach((entity) -> {
            LagPos lagPos = LagPos.fromChunkPos(entity.getChunkPos());
            ChunkEntityData chunkEntityData = chunkEntityDataMap.computeIfAbsent(lagPos, k -> new ChunkEntityData());
            if(chunkEntityData.lastCheck==0 || currentTime > chunkEntityData.lastCheck) {
                chunkEntityData.lastCheck = currentTime + AntiLagSettings.updateInterval;
                updateEntityCounts(chunkEntityData, serverWorld, lagPos);
            }

            boolean skip = (tickCount + entity.getId()) % chunkEntityData.getNearbyCount(getEntityType(entity)) != 0;

            if (!entity.isRemoved() && (!skip || entity.getType() == EntityType.PLAYER || entity.hasControllingPassenger())) {
                if (!getTickManager().shouldSkipTick(entity)) {
                    profiler.push("checkDespawn");
                    entity.checkDespawn();
                    profiler.pop();
                    if (entity instanceof ServerPlayerEntity || chunkManager.chunkLoadingManager.getLevelManager().shouldTickEntities(entity.getChunkPos().toLong())) {
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
            } else if (skip) {
                entity.age++;

                if (entity instanceof PassiveEntity passiveEntity) {
                    int i = passiveEntity.getBreedingAge();
                    if (i < 0) {
                        ++i;
                        passiveEntity.setBreedingAge(i);
                    } else if (i > 0) {
                        --i;
                        passiveEntity.setBreedingAge(i);
                    }
                }

                switch (entity) {
                    case SheepEntity sheepEntity -> {
                        EatGrassGoal eatGrassGoal = ((SheepEntityAccessor) sheepEntity).getEatGrassGoal();
                        if (eatGrassGoal != null) {
                            if (eatGrassGoal.shouldContinue()) {
                                eatGrassGoal.tick();
                            } else if (eatGrassGoal.canStart()) {
                                eatGrassGoal.start();
                            }
                        }
                    }
                    case ArmadilloEntity armadilloEntity -> {
                        int cooldown = ((ArmadilloEntityAccessor) armadilloEntity).getNextScuteShedCooldown();
                        ((ArmadilloEntityAccessor) armadilloEntity).setNextScuteShedCooldown(cooldown - 1);
                    }
                    case ChickenEntity chickenEntity -> chickenEntity.eggLayTime--;
                    case TadpoleEntity tadpoleEntity -> {
                        int tadPoleAge = ((TadpoleEntityAccessor) tadpoleEntity).getTadpoleAge();
                        ((TadpoleEntityAccessor) tadpoleEntity).setTadpoleAge(tadPoleAge + 1);
                    }
                    case TurtleEntity turtleEntity -> {
                        if (turtleEntity.isDiggingSand()) {
                            int sandDiggingCounter = ((TurtleEntityAccessor) turtleEntity).getSandDiggingCounter();
                            ((TurtleEntityAccessor) turtleEntity).setSandDiggingCounter(sandDiggingCounter + 1);
                        }
                    }
                    case VillagerEntity villagerEntity -> {
                        if (!villagerEntity.hasCustomer()) {
                            int levelUpTimer = ((VillagerEntityAccessor) villagerEntity).getLevelUpTimer();
                            if (levelUpTimer > 0) {
                                ((VillagerEntityAccessor) villagerEntity).setLevelUpTimer(levelUpTimer - 1);
                                if (levelUpTimer - 1 <= 0) {
                                    if (((VillagerEntityAccessor) villagerEntity).isLevelingUp()) {
                                        ((VillagerEntityAccessor) villagerEntity).setLevelingUp(false);
                                    }
                                }
                            }
                        }
                    }
                    case BeeEntity beeEntity -> {
                        if (beeEntity.hasStung()) {
                            int ticksSinceSting = ((BeeEntityAccessor) beeEntity).getTicksSinceSting();
                            ((BeeEntityAccessor) beeEntity).setTicksSinceSting(ticksSinceSting + 1);
                        }
                        if (!beeEntity.hasNectar()) {
                            int ticksSincePollination = ((BeeEntityAccessor) beeEntity).getTicksSincePollination();
                            ((BeeEntityAccessor) beeEntity).setTicksSincePollination(ticksSincePollination + 1);
                        }
                        int cannotEnterHiveTicks = ((BeeEntityAccessor) beeEntity).getCannotEnterHiveTicks();
                        if (cannotEnterHiveTicks > 0) {
                            ((BeeEntityAccessor) beeEntity).setCannotEnterHiveTicks(cannotEnterHiveTicks - 1);
                        }
                        int ticksLeftToFindHive = ((BeeEntityAccessor) beeEntity).getTicksLeftToFindHive();
                        if (ticksLeftToFindHive > 0) {
                            ((BeeEntityAccessor) beeEntity).setTicksLeftToFindHive(ticksLeftToFindHive - 1);
                        }
                        int ticksUntilCanPollinate = ((BeeEntityAccessor) beeEntity).getTicksUntilCanPollinate();
                        if (ticksUntilCanPollinate > 0) {
                            ((BeeEntityAccessor) beeEntity).setTicksUntilCanPollinate(ticksUntilCanPollinate - 1);
                        }
                    }
                    default -> {}
                }
            }

        });
    }
}