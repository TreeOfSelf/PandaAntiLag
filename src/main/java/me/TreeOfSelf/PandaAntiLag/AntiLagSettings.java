package me.TreeOfSelf.PandaAntiLag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AntiLagSettings {
    private static final File CONFIG_FILE = new File("./config/PandaAntiLag.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static int regionSize = 6;
    public static int regionSizeBits = 2;
    public static int regionBuffer = 1;
    public static int minimumRegionMobs = 75;
    public static int minimumRegionVehicle = 75;
    public static int projectileMax = 150;
    public static int mobStaggerLenience = 200;
    public static int vehicleStaggerLenience = 200;
    public static int tickTimeLenience = 10;
    public static long updateInterval = 10000;
    public static long enderPearlUpdateInterval = 10000;
    public static int maxEnderPearlsPerPlayer = 20;

    public static void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);

                regionSize = json.has("regionSize") ? json.get("regionSize").getAsInt() : regionSize;
                regionSizeBits = json.has("regionSizeBits") ? json.get("regionSizeBits").getAsInt() : regionSizeBits;
                regionBuffer = json.has("regionBuffer") ? json.get("regionBuffer").getAsInt() : regionBuffer;
                minimumRegionMobs = json.has("minimumRegionMobs") ? json.get("minimumRegionMobs").getAsInt() : minimumRegionMobs;
                minimumRegionVehicle = json.has("minimumRegionVehicle") ? json.get("minimumRegionVehicle").getAsInt() : minimumRegionVehicle;
                projectileMax = json.has("projectileMax") ? json.get("projectileMax").getAsInt() : projectileMax;
                mobStaggerLenience = json.has("mobStaggerLenience") ? json.get("mobStaggerLenience").getAsInt() : mobStaggerLenience;
                vehicleStaggerLenience = json.has("vehicleStaggerLenience") ? json.get("vehicleStaggerLenience").getAsInt() : vehicleStaggerLenience;
                tickTimeLenience = json.has("tickTimeLenience") ? json.get("tickTimeLenience").getAsInt() : tickTimeLenience;
                updateInterval = json.has("updateInterval") ? json.get("updateInterval").getAsLong() : updateInterval;
                enderPearlUpdateInterval = json.has("enderPearlUpdateInterval") ? json.get("enderPearlUpdateInterval").getAsLong() : enderPearlUpdateInterval;
                maxEnderPearlsPerPlayer = json.has("maxEnderPearlsPerPlayer") ? json.get("maxEnderPearlsPerPlayer").getAsInt() : maxEnderPearlsPerPlayer;
            } catch (IOException ignored) {
            }
        }
        saveConfig();
    }

    public static void saveConfig() {
        JsonObject json = new JsonObject();
        json.addProperty("regionSize", regionSize);
        json.addProperty("regionSizeBits", regionSizeBits);
        json.addProperty("regionBuffer", regionBuffer);
        json.addProperty("minimumRegionMobs", minimumRegionMobs);
        json.addProperty("minimumRegionVehicle", minimumRegionVehicle);
        json.addProperty("projectileMax", projectileMax);
        json.addProperty("mobStaggerLenience", mobStaggerLenience);
        json.addProperty("vehicleStaggerLenience", vehicleStaggerLenience);
        json.addProperty("tickTimeLenience", tickTimeLenience);
        json.addProperty("updateInterval", updateInterval);
        json.addProperty("enderPearlUpdateInterval", enderPearlUpdateInterval);
        json.addProperty("maxEnderPearlsPerPlayer", maxEnderPearlsPerPlayer);

        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException ignored) {
        }
    }
}
