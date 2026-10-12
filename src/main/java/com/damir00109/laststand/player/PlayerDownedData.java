package com.damir00109.laststand.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.damir00109.laststand.downed.DownedState;
import com.damir00109.laststand.downed.DownedTimer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;

public final class PlayerDownedData {
    public static final Codec<PlayerDownedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DownedState.CODEC.fieldOf("state").forGetter(PlayerDownedData::getState),
            Codec.INT.fieldOf("death_count").forGetter(PlayerDownedData::getDeathCount),
            Codec.LONG.fieldOf("death_window_start").forGetter(PlayerDownedData::getDeathWindowStart),
            Codec.INT.fieldOf("remaining_ticks").forGetter(PlayerDownedData::getRemainingTicks),
            GameType.CODEC.optionalFieldOf("respawn_game_mode", GameType.SURVIVAL).forGetter(PlayerDownedData::getRespawnGameMode),
            Codec.INT.optionalFieldOf("revive_progress", 0).forGetter(PlayerDownedData::getReviveProgress),
            Codec.INT.optionalFieldOf("revive_total", 0).forGetter(PlayerDownedData::getReviveTotal)
    ).apply(instance, PlayerDownedData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDownedData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlayerDownedData::isDowned,
            ByteBufCodecs.VAR_INT, PlayerDownedData::getDeathCount,
            ByteBufCodecs.VAR_LONG, PlayerDownedData::getDeathWindowStart,
            ByteBufCodecs.VAR_INT, PlayerDownedData::getRemainingTicks,
            ByteBufCodecs.VAR_INT.map(GameType::byId, GameType::getId), PlayerDownedData::getRespawnGameMode,
            ReviveInfo.STREAM_CODEC, PlayerDownedData::getReviveInfo,
            (isDowned, dc, dws, rt, gm, ri) -> new PlayerDownedData(
                    isDowned ? DownedState.DOWNED : DownedState.NORMAL, dc, dws, rt, gm, ri.progress(), ri.total(), ri.reviverName())
    );

    public record ReviveInfo(int progress, int total, String reviverName) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ReviveInfo> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ReviveInfo::progress,
                ByteBufCodecs.VAR_INT, ReviveInfo::total,
                ByteBufCodecs.STRING_UTF8, ReviveInfo::reviverName,
                ReviveInfo::new);
    }

    private DownedState state;
    private int deathCount;
    private long deathWindowStart;
    private final DownedTimer downedTimer;
    private transient DamageSource lastDamageSource;
    private GameType respawnGameMode;
    private int reviveProgress;
    private int reviveTotal;
    private String reviverName;
    private long reviveActiveUntil;
    private boolean reviveSyncDirty;

    public PlayerDownedData() {
        this.state = DownedState.NORMAL;
        this.deathCount = 0;
        this.deathWindowStart = 0L;
        this.downedTimer = new DownedTimer();
        this.lastDamageSource = null;
        this.respawnGameMode = GameType.SURVIVAL;
        this.reviveProgress = 0;
        this.reviveTotal = 0;
        this.reviverName = "";
        this.reviveActiveUntil = 0L;
        this.reviveSyncDirty = false;
    }

    private PlayerDownedData(DownedState state, int deathCount, long deathWindowStart, int remainingTicks,
                             GameType respawnGameMode, int reviveProgress, int reviveTotal) {
        this(state, deathCount, deathWindowStart, remainingTicks, respawnGameMode, reviveProgress, reviveTotal, "");
    }

    private PlayerDownedData(DownedState state, int deathCount, long deathWindowStart, int remainingTicks,
                             GameType respawnGameMode, int reviveProgress, int reviveTotal, String reviverName) {
        this.state = state;
        this.deathCount = deathCount;
        this.deathWindowStart = deathWindowStart;
        this.downedTimer = new DownedTimer(remainingTicks);
        this.lastDamageSource = null;
        this.respawnGameMode = respawnGameMode;
        this.reviveProgress = reviveProgress;
        this.reviveTotal = reviveTotal;
        this.reviverName = reviverName;
        this.reviveActiveUntil = 0L;
        this.reviveSyncDirty = false;
    }

    private ReviveInfo getReviveInfo() {
        return new ReviveInfo(reviveProgress, reviveTotal, reviverName);
    }

    public DownedState getState() {
        return state;
    }

    public void setState(DownedState state) {
        this.state = state;
    }

    public boolean isDowned() {
        return state == DownedState.DOWNED;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }

    public void incrementDeathCount() {
        this.deathCount++;
    }

    public long getDeathWindowStart() {
        return deathWindowStart;
    }

    public void setDeathWindowStart(long deathWindowStart) {
        this.deathWindowStart = deathWindowStart;
    }

    public DownedTimer getDownedTimer() {
        return downedTimer;
    }

    public int getRemainingTicks() {
        return downedTimer.getRemainingTicks();
    }

    public void setRemainingTicks(int ticks) {
        downedTimer.setRemainingTicks(ticks);
    }

    public DamageSource getLastDamageSource() {
        return lastDamageSource;
    }

    public void setLastDamageSource(DamageSource lastDamageSource) {
        this.lastDamageSource = lastDamageSource;
    }

    public GameType getRespawnGameMode() {
        return respawnGameMode;
    }

    public void setRespawnGameMode(GameType respawnGameMode) {
        this.respawnGameMode = respawnGameMode;
    }

    public int getReviveProgress() {
        return reviveProgress;
    }

    public void setReviveProgress(int reviveProgress) {
        this.reviveProgress = reviveProgress;
    }

    public int getReviveTotal() {
        return reviveTotal;
    }

    public void setReviveTotal(int reviveTotal) {
        this.reviveTotal = reviveTotal;
    }

    public String getReviverName() {
        return reviverName;
    }

    public void setReviverName(String reviverName) {
        this.reviverName = reviverName;
    }

    public boolean isBeingRevived() {
        return !reviverName.isEmpty();
    }

    public long getReviveActiveUntil() {
        return reviveActiveUntil;
    }

    public void setReviveActiveUntil(long reviveActiveUntil) {
        this.reviveActiveUntil = reviveActiveUntil;
    }

    public boolean isReviveActive(long gameTime) {
        return gameTime <= reviveActiveUntil;
    }

    public void markReviveSyncDirty() {
        this.reviveSyncDirty = true;
    }

    public boolean consumeReviveSyncDirty() {
        boolean dirty = reviveSyncDirty;
        reviveSyncDirty = false;
        return dirty;
    }

    public void resetRevive(int totalTicks) {
        this.reviveProgress = 0;
        this.reviveTotal = totalTicks;
        this.reviverName = "";
        this.reviveActiveUntil = 0L;
        this.reviveSyncDirty = false;
    }
}
