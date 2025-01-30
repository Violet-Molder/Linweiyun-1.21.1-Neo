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
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class onPlayerAttackEntityEvent {
    //是否暴击
    private static boolean isCriticalHit = false;
    //暴击事件
    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (event.getEntity() instanceof Player player) {
            isCriticalHit = event.isVanillaCritical();
        }
    }

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

                    /*仁慈*/

                    if (!player.level().isClientSide){
                        Object2IntMap.Entry<Holder<Enchantment>> itemEnchantment1 =LinEnchantmentHelper.getEnchantment(OLEnchantments.KINDNESS, itemStack);
                        if (itemEnchantment1 != null){
                            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                            float targetArmor = (float) target.getAttributeValue(Attributes.ARMOR);
                            float targetArmorToughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
                            if (isCriticalHit){
                                baseDamage = baseDamage * 1.5f;
                            }


                            float damage = (float) (baseDamage * (1 - 0.04 * (targetArmor - baseDamage * 4 / (8 + targetArmorToughness))));
                            if (damage >= target.getHealth()){
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
