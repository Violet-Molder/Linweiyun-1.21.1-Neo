package com.linweiyun.linweiyun.enchantment;


import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.enchantment.custom.FPSKillerFpsEnchantmentEffect;
import com.linweiyun.linweiyun.tags.LinEnchantmentTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

public class OLEnchantments {
    public static final ResourceKey<Enchantment> FPS_KILLER = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "fps_killer"));
    public static final ResourceKey<Enchantment> KINDNESS = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "kindness"));
    public static final ResourceKey<Enchantment> NEWTON_THIRD_LAW = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "newton_third_law"));
    public static final ResourceKey<Enchantment> CONFUSION = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "confusion"));
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var enchantments = context.lookup(Registries.ENCHANTMENT);
        var items = context.lookup(Registries.ITEM);

        register(context, FPS_KILLER, Enchantment.enchantment(Enchantment.definition(
            items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
            items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
            3,
            5,
            Enchantment.dynamicCost(1, 5),
            Enchantment.dynamicCost(5, 5),
            2,
            EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.DAMAGE, new FPSKillerFpsEnchantmentEffect())
        );
        register(context, KINDNESS, Enchantment.enchantment(Enchantment.definition(
            items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
            items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
            3,
            1,
            Enchantment.dynamicCost(1, 5),
            Enchantment.dynamicCost(5, 5),
            2,
            EquipmentSlotGroup.MAINHAND))
        );
        register(context, NEWTON_THIRD_LAW, Enchantment.enchantment(Enchantment.definition(
            items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
            items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
            3,
            5,
            Enchantment.dynamicCost(1, 5),
            Enchantment.dynamicCost(5, 5),
            2,
            EquipmentSlotGroup.MAINHAND))

        );
        register(context, CONFUSION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.EQUIPPABLE_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                3,
                1,
                Enchantment.dynamicCost(1, 5),
                Enchantment.dynamicCost(5, 5),
                2,
                EquipmentSlotGroup.MAINHAND))
        );
    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key,
                                 Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }
}
