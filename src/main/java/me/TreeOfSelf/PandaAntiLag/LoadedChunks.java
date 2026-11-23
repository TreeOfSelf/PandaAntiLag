package me.TreeOfSelf.PandaAntiLag;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class LoadedChunks {
    private LoadedChunks() {
    }

    // Whether every chunk the box reaches is really there. Vanilla's hasChunk (and touchingUnloadedChunk) only look at
    // the ticket level, so they already say yes for a chunk that was just requested, and reading a block then blocks.
    public static boolean allReady(ServerLevel level, AABB box) {
        int x0 = SectionPos.blockToSectionCoord(Mth.floor(box.minX)), x1 = SectionPos.blockToSectionCoord(Mth.ceil(box.maxX));
        int z0 = SectionPos.blockToSectionCoord(Mth.floor(box.minZ)), z1 = SectionPos.blockToSectionCoord(Mth.ceil(box.maxZ));
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (level.getChunkSource().getChunkNow(x, z) == null) return false;
            }
        }
        return true;
    }
}
