package com.damir00109.laststand.item;

import com.damir00109.laststand.data.LastStandDataAttachments;
import com.damir00109.laststand.downed.DownedManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class BandageItem extends Item {
    private static final int USE_DURATION = 30 * 20;

    public BandageItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Player targetPlayer && LastStandDataAttachments.get(targetPlayer).isDowned()) {
            player.startUsingItem(hand);
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (findDownedTarget(player) != null) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    public static void tickRevive(ServerPlayer player) {
        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof BandageItem)) {
            return;
        }

        LivingEntity target = findDownedTarget(player);
        if (!(target instanceof ServerPlayer downed) || !DownedManager.isDowned(downed)
                || downed.distanceTo(player) > player.entityInteractionRange() + 0.5) {
            return;
        }

        if (DownedManager.progressRevive(player, downed)) {
            DownedManager.exitDowned(downed);
            if (!player.getAbilities().instabuild) {
                player.getUseItem().shrink(1);
            }
            player.stopUsingItem();
        }
    }

    public static LivingEntity findDownedTarget(Player player) {
        double reach = player.entityInteractionRange();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(reach));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player,
                eye,
                end,
                searchBox,
                e -> e instanceof Player p && LastStandDataAttachments.get(p).isDowned(),
                reach * reach
        );

        return hit != null ? (LivingEntity) hit.getEntity() : null;
    }
}
