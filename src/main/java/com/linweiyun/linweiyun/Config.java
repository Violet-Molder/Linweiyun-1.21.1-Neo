package com.linweiyun.linweiyun;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@Mod(Linweiyun.MOD_ID)
@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public  class Config
{
    public static class CommonConfig {
        public final ModConfigSpec.BooleanValue INVERT_ENCHANTMENT_USAGE;
        public final ModConfigSpec.IntValue MAX_FPS_KILLER_DAMAGE;
        public final ModConfigSpec.DoubleValue DAMAGE_PER_FRAME;
        public final ModConfigSpec.IntValue START_FRAME;

        CommonConfig(ModConfigSpec.Builder builder) {
            builder.comment("Linweiyun common configuration settings")
                    .push("general");

            INVERT_ENCHANTMENT_USAGE = builder
                    .comment("Whether to invert enchantment usage")
                    .translation("config.linweiyun.invert_enchantment_usage")
                    .define("invert_enchantment_usage", false);

            MAX_FPS_KILLER_DAMAGE = builder
                    .comment("Maximum damage for FPS killer")
                    .translation("config.linweiyun.max_fps_killer_damage")
                    .defineInRange("max_fps_killer_damage", 15, 1, 100);

            DAMAGE_PER_FRAME = builder
                    .comment("Damage per frame")
                    .translation("config.linweiyun.damage_per_frame")
                    .defineInRange("damage_per_frame", 0.3d, 0.0d, 10.0d);

            START_FRAME = builder
                    .comment("Starting frame value")
                    .translation("config.linweiyun.start_frame")
                    .defineInRange("start_frame", 5, 1, 100);

            builder.pop();
        }
    }

    public static final CommonConfig COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    // 公共静态变量用于方便访问配置值
    public static boolean invertEnchantmentUsage;
    public static int maxFpsKillerDamage;
    public static double damagePerFrame;
    public static int startFrame;

    static {
        final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            invertEnchantmentUsage = COMMON.INVERT_ENCHANTMENT_USAGE.get();
            maxFpsKillerDamage = COMMON.MAX_FPS_KILLER_DAMAGE.get();
            damagePerFrame = COMMON.DAMAGE_PER_FRAME.get();
            startFrame = COMMON.START_FRAME.get();
        }
    }
}
