package com.damir00109.laststand.event;

import com.damir00109.laststand.downed.DownedManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

public final class MobTargetHandler {
    private static final double RELEASE_RADIUS = 64.0D;

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && DownedManager.isDowned(player)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    public static void releaseTargets(ServerPlayer player) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RELEASE_RADIUS))) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
            }
        }
    }
}
