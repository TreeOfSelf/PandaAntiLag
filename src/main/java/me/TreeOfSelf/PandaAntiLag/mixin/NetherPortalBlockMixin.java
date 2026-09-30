package me.TreeOfSelf.PandaAntiLag.mixin;

import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.ChunkEntityData;
import me.TreeOfSelf.PandaAntiLag.RegionMobCounts;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Portals stop spawning zombified piglins once the area already has too many monsters.
// Only spawning is blocked; mobs can still travel through portals normally.
@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void capPortalSpawns(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (AntiLagSettings.portalSpawnCapMultiplier <= 0) return;
        int cap = (int) (AntiLagSettings.minimumRegionMobs * AntiLagSettings.portalSpawnCapMultiplier);
        int monsters = ((RegionMobCounts) level).pandaAntiLag$countNearby(ChunkPos.containing(pos), ChunkEntityData.MONSTER_TYPE);
        if (monsters >= cap) ci.cancel();
    }
}
