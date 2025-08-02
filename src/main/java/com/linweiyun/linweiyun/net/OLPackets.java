package com.linweiyun.linweiyun.net;


import com.linweiyun.linweiyun.Linweiyun;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Linweiyun.MOD_ID)
public class OLPackets {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar(Linweiyun.MOD_ID).executesOn(HandlerThread.NETWORK);
        registrar.playBidirectional(
                OLFpsKillerC2SPacket.TYPE,
                OLFpsKillerC2SPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        null,//客户端接收到包后调用的方法
                        OLFpsKillerC2SPacket::handle//服务端接收到包后调用的方法
                )
        );

    }
}
