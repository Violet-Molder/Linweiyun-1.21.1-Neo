package com.linweiyun.linweiyun.events;


import com.linweiyun.linweiyun.Config;
import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.enchantment.LinEnchantmentHelper;
import com.linweiyun.linweiyun.enchantment.OLEnchantments;
import com.linweiyun.linweiyun.net.OLFpsKillerC2SPacket;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.atomic.AtomicReference;

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
                        else if (!player.level().isClientSide) {
                                // 处理目标实体
                                // 例如，对目标实体造成伤害
                                float weaponDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                                AtomicReference<Float> damage = new AtomicReference<>((float) 0);
                                //使用回调函数获取fps
                                OLFpsKillerC2SPacket.getFps(getFps -> {
                                    int fps = getFps;
                                    int enchantmentLevel = itemEnchantment.getIntValue();
                                    if (!Config.invertEnchantmentUsage){
                                        if (fps <= Config.startFrame){
                                            damage.set(weaponDamage + enchantmentLevel + Config.maxFpsKillerDamage);
                                        } else {
                                            damage.set((float) (weaponDamage + enchantmentLevel + Config.maxFpsKillerDamage - Config.damagePerFrame * (fps - Config.startFrame)));
                                        }

                                    } else {
                                        if (fps <= Config.startFrame){
                                            damage.set((float) (1 + enchantmentLevel));
                                        } else if (( Config.damagePerFrame * (fps - Config.startFrame)) >= Config.maxFpsKillerDamage ){
                                            damage.set(weaponDamage + enchantmentLevel + Config.maxFpsKillerDamage);
                                        }
                                        else {
                                            damage.set((float) (weaponDamage + enchantmentLevel + Config.damagePerFrame * (fps - Config.startFrame)));
                                        }
                                    }
                                    player.sendSystemMessage(Component.literal(String.valueOf(damage.get())));
                                    if (damage.get() > 0) {
                                        target.hurt(player.damageSources().playerAttack(player), damage.get());
                                    } else {
                                        target.heal(damage.get());
                                    }
                                });





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

                    }

                }
            }
        }
    }



    @Unique
    @OnlyIn(Dist.CLIENT)
    private static void fpsClientC2S(int fps, LivingEntity target){
        PacketDistributor.sendToServer(new OLFpsKillerC2SPacket(fps, target));
    }
}
