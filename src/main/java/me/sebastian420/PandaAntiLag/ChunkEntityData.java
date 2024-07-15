package me.sebastian420.PandaAntiLag;

import net.minecraft.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class ChunkEntityData {
    private final Map<String, Integer> nearbyCounts;

    public long lastCheck;

    public ChunkEntityData() {
        this.nearbyCounts = new HashMap<>();
        this.lastCheck = 0;
    }

    public int getNearbyCount(String entityType) {
        return nearbyCounts.getOrDefault(entityType, 1);
    }

    public void setNearbyCount(String entityType, int amount) {
        nearbyCounts.put(entityType, amount);
    }

}