package me.TreeOfSelf.PandaAntiLag.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.TreeOfSelf.PandaAntiLag.AntiLagSettings;
import me.TreeOfSelf.PandaAntiLag.PandaAntiLag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// Vanilla loads a chunk's mobs in two steps, both on the main thread: run the data fixer over the stored entity
// chunk (upgradeChunkTag), then build the entities. Entity chunks are only rewritten when their mobs change, so
// in an old world most of them are stored in an older format and get the full data-fixer pass on every load;
// on the lag simulation that was the biggest main-thread cost of loading mob-heavy areas.
// The data fixer is pure NBT -> NBT (block chunks already run it off-thread), so do that step on a worker as
// soon as the file is read. The main-thread call then sees the current data version and returns at once.
// Building the entities stays on the main thread. If the off-thread upgrade fails, the original tag goes
// through unchanged and vanilla's own main-thread upgrade handles it exactly as before.
@Mixin(EntityStorage.class)
public abstract class EntityStorageAsyncUpgradeMixin {

	@Shadow @Final private SimpleRegionStorage simpleRegionStorage;

	@WrapOperation(method = "loadEntities", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/chunk/storage/SimpleRegionStorage;read(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<Optional<CompoundTag>> pandaAntiLag$upgradeOffThread(SimpleRegionStorage storage, ChunkPos pos,
																				   Operation<CompletableFuture<Optional<CompoundTag>>> original) {
		CompletableFuture<Optional<CompoundTag>> read = original.call(storage, pos);
		if (!AntiLagSettings.asyncEntityUpgrade) return read;
		return read.thenApplyAsync(tag -> tag.map(t -> {
			try {
				return storage.upgradeChunkTag(t.copy(), -1);
			} catch (Throwable e) {
				PandaAntiLag.LOGGER.warn("Off-thread entity data upgrade failed for {}, leaving it to the main thread", pos, e);
				return t;
			}
		}), Util.backgroundExecutor());
	}
}
