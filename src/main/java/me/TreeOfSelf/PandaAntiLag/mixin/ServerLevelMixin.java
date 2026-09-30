package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.ChunkEntityData;
import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.LagPos;
import me.TreeOfSelf.PandaAntiLag.RegionMobCounts;
import me.TreeOfSelf.PandaAntiLag.mixin.accessor.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements RegionMobCounts {

    @Unique
    private double tickCount = 0;

    @Shadow
    public abstract TickRateManager tickRateManager();

    @Shadow
    public abstract void tickNonPassenger(Entity entity);

    @Shadow
    public abstract net.minecraft.server.level.ServerChunkCache getChunkSource();

    @Unique
    private final HashMap<LagPos, ChunkEntityData> chunkEntityDataMap = new HashMap<>();
    @Unique
    private ProfilerFiller profiler;
    @Unique
    private final Map<EntityType<?>, Integer> entityTypeCache = new HashMap<>();

    @Unique
    public int getEntityType(Entity entity) {
        if (entity instanceof VehicleEntity) return ChunkEntityData.VEHICLE_TYPE;
        if (!(entity instanceof LivingEntity)) return ChunkEntityData.NULL_TYPE;
        if (entity instanceof EnderDragon) return ChunkEntityData.NULL_TYPE;
        if (entity instanceof Villager) return ChunkEntityData.PEACEFUL_TYPE;
        if (entity instanceof WanderingTrader) return ChunkEntityData.PEACEFUL_TYPE;

        return entityTypeCache.computeIfAbsent(entity.getType(), type -> {
            MobCategory category = type.getCategory();
            if (category == MobCategory.MONSTER) return ChunkEntityData.MONSTER_TYPE;
            if (category == MobCategory.CREATURE || category == MobCategory.AXOLOTLS ||
                category == MobCategory.UNDERGROUND_WATER_CREATURE || category == MobCategory.AMBIENT ||
                category == MobCategory.WATER_AMBIENT || category == MobCategory.MISC ||
                category == MobCategory.WATER_CREATURE) return ChunkEntityData.PEACEFUL_TYPE;
            return ChunkEntityData.NULL_TYPE;
        });
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        profiler = Profiler.get();
    }

    @Unique
    private long regionCountsTick = -1;
    @Unique
    private final HashMap<LagPos, int[]> regionCounts = new HashMap<>();
    @Unique
    private long lastPrune = 0;

    // Count entities per region in one pass, at most once per tick, instead of scanning every entity for every region
    @Unique
    private void refreshRegionCounts(ServerLevel serverLevel) {
        if (regionCountsTick == (long) tickCount) return;
        regionCountsTick = (long) tickCount;
        regionCounts.clear();
        for (Entity foundEntity : serverLevel.getAllEntities()) {
            int entityType = getEntityType(foundEntity);
            if (entityType != ChunkEntityData.NULL_TYPE) {
                regionCounts.computeIfAbsent(LagPos.fromChunkPos(foundEntity.chunkPosition()), k -> new int[4])[entityType]++;
            }
        }
    }

    @Override
    public int pandaAntiLag$countNearby(ChunkPos chunkPos, int entityType) {
        refreshRegionCounts((ServerLevel) (Object) this);
        LagPos lagPos = LagPos.fromChunkPos(chunkPos);
        int buffer = AntiLagSettings.regionBuffer - 1;
        int count = 0;
        for (int dx = -buffer; dx <= buffer; dx++) {
            for (int dz = -buffer; dz <= buffer; dz++) {
                int[] regionCount = regionCounts.get(LagPos.of(lagPos.x + dx, lagPos.z + dz));
                if (regionCount != null) count += regionCount[entityType];
            }
        }
        return count;
    }

    @Unique
    public void updateEntityCounts(ChunkEntityData chunkEntityData, ServerLevel serverLevel, LagPos lagPos) {
        refreshRegionCounts(serverLevel);
        int[] counts = new int[4];
        int buffer = AntiLagSettings.regionBuffer - 1;
        for (int dx = -buffer; dx <= buffer; dx++) {
            for (int dz = -buffer; dz <= buffer; dz++) {
                int[] regionCount = regionCounts.get(LagPos.of(lagPos.x + dx, lagPos.z + dz));
                if (regionCount == null) continue;
                for (int type = 1; type < 4; type++) counts[type] += regionCount[type];
            }
        }

        float tickTimes = serverLevel.getServer().getCurrentSmoothedTickTime();
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
                entityCount = (int) ((float) entityCount / staggerLenience + tickTimes / AntiLagSettings.tickTimeLenience);
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
                    target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V",
                    ordinal = 0
            )
    )
    private void redirectEntityTick(EntityTickList instance, Consumer<Entity> action) {
        tickCount++;
        ServerLevel serverLevel = (ServerLevel) (Object) this;
        long currentTime = System.currentTimeMillis();
        TickRateManager tickRateManager = this.tickRateManager();

        if (currentTime - lastPrune > AntiLagSettings.updateInterval) {
            lastPrune = currentTime;
            // Drop regions nothing has ticked in for a while so the map does not grow forever as players explore
            chunkEntityDataMap.values().removeIf(data -> data.lastCheck < currentTime - AntiLagSettings.updateInterval * 2);
        }

        instance.forEach((entity) -> {
            LagPos lagPos = LagPos.fromChunkPos(entity.chunkPosition());
            ChunkEntityData chunkEntityData = chunkEntityDataMap.computeIfAbsent(lagPos, k -> new ChunkEntityData());
            if (chunkEntityData.lastCheck == 0 || currentTime > chunkEntityData.lastCheck) {
                chunkEntityData.lastCheck = currentTime + AntiLagSettings.updateInterval;
                updateEntityCounts(chunkEntityData, serverLevel, lagPos);
            }

            boolean skip = (tickCount + entity.getId()) % chunkEntityData.getNearbyCount(getEntityType(entity)) != 0;

            if (!entity.isRemoved() && (!skip || entity.getType() == EntityTypes.PLAYER || entity.hasControllingPassenger())) {
                if (!tickRateManager.isEntityFrozen(entity)) {
                    profiler.push("checkDespawn");
                    entity.checkDespawn();
                    profiler.pop();
                    if (entity instanceof ServerPlayer || getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(entity.chunkPosition().pack())) {
                        Entity entity2 = entity.getVehicle();
                        if (entity2 != null) {
                            if (!entity2.isRemoved() && entity2.hasPassenger(entity)) {
                                return;
                            }
                            entity.stopRiding();
                        }

                        profiler.push("tick");
                        serverLevel.guardEntityTick(this::tickNonPassenger, entity);
                        profiler.pop();
                    }
                }
            } else if (skip) {
                entity.tickCount++;

                if (entity instanceof AgeableMob passiveEntity) {
                    int i = passiveEntity.getAge();
                    if (i < 0) {
                        ++i;
                        passiveEntity.setAge(i);
                    } else if (i > 0) {
                        --i;
                        passiveEntity.setAge(i);
                    }
                }

                switch (entity) {
                    case Sheep sheepEntity -> {
                        EatBlockGoal eatBlockGoal = ((SheepEntityAccessor) sheepEntity).getEatBlockGoal();
                        if (eatBlockGoal != null) {
                            if (eatBlockGoal.canContinueToUse()) {
                                eatBlockGoal.tick();
                            } else if (eatBlockGoal.canUse()) {
                                eatBlockGoal.start();
                            }
                        }
                    }
                    case Armadillo armadilloEntity -> {
                        int cooldown = ((ArmadilloEntityAccessor) armadilloEntity).getScuteTime();
                        if (cooldown > 0) {
                            ((ArmadilloEntityAccessor) armadilloEntity).setScuteTime(cooldown - 1);
                        }
                    }
                    case Chicken chickenEntity -> chickenEntity.eggTime--;
                    case Tadpole tadpoleEntity -> {
                        int tadPoleAge = ((TadpoleEntityAccessor) tadpoleEntity).getAge();
                        ((TadpoleEntityAccessor) tadpoleEntity).setAge(tadPoleAge + 1);
                    }
                    case Turtle turtleEntity -> {
                        if (turtleEntity.isLayingEgg()) {
                            int layEggCounter = ((TurtleEntityAccessor) turtleEntity).getLayEggCounter();
                            ((TurtleEntityAccessor) turtleEntity).setLayEggCounter(layEggCounter + 1);
                        }
                    }
                    case Bee beeEntity -> {
                        if (beeEntity.hasStung()) {
                            int ticksSinceSting = ((BeeEntityAccessor) beeEntity).getTimeSinceSting();
                            ((BeeEntityAccessor) beeEntity).setTimeSinceSting(ticksSinceSting + 1);
                        }
                        if (!beeEntity.hasNectar()) {
                            int ticksSincePollination = ((BeeEntityAccessor) beeEntity).getTicksWithoutNectarSinceExitingHive();
                            ((BeeEntityAccessor) beeEntity).setTicksWithoutNectarSinceExitingHive(ticksSincePollination + 1);
                        }
                        int cannotEnterHiveTicks = ((BeeEntityAccessor) beeEntity).getStayOutOfHiveCountdown();
                        if (cannotEnterHiveTicks > 0) {
                            ((BeeEntityAccessor) beeEntity).setStayOutOfHiveCountdown(cannotEnterHiveTicks - 1);
                        }
                        int ticksLeftToFindHive = ((BeeEntityAccessor) beeEntity).getRemainingCooldownBeforeLocatingNewHive();
                        if (ticksLeftToFindHive > 0) {
                            ((BeeEntityAccessor) beeEntity).setRemainingCooldownBeforeLocatingNewHive(ticksLeftToFindHive - 1);
                        }
                        int ticksUntilCanPollinate = ((BeeEntityAccessor) beeEntity).getRemainingCooldownBeforeLocatingNewFlower();
                        if (ticksUntilCanPollinate > 0) {
                            ((BeeEntityAccessor) beeEntity).setRemainingCooldownBeforeLocatingNewFlower(ticksUntilCanPollinate - 1);
                        }
                    }
                    default -> {
                    }
                }
            }

        });
    }
}
