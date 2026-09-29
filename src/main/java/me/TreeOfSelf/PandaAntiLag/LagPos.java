package me.TreeOfSelf.PandaAntiLag;

import net.minecraft.world.level.ChunkPos;

public class LagPos {
    public final int x;
    public final int z;

    public static LagPos fromChunkPos(ChunkPos chunkPos) {
        int x = chunkPos.x() >> AntiLagSettings.regionSizeBits;
        int z = chunkPos.z() >> AntiLagSettings.regionSizeBits;
        return new LagPos(x, z);
    }

    public static LagPos of(int x, int z) {
        return new LagPos(x, z);
    }

    private LagPos(int x, int z) {
        this.x = x;
        this.z = z;
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
