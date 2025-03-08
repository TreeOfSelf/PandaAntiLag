package me.sebastian420.PandaAntiLag;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderPearlLimiter {

    private static long lastCheckTime = 0;

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(EnderPearlLimiter::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime >= AntiLagSettings.enderPearlUpdateInterval) {
            lastCheckTime = currentTime;
            checkAndRemoveExcessEnderPearls(server);
        }
    }

    private static void checkAndRemoveExcessEnderPearls(MinecraftServer server) {
        Map<UUID, List<Entity>> playerPearlMap = new HashMap<>();

        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.getEntitiesByType(EntityType.ENDER_PEARL, entity -> true)) {
                if (entity instanceof EnderPearlEntity enderPearl) {
                    if (enderPearl.getOwner() instanceof ServerPlayerEntity player) {
                        UUID playerUuid = player.getUuid();
                        playerPearlMap.computeIfAbsent(playerUuid, k -> new ArrayList<>()).add(entity);
                    }
                }
            }
        }

        for (Map.Entry<UUID, List<Entity>> entry : playerPearlMap.entrySet()) {
            List<Entity> pearls = entry.getValue();
            int count = pearls.size();

            if (count > AntiLagSettings.maxEnderPearlsPerPlayer) {
                int excessCount = count - AntiLagSettings.maxEnderPearlsPerPlayer;

                for (int i = 0; i < excessCount; i++) {
                    Entity pearl = pearls.get(i);
                    pearl.remove(Entity.RemovalReason.KILLED);
                    pearl.discard();
                }
            }
        }
    }
}