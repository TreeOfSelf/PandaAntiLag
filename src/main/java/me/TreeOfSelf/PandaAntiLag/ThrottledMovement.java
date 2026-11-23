package me.TreeOfSelf.PandaAntiLag;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

// Implemented by Mob (via MobMixin). On ticks the throttle skips, mobs that are airborne, knocked back, in water or
// walking a path still get a movement-only tick, so they keep moving at full speed and don't pile up.
// Being jostled by the crowd around them does not count: in a dense pile every mob is always being pushed a little,
// and counting that let every crowded mob escape the throttle (the exact case it exists for).
public interface ThrottledMovement {
    boolean pandaAntiLag$needsMovementTick();

    void pandaAntiLag$setMovementOnly(boolean movementOnly);

    boolean pandaAntiLag$isMovementOnly();

    // POIs this mob could not path to -> game time until which its POI searches leave them out
    Long2LongOpenHashMap pandaAntiLag$poiBackoffUntil();
}
