package com.damir00109.laststand.downed;

import com.damir00109.laststand.LastStand;
import com.damir00109.laststand.config.LastStandConfig;
import com.damir00109.laststand.data.LastStandDataAttachments;
import com.damir00109.laststand.event.MobTargetHandler;
import com.damir00109.laststand.player.PlayerDownedData;
import com.damir00109.laststand.util.DeathTimerCalculator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.GameType;

public final class DownedManager {
    public static final float SAFE_HEALTH = 1.0F;

    public static PlayerDownedData getData(ServerPlayer player) {
        return LastStandDataAttachments.get(player);
    }

    public static boolean isDowned(ServerPlayer player) {
        return getData(player).isDowned();
    }

    public static void enterDowned(ServerPlayer player) {
        enterDowned(player, null);
    }

    public static void enterDowned(ServerPlayer player, DamageSource source) {
        PlayerDownedData data = getData(player);
        if (data.isDowned()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - data.getDeathWindowStart() >= DeathTimerCalculator.TWENTY_FOUR_HOURS_MS) {
            data.setDeathCount(0);
            data.setDeathWindowStart(now);
        }

        data.setState(DownedState.DOWNED);
        data.setRespawnGameMode(player.gameMode.getGameModeForPlayer());
        data.setRemainingTicks(DeathTimerCalculator.getDownedDurationTicks(data.getDeathCount() + 1));
        data.setLastDamageSource(source);
        data.resetRevive(randomReviveTicks(player));

        player.setHealth(SAFE_HEALTH);
        player.setForcedPose(Pose.SWIMMING);
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);

        player.syncData(LastStandDataAttachments.DOWNED_DATA.get());

        MobTargetHandler.releaseTargets(player);

        LastStand.LOGGER.info("[Last Stand] Player {} entered DOWNED state. Death count: {}, Remaining: {} seconds",
                player.getName().getString(), data.getDeathCount(), DeathTimerCalculator.getRemainingSeconds(data.getRemainingTicks()));
    }

    public static void exitDowned(ServerPlayer player) {
        if (!clearDowned(player)) {
            return;
        }

        LastStand.LOGGER.info("[Last Stand] Player {} has been revived/returned to normal state.", player.getName().getString());
    }

    public static boolean clearDowned(ServerPlayer player) {
        PlayerDownedData data = getData(player);
        if (!data.isDowned()) {
            return false;
        }

        data.setState(DownedState.NORMAL);
        data.setRemainingTicks(0);
        data.setLastDamageSource(null);
        data.resetRevive(0);
        player.setForcedPose(null);
        player.gameMode.changeGameModeForPlayer(data.getRespawnGameMode());

        player.syncData(LastStandDataAttachments.DOWNED_DATA.get());
        return true;
    }

    public static void tick(ServerPlayer player) {
        PlayerDownedData data = getData(player);
        if (!data.isDowned() || !player.isAlive()) {
            return;
        }

        if (player.getHealth() != SAFE_HEALTH) {
            player.setHealth(SAFE_HEALTH);
        }

        if (player.isShiftKeyDown()) {
            player.setShiftKeyDown(false);
        }

        long gameTime = player.level().getGameTime();
        boolean beingRevived = data.isReviveActive(gameTime);
        if (!beingRevived && data.isBeingRevived()) {
            data.setReviverName("");
            data.markReviveSyncDirty();
        }

        int prevRemaining = data.getRemainingTicks();
        if (!beingRevived) {
            data.getDownedTimer().tick();
        }

        if (data.getRemainingTicks() != prevRemaining && data.getRemainingTicks() % 5 == 0) {
            player.syncData(LastStandDataAttachments.DOWNED_DATA.get());
        }

        if (data.consumeReviveSyncDirty() || (beingRevived && gameTime % 4 == 0)) {
            player.syncData(LastStandDataAttachments.DOWNED_DATA.get());
        }

        if (data.getDownedTimer().isExpired()) {
            LastStand.LOGGER.info("[Last Stand] Player {}'s DOWNED timer expired.", player.getName().getString());
            data.incrementDeathCount();
            data.setState(DownedState.NORMAL);
            data.setRemainingTicks(0);
            player.setForcedPose(null);
            player.syncData(LastStandDataAttachments.DOWNED_DATA.get());
            try {
                DamageSource source = data.getLastDamageSource();
                DamageSource deathSource = source != null ? source : player.level().damageSources().generic();
                player.getCombatTracker().recordDamage(deathSource, player.getMaxHealth());
                player.setHealth(0.0F);
                player.die(deathSource);
            } finally {
                data.setLastDamageSource(null);
            }
        }
    }

    public static boolean progressRevive(ServerPlayer reviver, ServerPlayer downed) {
        PlayerDownedData data = getData(downed);
        if (!data.isDowned()) {
            return false;
        }

        if (data.getReviveTotal() <= 0) {
            data.setReviveTotal(randomReviveTicks(downed));
        }

        long gameTime = downed.level().getGameTime();
        data.setReviveActiveUntil(gameTime + 8);
        String name = reviver.getGameProfile().getName();
        if (!name.equals(data.getReviverName())) {
            data.setReviverName(name);
            data.markReviveSyncDirty();
        }

        int progress = data.getReviveProgress() + 1;
        data.setReviveProgress(progress);
        if (progress % 4 == 0) {
            data.markReviveSyncDirty();
        }

        return progress >= data.getReviveTotal();
    }

    private static int randomReviveTicks(ServerPlayer player) {
        int min = Math.min(LastStandConfig.REVIVE_SECONDS_MIN.get(), LastStandConfig.REVIVE_SECONDS_MAX.get());
        int max = Math.max(LastStandConfig.REVIVE_SECONDS_MIN.get(), LastStandConfig.REVIVE_SECONDS_MAX.get());
        return player.getRandom().nextIntBetweenInclusive(min, max) * 20;
    }

    public static int getRemainingSeconds(ServerPlayer player) {
        return DeathTimerCalculator.getRemainingSeconds(getData(player).getRemainingTicks());
    }
}
