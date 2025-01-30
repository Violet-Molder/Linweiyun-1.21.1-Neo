package com.linweiyun.linweiyun.client;

import com.linweiyun.linweiyun.Linweiyun;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod(value = Linweiyun.MOD_ID, dist = Dist.CLIENT)
public class ClientLinweiyun {
    public ClientLinweiyun(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
