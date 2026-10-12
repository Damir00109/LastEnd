package com.damir00109.laststand.event;

import com.damir00109.laststand.downed.DownedManager;
import com.damir00109.laststand.item.BandageItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class PlayerTickHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            DownedManager.tick(player);
            PlayerRespawnHandler.tick(player);
            BandageItem.tickRevive(player);
        }
    }
}
