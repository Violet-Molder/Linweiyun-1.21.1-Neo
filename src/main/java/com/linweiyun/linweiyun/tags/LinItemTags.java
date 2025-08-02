package com.linweiyun.linweiyun.tags;

import com.linweiyun.linweiyun.Linweiyun;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public class LinItemTags {
    public static final TagKey<Item> NO_CONFUSION_ENCHANTMENT_TAG = bind("no_confusion");

    private static TagKey<Item> bind(String name) {

        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID,name));
    }
}
