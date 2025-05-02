package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.LagPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void init(EntityType entityType, World world, CallbackInfo ci){
        ServerWorld serverWorld = (ServerWorld) world;
        ProjectileEntity projectileEntity = (ProjectileEntity)(Object)this;
        LagPos lagPos = new LagPos(projectileEntity.getChunkPos());
        List<? extends ProjectileEntity> nearbyEntities = serverWorld.getEntitiesByType(
                TypeFilter.instanceOf(ProjectileEntity.class),
                entity -> {
                    LagPos thisLagPos = new LagPos(entity.getChunkPos());
                    return true;
                    /*return Math.abs(thisLagPos.x - lagPos.x) < AntiLagSettings.regionBuffer &&
                            Math.abs(thisLagPos.z - lagPos.z) < AntiLagSettings.regionBuffer &&
                            entity.getClass() == projectileEntity.getClass();*/
                }
        );

        int nearby = nearbyEntities.size();
        if (nearby > AntiLagSettings.projectileMax) {
            int over = (nearby - AntiLagSettings.projectileMax);
            for(int index = 0; index <= over; index++){
                nearbyEntities.getFirst().remove(RemovalReason.KILLED);
            }
        }
    }
}

