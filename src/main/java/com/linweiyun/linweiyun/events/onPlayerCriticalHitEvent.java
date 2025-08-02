package com.linweiyun.linweiyun.events;

import com.linweiyun.linweiyun.Linweiyun;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;


@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class onPlayerCriticalHitEvent {
    private static boolean isCriticalHit = false;

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        isCriticalHit = event.isVanillaCritical();
    }

    public static boolean isCriticalHit() {
        return isCriticalHit;
    }
}
