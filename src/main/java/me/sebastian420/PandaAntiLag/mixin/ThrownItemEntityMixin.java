package me.sebastian420.PandaAntiLag.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ProjectileEntity.class)
public abstract class ThrownItemEntityMixin extends Entity {

    @Unique
    private long lastCheck;
    @Unique
    private int nearby = 1;

    public ThrownItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {

        if (this.lastCheck == 0 || System.currentTimeMillis() - this.lastCheck > 0) {
            this.lastCheck = System.currentTimeMillis() + 10000 + (this.nearby * 10L);
            ServerWorld world = (ServerWorld) this.getWorld();
            ProjectileEntity projectileEntity = (ProjectileEntity)(Object)this;
            ChunkPos chunkPos = this.getChunkPos();
            List<? extends ProjectileEntity> nearbyEntities = world.getEntitiesByType(TypeFilter.instanceOf(ProjectileEntity.class), entity -> Math.abs(entity.getChunkPos().x - chunkPos.x) < 12 && Math.abs(entity.getChunkPos().z - chunkPos.z) < 12 && entity.getClass() == projectileEntity.getClass());
            this.nearby = nearbyEntities.size();
            if (this.nearby > 150) {
                int over = this.nearby - 150;
                for(int index = 0; index <= over; index++){
                    nearbyEntities.getFirst().remove(RemovalReason.KILLED);
                }
            }
        }

    }

}

