package com.linweiyun.linweiyun.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record FPSKillerFpsEnchantmentEffect() implements EnchantmentValueEffect {
    public static final MapCodec<FPSKillerFpsEnchantmentEffect> CODEC = MapCodec.unit(new FPSKillerFpsEnchantmentEffect());



    @Override
    public float process(int enchantmentLevel, RandomSource random, float value) {
        return 0;
    }

    @Override
    public MapCodec<? extends EnchantmentValueEffect> codec() {
        return CODEC;
    }



}
