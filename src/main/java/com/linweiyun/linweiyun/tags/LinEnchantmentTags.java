package com.linweiyun.linweiyun.tags;

import com.linweiyun.linweiyun.Linweiyun;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class LinEnchantmentTags {
    public static final TagKey<Enchantment> FPS_KILLER_ENCHANTMENT_TAG = bind("fps_killer");
    public static final TagKey<Enchantment> KINDNESS_ENCHANTMENT_TAG = bind("kindness");
    public static final TagKey<Enchantment> NO_CONFUSION_ENCHANTMENT_TAG = bind("no_confusion");

    private static TagKey<Enchantment> bind(String name) {

        return TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID,name));
    }
}
