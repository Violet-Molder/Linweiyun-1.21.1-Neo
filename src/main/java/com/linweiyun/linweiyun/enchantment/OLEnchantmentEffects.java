package com.linweiyun.linweiyun.enchantment;


import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.enchantment.custom.FPSKillerFpsEnchantmentEffect;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class OLEnchantmentEffects {
    public static final DeferredRegister<MapCodec<? extends EnchantmentValueEffect>> VALUE_ENCHANTMENT_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_VALUE_EFFECT_TYPE, Linweiyun.MOD_ID);


    public static final Supplier<MapCodec<? extends EnchantmentValueEffect>> FPS_KILLER =
            VALUE_ENCHANTMENT_EFFECTS.register("fps_killer", () -> FPSKillerFpsEnchantmentEffect.CODEC);

    public static void register(IEventBus eventBus){
        VALUE_ENCHANTMENT_EFFECTS.register(eventBus);
    }
}
