package me.sebastian420.PandaAntiLag;

import net.minecraft.util.math.ChunkPos;

public class LagPos {
    public final int x;
    public final int z;

    public LagPos(ChunkPos chunkPos) {
        this.x = (int) Math.floor(chunkPos.x / AntiLagSettings.regionSize);
        this.z = (int) Math.floor(chunkPos.z / AntiLagSettings.regionSize);
    }

    public int hashCode() {
        return 31 * x + z;
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