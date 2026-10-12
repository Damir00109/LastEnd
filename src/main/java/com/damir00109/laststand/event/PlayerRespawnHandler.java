package com.damir00109.laststand.event;

import com.damir00109.laststand.LastStand;
import com.damir00109.laststand.data.LastStandDataAttachments;
import com.damir00109.laststand.downed.DownedManager;
import com.damir00109.laststand.downed.DownedState;
import com.damir00109.laststand.player.PlayerDownedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerRespawnHandler {

    private static final Map<UUID, GameType> PENDING_RESPAWN = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerDownedData data = DownedManager.getData(player);
        GameType respawnMode = data.getRespawnGameMode();
        if (respawnMode == null) {
            respawnMode = GameType.SURVIVAL;
        }

        if (data.isDowned()) {
            data.setState(DownedState.NORMAL);
            data.setRemainingTicks(0);
            data.setLastDamageSource(null);
            LastStand.LOGGER.info("[Last Stand] Player {} respawned; DOWNED state cleared.", player.getName().getString());
        }

        player.setForcedPose(null);
        player.syncData(LastStandDataAttachments.DOWNED_DATA.get());

        PENDING_RESPAWN.put(player.getUUID(), respawnMode);
    }

    public static void tick(ServerPlayer player) {
        GameType desired = PENDING_RESPAWN.remove(player.getUUID());
        if (desired == null) {
            return;
        }
        player.setGameMode(desired);
        LastStand.LOGGER.info("[Last Stand] Player {} respawned; set game mode to {}.", player.getName().getString(), desired.getName());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerDownedData data = DownedManager.getData(player);
        if (data.isDowned()) {
            player.setForcedPose(Pose.SWIMMING);
            player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
            player.syncData(LastStandDataAttachments.DOWNED_DATA.get());
            MobTargetHandler.releaseTargets(player);
            LastStand.LOGGER.info("[Last Stand] Player {} logged in while DOWNED; reapplied state.", player.getName().getString());
        }
    }
}
