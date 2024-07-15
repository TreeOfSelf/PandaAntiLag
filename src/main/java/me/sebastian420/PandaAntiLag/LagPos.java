package me.sebastian420.PandaAntiLag;

import net.minecraft.util.math.ChunkPos;

public class LagPos {
    public int x;
    public int z;

    public LagPos(ChunkPos chunkPos) {
        this.x = (int) Math.floor(chunkPos.getStartX() / AntiLagSettings.regionSize);
        this.z = (int) Math.floor(chunkPos.getStartZ() / AntiLagSettings.regionSize);
    }
}