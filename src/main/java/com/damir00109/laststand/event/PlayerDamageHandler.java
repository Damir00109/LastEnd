package com.damir00109.laststand.event;

import com.damir00109.laststand.config.LastStandConfig;
import com.damir00109.laststand.downed.DownedManager;
import com.damir00109.laststand.player.PlayerDownedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class PlayerDamageHandler {

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (DownedManager.isDowned(player)) {
            if (event.getAmount() > 1.0F) {
                DownedManager.exitDowned(player);
                return;
            }
            player.setHealth(DownedManager.SAFE_HEALTH);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerDownedData data = DownedManager.getData(player);
        DamageSource source = event.getSource();

        if (LastStandConfig.isInstantKill(source)) {
            DownedManager.clearDowned(player);
            return;
        }

        if (data.isDowned()) {
            event.setCanceled(true);
            return;
        }

        float health = player.getHealth();
        float incoming = event.getAmount();

        if (health <= incoming) {
            event.setCanceled(true);
            DownedManager.enterDowned(player, source);
        }
    }
}
