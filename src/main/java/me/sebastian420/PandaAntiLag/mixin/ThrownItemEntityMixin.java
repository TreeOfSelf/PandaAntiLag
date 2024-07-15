package me.sebastian420.PandaAntiLag.mixin;

import me.sebastian420.PandaAntiLag.AntiLagSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ProjectileEntity.class)
public abstract class ThrownItemEntityMixin extends Entity {

    public ThrownItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void init(EntityType entityType, World world, CallbackInfo ci){
        ServerWorld serverWorld = (ServerWorld) world;
        ProjectileEntity projectileEntity = (ProjectileEntity)(Object)this;
        ChunkPos chunkPos = projectileEntity.getChunkPos();
        List<? extends ProjectileEntity> nearbyEntities = serverWorld.getEntitiesByType(TypeFilter.instanceOf(ProjectileEntity.class), entity ->
                Math.abs(entity.getChunkPos().x - chunkPos.x) < AntiLagSettings.regionSize &&
                        Math.abs(entity.getChunkPos().z - chunkPos.z) < AntiLagSettings.regionSize &&
                        entity.getClass() == projectileEntity.getClass());

        int nearby = nearbyEntities.size();
        if (nearby > AntiLagSettings.projectileMax) {
            int over = (int) (nearby - AntiLagSettings.projectileMax);
            for(int index = 0; index <= over; index++){
                nearbyEntities.getFirst().remove(RemovalReason.KILLED);
            }
        }
    }
}

