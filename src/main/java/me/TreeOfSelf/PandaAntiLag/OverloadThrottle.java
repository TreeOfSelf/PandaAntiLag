package me.TreeOfSelf.PandaAntiLag;

import net.minecraft.server.MinecraftServer;

import java.util.Arrays;

// How many ticks apart every mob ticks while the whole server is behind. The median of the last 100 ticks decides:
// the average (and vanilla's smoothed tick time) jumps after one long tick, the median only moves when most ticks
// of the last 5 seconds were slow.
public final class OverloadThrottle {
    private static int lastTick = -1;
    private static int lastDivisor = 1;

    private OverloadThrottle() {
    }

    public static int divisor(MinecraftServer server) {
        // Every dimension asks once per tick, the answer is the same
        int tick = server.getTickCount();
        if (tick != lastTick) {
            lastTick = tick;
            lastDivisor = AntiLagSettings.overloadStartMspt <= 0 ? 1
                    : divisorFor(server.getTickTimesNanos(), AntiLagSettings.overloadStartMspt,
                            AntiLagSettings.overloadStepMspt, AntiLagSettings.overloadMaxDivisor);
        }
        return lastDivisor;
    }

    static int divisorFor(long[] tickTimesNanos, double startMspt, double stepMspt, int maxDivisor) {
        long[] sorted = tickTimesNanos.clone();
        Arrays.sort(sorted);
        double median = sorted[sorted.length / 2] / 1_000_000.0;
        if (median <= startMspt) return 1;
        double steps = Math.ceil((median - startMspt) / Math.max(1.0, stepMspt));
        return (int) Math.min(maxDivisor, 1 + steps);
    }
}
