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
    // Portals stop spawning piglins when nearby monsters >= minimumRegionMobs * this (0 disables)
    public static double portalSpawnCapMultiplier = 2.0;
    // Most portal-loaded areas one player can be responsible for at once (0 disables)
    public static int maxPortalLoadsPerPlayer = 10;
    // Falling, swimming and path-following mobs still move on ticks the throttle skips, only their AI is throttled
    public static boolean movementWhileThrottled = true;
    // Mobs that would despawn far from players also despawn when their dimension has no players at all
    public static boolean despawnWithoutPlayers = true;
    public static long enderPearlUpdateInterval = 10000;
    public static int maxEnderPearlsPerPlayer = 20;
    // Skip a player's entity tick while they touch a chunk that isn't loaded yet (vanilla does this for spectators)
    public static boolean waitForChunksBeforePlayerTick = true;
    // Don't collision-move a player into chunks that aren't loaded yet (that load blocks the whole server)
    public static boolean waitForChunksBeforePlayerMove = true;
    // A throttled mob only gets a movement tick for horizontal speed above this (blocks/tick): knockback yes, crowd jostling no
    public static double movementTickMinSpeed = 0.2;
    // Overload throttle: once the server's average tick takes longer than overloadStartMspt, every mob's AI (not
    // only mobs in dense spots) runs 1 + (mspt - start) / step times less often, up to overloadMaxDivisor.
    // Mobs going after a player stay at full speed. 0 start disables.
    public static double overloadStartMspt = 25;
    public static double overloadStepMspt = 5;
    public static int overloadMaxDivisor = 10;
    // Run the data fixer for stored mobs on a worker thread when their chunk loads, instead of the main thread
    public static boolean asyncEntityUpgrade = true;
    // After a villager fails to path to any bed / job site / meeting point, it waits this many ticks (+ up to the
    // same again) before searching for that kind again. 0 = vanilla (retry every 1-2 seconds)
    public static int poiSearchBackoffTicks = 200;
    // While overloaded, natural spawn attempts in each chunk also only run every overload-divisor ticks, capped
    // at this (1 = never throttle spawning). The every-400-ticks animal spawn pass is never skipped.
    public static int spawnOverloadMaxDivisor = 4;
    // Set every tick from the server's average tick time (not a config value)
    public static volatile int currentOverloadDivisor = 1;

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
                portalSpawnCapMultiplier = json.has("portalSpawnCapMultiplier") ? json.get("portalSpawnCapMultiplier").getAsDouble() : portalSpawnCapMultiplier;
                maxPortalLoadsPerPlayer = json.has("maxPortalLoadsPerPlayer") ? json.get("maxPortalLoadsPerPlayer").getAsInt() : maxPortalLoadsPerPlayer;
                movementWhileThrottled = json.has("movementWhileThrottled") ? json.get("movementWhileThrottled").getAsBoolean() : movementWhileThrottled;
                despawnWithoutPlayers = json.has("despawnWithoutPlayers") ? json.get("despawnWithoutPlayers").getAsBoolean() : despawnWithoutPlayers;
                waitForChunksBeforePlayerTick = json.has("waitForChunksBeforePlayerTick") ? json.get("waitForChunksBeforePlayerTick").getAsBoolean() : waitForChunksBeforePlayerTick;
                waitForChunksBeforePlayerMove = json.has("waitForChunksBeforePlayerMove") ? json.get("waitForChunksBeforePlayerMove").getAsBoolean() : waitForChunksBeforePlayerMove;
                movementTickMinSpeed = json.has("movementTickMinSpeed") ? json.get("movementTickMinSpeed").getAsDouble() : movementTickMinSpeed;
                overloadStartMspt = json.has("overloadStartMspt") ? json.get("overloadStartMspt").getAsDouble() : overloadStartMspt;
                overloadStepMspt = json.has("overloadStepMspt") ? json.get("overloadStepMspt").getAsDouble() : overloadStepMspt;
                overloadMaxDivisor = json.has("overloadMaxDivisor") ? json.get("overloadMaxDivisor").getAsInt() : overloadMaxDivisor;
                asyncEntityUpgrade = json.has("asyncEntityUpgrade") ? json.get("asyncEntityUpgrade").getAsBoolean() : asyncEntityUpgrade;
                poiSearchBackoffTicks = json.has("poiSearchBackoffTicks") ? json.get("poiSearchBackoffTicks").getAsInt() : poiSearchBackoffTicks;
                spawnOverloadMaxDivisor = json.has("spawnOverloadMaxDivisor") ? json.get("spawnOverloadMaxDivisor").getAsInt() : spawnOverloadMaxDivisor;
                enderPearlUpdateInterval = json.has("enderPearlUpdateInterval") ? json.get("enderPearlUpdateInterval").getAsLong() : enderPearlUpdateInterval;
                maxEnderPearlsPerPlayer = json.has("maxEnderPearlsPerPlayer") ? json.get("maxEnderPearlsPerPlayer").getAsInt() : maxEnderPearlsPerPlayer;
            } catch (Exception ignored) {
                // A malformed config should not take the server down. Fall back to defaults.
            }
        }
        sanitize();
        saveConfig();
    }

    private static void sanitize() {
        // These feed divisions and array sizes. A 0 lenience used to produce an infinite
        // divisor, which made entities effectively stop ticking. Clamp everything sensible.
        regionSizeBits = Math.max(0, Math.min(10, regionSizeBits));
        regionBuffer = Math.max(0, regionBuffer);
        minimumRegionMobs = Math.max(0, minimumRegionMobs);
        minimumRegionVehicle = Math.max(0, minimumRegionVehicle);
        projectileMax = Math.max(0, projectileMax);
        mobStaggerLenience = Math.max(1, mobStaggerLenience);
        vehicleStaggerLenience = Math.max(1, vehicleStaggerLenience);
        tickTimeLenience = Math.max(1, tickTimeLenience);
        maxPortalLoadsPerPlayer = Math.max(0, maxPortalLoadsPerPlayer);
        enderPearlUpdateInterval = Math.max(1000L, enderPearlUpdateInterval);
        maxEnderPearlsPerPlayer = Math.max(0, maxEnderPearlsPerPlayer);
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
        json.addProperty("portalSpawnCapMultiplier", portalSpawnCapMultiplier);
        json.addProperty("maxPortalLoadsPerPlayer", maxPortalLoadsPerPlayer);
        json.addProperty("movementWhileThrottled", movementWhileThrottled);
        json.addProperty("despawnWithoutPlayers", despawnWithoutPlayers);
        json.addProperty("enderPearlUpdateInterval", enderPearlUpdateInterval);
        json.addProperty("maxEnderPearlsPerPlayer", maxEnderPearlsPerPlayer);
        json.addProperty("waitForChunksBeforePlayerTick", waitForChunksBeforePlayerTick);
        json.addProperty("waitForChunksBeforePlayerMove", waitForChunksBeforePlayerMove);
        json.addProperty("movementTickMinSpeed", movementTickMinSpeed);
        json.addProperty("overloadStartMspt", overloadStartMspt);
        json.addProperty("overloadStepMspt", overloadStepMspt);
        json.addProperty("overloadMaxDivisor", overloadMaxDivisor);
        json.addProperty("asyncEntityUpgrade", asyncEntityUpgrade);
        json.addProperty("poiSearchBackoffTicks", poiSearchBackoffTicks);
        json.addProperty("spawnOverloadMaxDivisor", spawnOverloadMaxDivisor);

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
