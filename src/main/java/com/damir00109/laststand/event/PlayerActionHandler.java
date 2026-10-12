package com.damir00109.laststand.event;

import com.damir00109.laststand.data.LastStandDataAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class PlayerActionHandler {

    private static boolean isDownedPlayer(LivingEntity entity) {
        return entity instanceof net.minecraft.world.entity.player.Player player
                && LastStandDataAttachments.get(player).isDowned();
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isDownedPlayer(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isDownedPlayer(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isDownedPlayer(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isDownedPlayer(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isDownedPlayer(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isDownedPlayer(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onUseItem(LivingEntityUseItemEvent.Start event) {
        if (isDownedPlayer(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (isDownedPlayer(event.getPlayer())) event.setCanceled(true);
    }
}
