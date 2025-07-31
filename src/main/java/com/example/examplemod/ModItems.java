package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // 创建一个延迟注册器，用于注册物品
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("examplemod");

    // 注册绿色光球物品
    public static final DeferredItem<GreenOrbItem> GREEN_ORB = ITEMS.register("green_orb",
            () -> new GreenOrbItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}