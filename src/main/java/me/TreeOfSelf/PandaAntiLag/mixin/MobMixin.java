package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.ThrottledMovement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends Entity implements ThrottledMovement {

    @Shadow
    protected PathNavigation navigation;
    @Shadow
    protected MoveControl moveControl;
    @Shadow
    protected JumpControl jumpControl;

    @Unique
    private boolean movementOnly = false;

    public MobMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean pandaAntiLag$needsMovementTick() {
        if (!this.onGround()) return true;
        // Knockback, explosions and other pushes
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) return true;
        if (this.isInWater()) {
            MobCategory category = this.getType().getCategory();
            if (category != MobCategory.WATER_CREATURE && category != MobCategory.WATER_AMBIENT &&
                category != MobCategory.UNDERGROUND_WATER_CREATURE && category != MobCategory.AXOLOTLS) return true;
        }
        return !this.navigation.isDone();
    }

    @Override
    public void pandaAntiLag$setMovementOnly(boolean movementOnly) {
        this.movementOnly = movementOnly;
    }

    // Keep following the current path, but skip sensing, targeting, goals and brains
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void movementOnlyAiStep(CallbackInfo ci) {
        if (!movementOnly) return;
        this.navigation.tick();
        this.moveControl.tick();
        this.jumpControl.tick();
        ci.cancel();
    }

    // Vanilla only despawns a mob when some player in its dimension is far enough away. With no players there
    // at all (chunks kept loaded by portals), nothing ever despawns, so mobs walking through a farm's portals
    // pile up on the other side forever. Treat "no player" the same as "player too far away".
    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void despawnWithoutPlayers(CallbackInfo ci) {
        if (!AntiLagSettings.despawnWithoutPlayers || !this.level().players().isEmpty()) return;
        Mob mob = (Mob) (Object) this;
        if (mob.isPersistenceRequired() || mob.requiresCustomPersistence()) return;
        if (mob.removeWhenFarAway(Double.MAX_VALUE)) {
            this.discard();
            ci.cancel();
        }
    }
}
