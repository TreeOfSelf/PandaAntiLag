package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.LagPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(Projectile.class)
public abstract class ProjectileEntityMixin extends Entity {

    @Unique
    private static final Map<LagPos, Map<EntityType<?>, Integer>> projectileCounts = new HashMap<>();
    @Unique
    private static long lastProjectileCheck = 0;

    public ProjectileEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    private static <T extends Projectile> void handleProjectileSpawn(T projectile, ServerLevel world) {
        LagPos lagPos = LagPos.fromChunkPos(projectile.chunkPosition());
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastProjectileCheck > 1000) {
            projectileCounts.clear();
            lastProjectileCheck = currentTime;
        }

        Map<EntityType<?>, Integer> regionCounts = projectileCounts.computeIfAbsent(lagPos, k -> new HashMap<>());
        EntityType<?> projectileType = projectile.getType();

        int count = regionCounts.computeIfAbsent(projectileType, type ->
            world.getEntities(EntityTypeTest.forClass(Projectile.class), entity -> {
                    LagPos thisLagPos = LagPos.fromChunkPos(entity.chunkPosition());
                    return Math.abs(thisLagPos.x - lagPos.x) <= AntiLagSettings.regionBuffer &&
                           Math.abs(thisLagPos.z - lagPos.z) <= AntiLagSettings.regionBuffer &&
                           entity.getType() == type;
                }
            ).size()
        );

        if (count >= AntiLagSettings.projectileMax) {
            List<? extends Projectile> nearbyEntities = world.getEntities(EntityTypeTest.forClass(Projectile.class), entity -> {
                LagPos thisLagPos = LagPos.fromChunkPos(entity.chunkPosition());
                return Math.abs(thisLagPos.x - lagPos.x) <= AntiLagSettings.regionBuffer &&
                       Math.abs(thisLagPos.z - lagPos.z) <= AntiLagSettings.regionBuffer &&
                       entity.getType() == projectileType;
            });

            int over = nearbyEntities.size() - AntiLagSettings.projectileMax;
            for (int index = 0; index <= over; index++) {
                nearbyEntities.getFirst().remove(RemovalReason.KILLED);
            }
            regionCounts.put(projectileType, AntiLagSettings.projectileMax);
        } else {
            regionCounts.put(projectileType, count + 1);
        }
    }

    @Inject(method = "spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/Projectile;", at = @At("TAIL"))
    private static <T extends Projectile> void spawn(T projectile, ServerLevel world, ItemStack projectileStack, CallbackInfoReturnable<T> cir) {
        handleProjectileSpawn(projectile, world);
    }

    @Inject(method = "spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;", at = @At("TAIL"))
    private static <T extends Projectile> void spawn(T projectile, ServerLevel world, ItemStack projectileStack, Consumer<T> beforeSpawn, CallbackInfoReturnable<T> cir) {
        handleProjectileSpawn(projectile, world);
    }
}
