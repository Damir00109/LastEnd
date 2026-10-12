package com.damir00109.laststand.util;

public final class DeathTimerCalculator {
    public static final int TICKS_PER_SECOND = 20;
    public static final long TWENTY_FOUR_HOURS_MS = 24L * 60L * 60L * 1000L;

    private static final int[] DURATION_MINUTES = {1, 5, 10, 15, 20, 25, 30};

    public static int getDownedDurationTicks(int deathCount) {
        int clamped = Math.max(1, Math.min(deathCount, DURATION_MINUTES.length));
        return DURATION_MINUTES[clamped - 1] * 60 * TICKS_PER_SECOND;
    }

    public static int getRemainingSeconds(int ticks) {
        return Math.max(0, ticks) / TICKS_PER_SECOND;
    }
}
