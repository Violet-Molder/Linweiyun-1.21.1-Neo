package com.linweiyun.linweiyun.events;

import com.linweiyun.linweiyun.Linweiyun;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;


@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class onEntityPreAttack {


    private static float oriDamage;
    private static float newDamage;
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (!player.level().isClientSide) {
                newDamage = event.getNewDamage();
                oriDamage = event.getOriginalDamage();
            }
        }
    }

    public static float getNewDamage() {
        return newDamage;
    }
    public static float getOriDamage() {
        return oriDamage;
    }
}
