package com.damir00109.laststand.event;

import com.damir00109.laststand.config.LastStandConfig;
import com.damir00109.laststand.downed.DownedManager;
import com.damir00109.laststand.player.PlayerDownedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Items;
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
            if (canUseTotem(player, source)) {
                return;
            }
            event.setCanceled(true);
            DownedManager.enterDowned(player, source);
        }
    }

    private static boolean canUseTotem(ServerPlayer player, DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).is(Items.TOTEM_OF_UNDYING)) {
                return true;
            }
        }
        return false;
    }
}
