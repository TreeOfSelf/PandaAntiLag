package me.TreeOfSelf.PandaAntiLag;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Tracks which player is responsible for each area kept loaded by a portal ticket.
// An entity going through a portal is blamed on the nearest player simulating the chunk it left from,
// or, if no player is there, on the owner of the portal ticket loading that chunk. So chains of portals
// loading portals are all blamed on the player who started the chain.
public class PortalOwnership {
    // Vanilla portal tickets last 300 ticks and load a radius of 3 chunks
    private static final int TICKET_TICKS = 300;
    private static final int TICKET_RADIUS = 3;
    private static final UUID NO_OWNER = new UUID(0, 0);

    private record Area(UUID owner, int expires) {}

    private static final Map<ResourceKey<Level>, Long2ObjectMap<Area>> areas = new HashMap<>();
    // Owner of the entity currently teleporting (a stack, since passengers teleport inside their vehicle's teleport)
    private static final Deque<UUID> teleporting = new ArrayDeque<>();

    // Players aren't capped; they load chunks around themselves anyway
    public static void startTeleport(Entity entity) {
        UUID owner = entity.level() instanceof ServerLevel level && !(entity instanceof Player) ? findOwner(level, entity.chunkPosition()) : null;
        teleporting.push(owner == null ? NO_OWNER : owner);
    }

    public static void endTeleport() {
        teleporting.poll();
    }

    // Returns false if the portal ticket at this spot would put its owner over the cap
    public static boolean allowTicket(ServerLevel level, ChunkPos at) {
        UUID owner = teleporting.peek();
        if (owner == null || owner == NO_OWNER || AntiLagSettings.maxPortalLoadsPerPlayer <= 0) return true;
        int now = level.getServer().getTickCount();
        Long2ObjectMap<Area> levelAreas = areas.computeIfAbsent(level.dimension(), k -> new Long2ObjectOpenHashMap<>());
        Area existing = levelAreas.get(at.pack());
        boolean alreadyOwned = existing != null && existing.expires > now && existing.owner.equals(owner);
        if (!alreadyOwned && countOwned(owner, now) >= AntiLagSettings.maxPortalLoadsPerPlayer) return false;
        levelAreas.put(at.pack(), new Area(owner, now + TICKET_TICKS));
        return true;
    }

    private static UUID findOwner(ServerLevel level, ChunkPos from) {
        int simulationDistance = level.getServer().getPlayerList().getSimulationDistance();
        ServerPlayer nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            ChunkPos pos = player.chunkPosition();
            int distance = Math.max(Math.abs(pos.x() - from.x()), Math.abs(pos.z() - from.z()));
            if (distance <= simulationDistance && distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        if (nearest != null) return nearest.getUUID();

        Long2ObjectMap<Area> levelAreas = areas.get(level.dimension());
        if (levelAreas == null) return null;
        int now = level.getServer().getTickCount();
        for (int dx = -TICKET_RADIUS; dx <= TICKET_RADIUS; dx++) {
            for (int dz = -TICKET_RADIUS; dz <= TICKET_RADIUS; dz++) {
                Area area = levelAreas.get(ChunkPos.pack(from.x() + dx, from.z() + dz));
                if (area != null && area.expires > now) return area.owner;
            }
        }
        // Loaded by something else (forceload etc.), nobody to blame
        return null;
    }

    // Counts the owner's active areas, dropping expired ones along the way
    private static int countOwned(UUID owner, int now) {
        int count = 0;
        for (Long2ObjectMap<Area> levelAreas : areas.values()) {
            var it = levelAreas.values().iterator();
            while (it.hasNext()) {
                Area area = it.next();
                if (area.expires <= now) it.remove();
                else if (area.owner.equals(owner)) count++;
            }
        }
        return count;
    }
}
