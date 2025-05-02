package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.LagPos;
import me.TreeOfSelf.PandaAntiLag.PandaAntiLag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    private static <T extends ProjectileEntity> void handleProjectileSpawn(T projectile, ServerWorld world) {
        LagPos lagPos = new LagPos(projectile.getChunkPos());
        List<? extends ProjectileEntity> nearbyEntities = world.getEntitiesByType(
                TypeFilter.instanceOf(ProjectileEntity.class),
                entity -> {
                    LagPos thisLagPos = new LagPos(entity.getChunkPos());
                    return Math.abs(thisLagPos.x - lagPos.x) <= AntiLagSettings.regionBuffer &&
                            Math.abs(thisLagPos.z - lagPos.z) <= AntiLagSettings.regionBuffer
                            && entity.getType() == projectile.getType();
                }
        );

        int nearby = nearbyEntities.size();
        
        if (nearby > AntiLagSettings.projectileMax) {
            int over = (nearby - AntiLagSettings.projectileMax);
            for (int index = 0; index <= over; index++) {
                nearbyEntities.getFirst().remove(RemovalReason.KILLED);
            }
        }
    }

    @Inject(method = "spawn(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/ProjectileEntity;", at = @At(value = "TAIL"))
    private static <T extends ProjectileEntity> void spawn(T projectile, ServerWorld world, ItemStack projectileStack, CallbackInfoReturnable<T> cir) {
        handleProjectileSpawn(projectile, world);
    }

    @Inject(method = "spawn(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/entity/projectile/ProjectileEntity;", at = @At(value = "TAIL"))
    private static <T extends ProjectileEntity> void spawn(T projectile, ServerWorld world, ItemStack projectileStack, Consumer<T> beforeSpawn, CallbackInfoReturnable<T> cir) {
        handleProjectileSpawn(projectile, world);
    }
}
