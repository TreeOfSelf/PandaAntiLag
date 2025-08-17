package me.TreeOfSelf.PandaAntiLag;

public class ChunkEntityData {
    public static final int NULL_TYPE = 0;
    public static final int MONSTER_TYPE = 1; 
    public static final int PEACEFUL_TYPE = 2;
    public static final int VEHICLE_TYPE = 3;

    private final int[] nearbyCounts = new int[4];

    public long lastCheck;

    public ChunkEntityData() {
        this.nearbyCounts[MONSTER_TYPE] = 1;
        this.nearbyCounts[PEACEFUL_TYPE] = 1;
        this.nearbyCounts[VEHICLE_TYPE] = 1;
        this.lastCheck = 0;
    }

    public int getNearbyCount(int entityType) {
        return entityType == NULL_TYPE ? 1 : nearbyCounts[entityType];
    }

    public void setNearbyCount(int entityType, int amount) {
        if (entityType != NULL_TYPE) {
            nearbyCounts[entityType] = amount;
        }
    }
}