package com.linweiyun.linweiyun;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Linweiyun.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static ModConfigSpec.BooleanValue INVERT_ENCHANTMENT_USAGE = BUILDER
            .translation("config.linweiyun.invert_enchantment_usage")
            .define("invert_enchantment_usage", false);
    public static ModConfigSpec.ConfigValue<Integer> MAX_FPS_KILLER_DAMAGE = BUILDER
            .translation("config.linweiyun.max_fps_killer_damage")
            .define("max_fps_killer_damage", 15);
    public static ModConfigSpec.ConfigValue<Float> DAMAGE_PER_FRAME = BUILDER
            .translation("config.linweiyun.damage_per_frame")
            .define("damage_per_frame", 0.3f);
    public static ModConfigSpec.ConfigValue<Integer> START_FRAME = BUILDER
            .translation("config.linweiyun.start_frame")
            .define("start_frame", 5);

    public static boolean invertEnchantmentUsage;
    public static int maxFpsKillerDamage;
    public static float damagePerFrame;
    public static int startFrame;
    static final ModConfigSpec SPEC = BUILDER.build();

//    public static Set<Item> items;
//    // a list of strings that are treated as resource locations for items
//    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
//            .comment("A list of items to log on common setup.")
//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);
//
//
//    private static boolean validateItemName(final Object obj)
//    {
//        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
//    }
//
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        invertEnchantmentUsage = INVERT_ENCHANTMENT_USAGE.get();
        maxFpsKillerDamage = MAX_FPS_KILLER_DAMAGE.get();
        damagePerFrame = DAMAGE_PER_FRAME.get();
        startFrame = START_FRAME.get();

//        // convert the list of strings into a set of items
//        items = ITEM_STRINGS.get().stream()
//                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
//                .collect(Collectors.toSet());
    }
}
