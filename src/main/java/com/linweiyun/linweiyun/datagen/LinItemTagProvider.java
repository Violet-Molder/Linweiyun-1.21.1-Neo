package com.linweiyun.linweiyun.datagen;

import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.tags.LinEnchantmentTags;
import com.linweiyun.linweiyun.tags.LinItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class LinItemTagProvider  extends ItemTagsProvider {
    public LinItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTags, modId, existingFileHelper);
    }



    @Override
    protected void addTags(HolderLookup.Provider provider) {

    }
}
