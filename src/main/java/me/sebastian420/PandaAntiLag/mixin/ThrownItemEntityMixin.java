package me.sebastian420.PandaAntiLag.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ProjectileEntity.class)
public abstract class ThrownItemEntityMixin extends Entity {
    public ThrownItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
        ServerWorld serverWorld = (ServerWorld) world;
        ProjectileEntity projectileEntity = (ProjectileEntity)(Object)this;
        ChunkPos chunkPos = projectileEntity.getChunkPos();
        List<? extends ProjectileEntity> nearbyEntities = serverWorld.getEntitiesByType(TypeFilter.instanceOf(ProjectileEntity.class), entity -> Math.abs(entity.getChunkPos().x - chunkPos.x) < 12 && Math.abs(entity.getChunkPos().z - chunkPos.z) < 12 && entity.getClass() == projectileEntity.getClass());
        int nearby = nearbyEntities.size();
        if (nearby > 150) {
            int over = nearby - 150;
            for(int index = 0; index <= over; index++){
                nearbyEntities.getFirst().remove(RemovalReason.KILLED);
            }
        }
    }
}

