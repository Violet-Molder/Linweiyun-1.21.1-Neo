package com.linweiyun.linweiyun;

import com.linweiyun.linweiyun.enchantment.OLEnchantmentEffects;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(Linweiyun.MOD_ID)
public class Linweiyun {
    public static final String MOD_ID = "linweiyun";
    public static final Logger LOGGER = LogUtils.getLogger();
    public Linweiyun(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        OLEnchantmentEffects.register(modEventBus);

        ModItems.register(modEventBus);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.GREEN_ORB);
        }
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
