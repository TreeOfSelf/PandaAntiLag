package me.sebastian420.PandaAntiLag.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MobEntity.class)
public abstract class MixinLivingEntity extends Entity {


	@Unique
	private long lastCheck;
	@Unique
	private int nearby = 1;

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

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

	@Inject(at = @At("HEAD"), method = "tick", cancellable = true)
	public void tick(CallbackInfo ci){


		if (this.age % this.nearby != 0) {
			ci.cancel();
			return;
		}

		String entityType = getEntityType(this);
		if(entityType=="NULL"){
			return;
		}

		//Check initial nearby values

		if(this.lastCheck==0 || System.currentTimeMillis() - this.lastCheck>0){
			this.lastCheck = System.currentTimeMillis() + 10000 + (this.nearby * 10L);
			ServerWorld world = (ServerWorld) this.getWorld();
			ChunkPos chunkPos = this.getChunkPos();
			this.nearby = world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), entity -> Math.abs(entity.getChunkPos().x - chunkPos.x) < 12 && Math.abs(entity.getChunkPos().z - chunkPos.z) < 12 && entityType == getEntityType(entity)).size();
			if(this.nearby > 75) {
				float tickTimes = this.getServer().getAverageTickTime();
				this.nearby = (int) (this.nearby / 200 + tickTimes/10);
			}else{
				this.nearby = 1;
			}

			if(this.nearby<1) this.nearby=1;

		}
	}

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		if (this.age % this.nearby == 0) {
			super.move(movementType,movement);
		}
	}






}