package me.TreeOfSelf.PandaAntiLag;

import net.minecraft.util.math.ChunkPos;
import java.util.HashMap;
import java.util.Map;

public class LagPos {
    public final int x;
    public final int z;

    private static final Map<Long, LagPos> CACHE = new HashMap<>();

    public static LagPos fromChunkPos(ChunkPos chunkPos) {
        int x = chunkPos.x >> AntiLagSettings.regionSizeBits;
        int z = chunkPos.z >> AntiLagSettings.regionSizeBits;
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
        return CACHE.computeIfAbsent(key, k -> new LagPos(x, z));
    }

    private LagPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public LagPos(ChunkPos chunkPos) {
        this.x = chunkPos.x >> AntiLagSettings.regionSizeBits;
        this.z = chunkPos.z >> AntiLagSettings.regionSizeBits;
    }

    public int hashCode() {
        return (x << 16) ^ z;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LagPos lagPos)) {
            return false;
        } else {
            return this.x == lagPos.x && this.z == lagPos.z;
        }
    }
}