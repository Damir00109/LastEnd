package com.damir00109.laststand.downed;

public final class DownedTimer {
    private int remainingTicks;

    public DownedTimer() {
        this(0);
    }

    public DownedTimer(int remainingTicks) {
        this.remainingTicks = Math.max(0, remainingTicks);
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public void setRemainingTicks(int remainingTicks) {
        this.remainingTicks = Math.max(0, remainingTicks);
    }

    public void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public boolean isExpired() {
        return remainingTicks <= 0;
    }
}
