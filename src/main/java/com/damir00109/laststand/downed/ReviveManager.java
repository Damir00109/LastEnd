package com.damir00109.laststand.downed;

import com.damir00109.laststand.LastStand;
import net.minecraft.server.level.ServerPlayer;

public final class ReviveManager {
    public static void revive(ServerPlayer downedPlayer, ServerPlayer reviver) {
        if (downedPlayer == null || reviver == null) {
            return;
        }
        if (!DownedManager.isDowned(downedPlayer)) {
            return;
        }

        LastStand.LOGGER.info("[Last Stand] Player {} is being revived by {}.", downedPlayer.getName().getString(), reviver.getName().getString());
        DownedManager.exitDowned(downedPlayer);
    }
}
