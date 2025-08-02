package com.linweiyun.linweiyun.events;

import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.enchantment.LinEnchantmentHelper;
import com.linweiyun.linweiyun.enchantment.OLEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class onPlayerUseItemEvent {

    @SubscribeEvent
    public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {

        Level level = event.getLevel();
        if (!level.isClientSide()) {
            Player player = event.getEntity();
            ItemStack itemStack = player.getItemInHand(event.getHand());
            // 检查物品是否有冷却时间
            if (player.getCooldowns().isOnCooldown(itemStack.getItem())) {
                return; // 物品正在冷却中，不执行任何效果
            }

            // 检测物品是否拥有特定附魔
            Object2IntMap.Entry<Holder<Enchantment>> itemEnchantment =LinEnchantmentHelper.getEnchantment(OLEnchantments.CONFUSION, itemStack);
            if (itemEnchantment != null){
                Random random = new Random();
                int effectNumber = random.nextInt(100) + 1;
                if (effectNumber <= 20 ) {
                    int offsetX = random.nextInt(101) - 50;
                    int offsetY = random.nextInt(20) - 15;
                    int offsetZ = random.nextInt(101) - 50;
                    int nowX = (int) player.getX();
                    int nowY = (int) player.getY();
                    int nowZ = (int) player.getZ();
                    int newX = nowX + offsetX;
                    int newY = Math.max(-59, nowY + offsetY);
                    int newZ = nowZ + offsetZ;
                    BlockPos newPos = new BlockPos(newX, newY, newZ);
                    BlockPos tpPos =  findSafeLocation(level, newPos);
                    if (tpPos != null) {
                        player.sendSystemMessage(Component.translatable("message.linweiyun.safe_location_found", tpPos.getX(), tpPos.getY(), tpPos.getZ()));
                        new Timer().schedule(new TimerTask() { public void run() { player.teleportTo(tpPos.getX(), tpPos.getY(), tpPos.getZ()); } }, 3000);
                    }
                } else if (effectNumber <= 40 ) {
                    player.heal(4);
                } else if (effectNumber <= 45 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 0));
                } else if (effectNumber <= 50 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 250, 1));
                } else if (effectNumber <= 55 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 400, 1));
                } else if (effectNumber <= 60 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
                } else if (effectNumber <= 65 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 1));
                } else if (effectNumber <= 70 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 1));
                } else if (effectNumber <= 75) {
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 400, 2));
                } else if (effectNumber <= 80 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 400, 0));
                } else if (effectNumber <= 85 ) {
                    player.hurt(player.damageSources().playerAttack(player), 4.0F);
                } else if (effectNumber <= 90 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
                } else if (effectNumber <= 95 ) {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0));
                } else if (effectNumber <= 100 ) {
                    player.addItem(new ItemStack(Items.DIAMOND, 2));
                }

                // 添加使用冷却时间（例如：20 ticks = 1秒）
                player.getCooldowns().addCooldown(itemStack.getItem(), 20);

                // 减少物品耐久度
                itemStack.hurtAndBreak(1,player, LivingEntity.getSlotForHand(event.getHand()));



            }
        }
    }

    private static BlockPos findSafeLocation(Level level, BlockPos  pos) {
        boolean posOneIsSafe = testTheBlockPosIsSafe(level, pos);
        if (posOneIsSafe) {
            System.out.println("功劳是1的" +  pos);
            return  pos;
        } else {
            for (int offsetY = -10; offsetY <= Math.min(10, pos.getY() + 59); offsetY++) {
                for (int offsetX = -10; offsetX <= 10; offsetX++) {
                    for (int offsetZ = -10; offsetZ <= 10; offsetZ++) {

                        BlockPos newPos = pos.offset(offsetX, offsetY, offsetZ);
                        BlockState blockState = level.getBlockState(newPos);
                        if (blockState.isAir()){
                            if (level.getBlockState(newPos.below()).isAir() || level.getBlockState(newPos.above()).isAir()){
                                for (int offsetY1 = 0; newPos.getY() - offsetY1 <= -59; offsetY++) {
                                    BlockPos testPos1 = newPos.offset(0, - offsetY, 0);
                                    if (testTheBlockPosIsSafe(level, testPos1)){
                                        return testPos1;
                                    }
                                }
                            }

                        } else if (blockState.isSolidRender(level, newPos)){
                            System.out.println("是方块");
                            for (int i = 0; i < 100; i++){

                                BlockPos testPos1 = newPos.offset(0, i, 0);
                                System.out.println("方块Y偏移中，当前坐标：" + testPos1);
                                if (testTheBlockPosIsSafe(level, testPos1)){
                                    return testPos1;
                                }
                            }
                        }

                    }
                }
            }
        }
        return null;
    }
    private static boolean testTheBlockPosIsSafe(Level level, BlockPos pos) {
        if (level.getBlockState( pos).isAir()) {
            if ((level.getBlockState(pos.above()).isAir()) && level.getBlockState(pos.below()).isSolidRender(level, pos) && level.getBlockState(pos.above().above()).isAir()){
//                    for (int x = -1; x < 2; x++){
//                        for (int z = -1; z < 2; z++){
//                            if (!level.getBlockState(pos.offset(x, 1, z)).isAir()){
//                                return false;
//                            }
//                        }
//                    }

                return true;

            }
        }
//        else if (level.getBlockState(pos).isSolidRender(level, pos) ) {
//
//            BlockPos testPos1 = pos.offset(0,  1, 0);
//            BlockPos testPos2 = pos.offset(0,  2, 0);
//            if(level.getBlockState(testPos1).isAir() && level.getBlockState(testPos2).isAir()){
//                System.out.println("3" +  pos);
////                for (int y = 0; y < 3; y++){
////                    for (int x = -1; x < 2; x++){
////                        for (int z = -1; z < 2; z++){
////                            if (!level.getBlockState(pos.offset(x, y, z)).isAir()){
////                                return false;
////                            }
////                        }
////                    }
////                }
//                return true;
//            }
//        }
        return false;
    }

}
