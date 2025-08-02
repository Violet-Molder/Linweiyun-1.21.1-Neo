package com.linweiyun.linweiyun.events;

import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.enchantment.LinEnchantmentHelper;
import com.linweiyun.linweiyun.enchantment.OLEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class onEntityPreAttack {


    private static float oriDamage = 0;
    private static float newDamage = 0;
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (!player.level().isClientSide) {
                ItemStack itemStack = player.getMainHandItem();
                LivingEntity target =  event.getEntity();
                newDamage = event.getNewDamage();
                oriDamage = event.getOriginalDamage();
                /*牛顿第三定律*/
                Object2IntMap.Entry<Holder<Enchantment>> itemEnchantment2 = LinEnchantmentHelper.getEnchantment(OLEnchantments.NEWTON_THIRD_LAW, itemStack);
                if (itemEnchantment2 != null){
                    player.hurt(target.damageSources().mobAttack(target), getOriDamage());
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

                            } else {
                                giveOriDropItem(disarmedItemStack.copy(), targetPlayer, target.level());
                                targetPlayer.setItemSlot(selectedSlot, ItemStack.EMPTY);
                            }

                        }
                    }

                }
            }

        }
    }

    static void giveOriDropItem(ItemStack stack, Player player, Level level){
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
            ItemEntity entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
            entity.setPickUpDelay(60);
            level.addFreshEntity(entity);

        }
    }

    public static float getNewDamage() {
        return newDamage;
    }
    public static float getOriDamage() {
        return oriDamage;
    }
}
