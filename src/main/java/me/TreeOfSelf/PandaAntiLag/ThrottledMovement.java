package me.TreeOfSelf.PandaAntiLag;

// Implemented by Mob (via MobMixin). On ticks the throttle skips, mobs that are airborne, being pushed, in water or
// walking a path still get a movement-only tick, so they keep moving at full speed and don't pile up.
public interface ThrottledMovement {
    boolean pandaAntiLag$needsMovementTick();

    void pandaAntiLag$setMovementOnly(boolean movementOnly);
}
