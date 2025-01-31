package com.linweiyun.linweiyun.events;


import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.enchantment.LinEnchantmentHelper;
import com.linweiyun.linweiyun.enchantment.OLEnchantments;
import com.linweiyun.linweiyun.net.OLFpsKillerC2SPacket;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.linweiyun.linweiyun.events.onPlayerCriticalHitEvent.isCriticalHit;

@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class onPlayerAttackEntityEvent {
    //是否暴击


    //暴击事件


    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player != null) {
            if (event.getTarget() instanceof LivingEntity target) {
                ItemStack itemStack = player.getMainHandItem();
                if (!itemStack.isEmpty()) {
                    /*
                    *   FPS杀手
                    */
                    // 检测物品是否拥有特定附魔
                    Object2IntMap.Entry<Holder<Enchantment>> itemEnchantment =LinEnchantmentHelper.getEnchantment(OLEnchantments.FPS_KILLER, itemStack);
                    if (itemEnchantment != null){
                        event.setCanceled(true);
                        itemStack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(itemStack));
                        if (player.level().isClientSide){
                            int fps = Minecraft.getInstance().getFps();
                            fpsClientC2S(fps, target);
                        }
                    }



                    if (!player.level().isClientSide){
                        /*仁慈*/
                        Object2IntMap.Entry<Holder<Enchantment>> itemEnchantment1 =LinEnchantmentHelper.getEnchantment(OLEnchantments.KINDNESS, itemStack);
                        if (itemEnchantment1 != null){
                            if (onEntityPreAttack.getNewDamage() + 1 >= target.getHealth()){
                                event.setCanceled(true);
                                if (target.getHealth() > 1) {
                                    target.hurt(player.damageSources().playerAttack(player), target.getHealth()-0.9f);
                                }
                            }
                        }

                        /*牛顿第三定律*/
                        Object2IntMap.Entry<Holder<Enchantment>> itemEnchantment2 =LinEnchantmentHelper.getEnchantment(OLEnchantments.NEWTON_THIRD_LAW, itemStack);
                        if (itemEnchantment2 != null){
                            player.hurt(target.damageSources().mobAttack(target), onEntityPreAttack.getOriDamage());
                            Random random = new Random();
                            int itemEnchantmentLevel = itemEnchantment2.getIntValue();
                            //缴械概率
                            if ((random.nextInt(100) + 1 ) <= (20 + itemEnchantmentLevel* 2)){

                                List<EquipmentSlot> nonEmptySlots = new ArrayList<>();

                                List<EquipmentSlot> equipmentSlots = Arrays.stream(EquipmentSlot.values())
                                        .filter(slot -> slot != EquipmentSlot.BODY)
                                        .toList();
                                target.getItemBySlot(EquipmentSlot.CHEST);
                                for (EquipmentSlot equipmentSlot : equipmentSlots){
                                    ItemStack whetherIsEmpty = target.getItemBySlot(equipmentSlot);
                                    if (!whetherIsEmpty.isEmpty()){
                                        nonEmptySlots.add(equipmentSlot);
                                    }
                                }
                                if (!nonEmptySlots.isEmpty()){

                                    // 生成随机数，范围从 1 到 nonEmptySlots 的大小
                                    int randomIndex = random.nextInt(nonEmptySlots.size());

                                    // 获取随机选择的装备槽位
                                    EquipmentSlot selectedSlot = nonEmptySlots.get(randomIndex);
                                    ItemStack disarmedItemStack = target.getItemBySlot(selectedSlot);
                                    if (!(target instanceof Player targetPlayer)){
                                        int whetherDrop = random.nextInt(100 + 1);

                                        if (whetherDrop <= 10 + itemEnchantmentLevel) {
                                            ItemEntity dropItemEntity = new ItemEntity(target.level(), target.getX(), target.getY(), target.getZ(), disarmedItemStack.copy());

                                            dropItemEntity.setPickUpDelay(5);
                                            target.level().addFreshEntity(dropItemEntity);
                                        }

                                        disarmedItemStack.shrink(disarmedItemStack.getCount());
//                                        target.setItemSlot(selectedSlot, ItemStack.EMPTY);

                                    } else {
                                        int whetherDrop = random.nextInt(100 + 1);
                                        giveOriDropItem(disarmedItemStack.copy(), targetPlayer, target.level(), itemEnchantmentLevel, whetherDrop);
                                        targetPlayer.setItemSlot(selectedSlot, ItemStack.EMPTY);
                                    }

                                }
                            }

                        }

                    }

                }
            }
        }
    }

    static void giveOriDropItem(ItemStack stack, Player player, Level level, int enchantmentLevel, int random){
        boolean canDrop = true;
        boolean canGive = true;
        for (int i = 0; i < player.getInventory().getContainerSize()-5; i++){
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack.isEmpty() && canGive){
                player.getInventory().setItem(i,stack);
                canGive = false;
                canDrop = false;
            }
            if (itemStack.getItem() == stack.getItem() && canGive && !(itemStack.getCount() == itemStack.getMaxStackSize())){
                int maxStackSize = itemStack.getMaxStackSize();
                int remainingSpace = maxStackSize - itemStack.getCount();
                int transferAmount = Math.min(remainingSpace, itemStack.getCount());
                itemStack.grow(transferAmount);
                stack.shrink(transferAmount);
                if (itemStack.isEmpty()) {
                    canGive = false;
                    canDrop = false;
                    return;

                }
            }
        }
        if (canDrop){
            if (random <= 10 + enchantmentLevel) {
                ItemEntity entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
                entity.setPickUpDelay(40);
                level.addFreshEntity(entity);
            }
        }
    }

    @Unique
    @OnlyIn(Dist.CLIENT)
    private static void fpsClientC2S(int fps, LivingEntity target){
        PacketDistributor.sendToServer(new OLFpsKillerC2SPacket(fps, target));
    }
}
