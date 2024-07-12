package me.sebastian420.PandaAntiLag.mixin;

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

@Mixin(ProjectileEntity.class)
public abstract class ThrownItemEntityMixin extends Entity {

    private long lastCheck;
    private int nearby;

    public ThrownItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    //Check enttiy type of villager
    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci){
        //Check initial nearby values
        if(this.lastCheck==0 || System.currentTimeMillis() - this.lastCheck>0){
            this.lastCheck = System.currentTimeMillis() + 10000;
            ServerWorld world = (ServerWorld) this.getWorld();

            ChunkPos chunkPos = this.getChunkPos();
            this.nearby = world.getEntitiesByType(TypeFilter.instanceOf(ProjectileEntity.class), entity -> Math.abs(entity.getChunkPos().x - chunkPos.x) < 4 && Math.abs(entity.getChunkPos().z - chunkPos.z) < 4 ).size();
            if(this.nearby > 75) {
                float tickTimes = this.getServer().getAverageTickTime();
                this.nearby = (int) (this.nearby / 200 + tickTimes/10);
            }else{
                this.nearby = 1;
            }

            if(this.nearby<1) this.nearby=1;

        }

        if (this.age % this.nearby != 0) {
            ci.cancel();
        }
    }



}
