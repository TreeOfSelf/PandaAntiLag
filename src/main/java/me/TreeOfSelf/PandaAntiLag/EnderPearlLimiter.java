package me.TreeOfSelf.PandaAntiLag;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;

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

        for (ServerLevel world : server.getAllLevels()) {
            for (ThrownEnderpearl enderPearl : world.getEntities(EntityTypes.ENDER_PEARL, e -> true)) {
                if (enderPearl.getOwner() instanceof ServerPlayer player) {
                    UUID playerUuid = player.getUUID();
                    playerPearlMap.computeIfAbsent(playerUuid, k -> new ArrayList<>()).add(enderPearl);
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
                }
            }
        }
    }
}
