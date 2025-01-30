package com.linweiyun.linweiyun.events;


import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.enchantment.OLEnchantments;
import com.linweiyun.linweiyun.net.OLFpsKillerC2SPacket;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class SendFpsEvent {

    @SubscribeEvent
    public static void onPlayerTick(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player != null) {

            if (event.getTarget() instanceof LivingEntity target) {
                ItemStack itemStack = player.getMainHandItem();
                if (!itemStack.isEmpty()) {
                    // 检测物品是否拥有特定附魔
                    ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                        @Nullable ResourceKey<Enchantment> enchantmentKey = entry.getKey().getKey();
                        if (enchantmentKey != null && enchantmentKey.equals(OLEnchantments.FPS_KILLER)) {
                            event.setCanceled(true);
                            itemStack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(itemStack));
                            if (player.level().isClientSide){
                                int fps = Minecraft.getInstance().getFps();
                                fpsClientC2S(fps, target);
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
