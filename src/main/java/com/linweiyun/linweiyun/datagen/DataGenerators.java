
package com.linweiyun.linweiyun.datagen;


import com.linweiyun.linweiyun.Linweiyun;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        TagsProvider<Block> blockTags = generator.addProvider(event.includeServer(), new LinBlockTagProvider(packOutput, lookupProvider,Linweiyun.MOD_ID ,existingFileHelper));
        generator.addProvider(event.includeServer(), new OLDatapackProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new LinItemTagProvider(packOutput, lookupProvider,blockTags.contentsGetter(),Linweiyun.MOD_ID, existingFileHelper));
    }
}
