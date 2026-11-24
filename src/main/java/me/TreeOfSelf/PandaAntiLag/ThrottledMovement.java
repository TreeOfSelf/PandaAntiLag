package me.TreeOfSelf.PandaAntiLag;

// Implemented by Mob (via MobMixin). On ticks the throttle skips, mobs that are airborne, knocked back, in water or
// walking a path still get a movement-only tick, so they keep moving at full speed and don't pile up.
// Being jostled by the crowd around them does not count: in a dense pile every mob is always being pushed a little,
// and counting that let every crowded mob escape the throttle (the exact case it exists for).
public interface ThrottledMovement {
    boolean pandaAntiLag$needsMovementTick();

    void pandaAntiLag$setMovementOnly(boolean movementOnly);

    boolean pandaAntiLag$isMovementOnly();

}
