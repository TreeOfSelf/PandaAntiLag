package me.TreeOfSelf.PandaAntiLag;

import net.minecraft.world.level.ChunkPos;

// Implemented by ServerLevel (via ServerLevelMixin) to expose the per-region mob counts
public interface RegionMobCounts {
    int pandaAntiLag$countNearby(ChunkPos chunkPos, int entityType);
}
