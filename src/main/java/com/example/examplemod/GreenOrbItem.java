package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GreenOrbItem extends Item {
    public GreenOrbItem(Properties properties) {
        super(properties);
    }

    double actualLength;

    /**
     * 当玩家右键使用物品时调用此方法
     *
     * @param level  当前世界
     * @param player 使用物品的玩家
     * @param hand   使用物品的手（主手或副手）
     * @return 交互结果
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        double maxLength = 10.0;
        Vec3 start = player.getEyePosition();
        Vec3 end = player.getLookAngle().normalize().scale(maxLength).add(start);

        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        end = blockHit.getLocation();
        AABB range =  player.getBoundingBox().expandTowards(end.subtract(start));
        List<HitResult> hits = new ArrayList<>();
        List<? extends Entity> entities = level.getEntities(
                player,
                range,
                e -> e != player && (e.isPickable() || e.isAlive())
        );
        for (var entity : entities){
            Vec3 hitPos = entity.getBoundingBox().clip(start, end).orElse( null);
            if (hitPos != null){
                EntityHitResult entityHit = new EntityHitResult(entity, hitPos);
                hits.add(entityHit);
            }
        }
        if (!hits.isEmpty()){
            hits.sort((o1, o2) -> o1.getLocation().distanceToSqr(start) < o2.getLocation().distanceToSqr(start)?-1:1);
            HitResult hitResult = hits.getFirst();
            if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity entity) {
                double entityDistance = Math.sqrt(hitResult.getLocation().distanceToSqr(start));
                maxLength = entityDistance;
                entity.hurt(player.damageSources().playerAttack(player), 4.0F);
            }
        } else {
            // 在破坏方块之前计算距离
            double blockDistance = Math.sqrt(blockHit.getLocation().distanceToSqr(start));
            maxLength = blockDistance; // 更新maxLength为实际距离
            level.destroyBlock(blockHit.getBlockPos(), true, player);
        }
        actualLength = maxLength;

        // 只在客户端执行渲染逻辑
        if (level.isClientSide) {
            // 执行射线检测


//            // 触发光球渲染事件
            LineRendererTest.showRect(player, (float) actualLength);
            player.sendSystemMessage(Component.literal("客户端: " + actualLength));
            // 显示发光球渲染事件
//            GreenOrbRenderer.showOrb(player);
        }

        // 播放使用物品的声音
        player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);


        // 返回成功使用物品的结果
        return InteractionResultHolder.pass(itemStack);
    }

}



