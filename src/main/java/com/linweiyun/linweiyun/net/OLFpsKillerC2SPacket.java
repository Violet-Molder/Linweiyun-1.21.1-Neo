package com.linweiyun.linweiyun.net;


import com.google.common.collect.Multimap;
import com.linweiyun.linweiyun.Config;
import com.linweiyun.linweiyun.Linweiyun;
import com.linweiyun.linweiyun.enchantment.LinEnchantmentHelper;
import com.linweiyun.linweiyun.enchantment.OLEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class OLFpsKillerC2SPacket implements CustomPacketPayload {

    static int fps;

    public int receiveFps;

    //用来回调获取fps的
    private static Consumer<Integer> fpsCallback = null;

    public static void receive(OLFpsKillerC2SPacket data) {
        fps = data.receiveFps;
        //设置回调的参数。如果accept多个可接受的参数，那么在处理的时候会把这几个参数依次带入需要处理的函数分别执行依次
        if (fpsCallback != null){
            fpsCallback.accept(fps);
            fpsCallback = null;
        }
    }

    //回调函数
    public static void getFps(Consumer<Integer> fpsCallback) {
        {
            OLFpsKillerC2SPacket.fpsCallback = fpsCallback;
        }
    }

    public static final Type<OLFpsKillerC2SPacket> TYPE = new Type<OLFpsKillerC2SPacket>(ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "fps_killer_c2s"));

    public static final StreamCodec<FriendlyByteBuf, OLFpsKillerC2SPacket> STREAM_CODEC =
            CustomPacketPayload.codec(OLFpsKillerC2SPacket::write, OLFpsKillerC2SPacket::new);

    private OLFpsKillerC2SPacket(FriendlyByteBuf buf) {
        this.receiveFps = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.receiveFps);
    }

    public OLFpsKillerC2SPacket(int receiveFps, LivingEntity targetEntity) {
        this.receiveFps = receiveFps;

    }

    public static void handle(final OLFpsKillerC2SPacket data, final IPayloadContext context) {

        context.enqueueWork(() -> {
            receive(data);
        });
    }



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
