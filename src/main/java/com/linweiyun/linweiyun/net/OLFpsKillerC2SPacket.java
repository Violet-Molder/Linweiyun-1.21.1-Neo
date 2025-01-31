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

public class OLFpsKillerC2SPacket implements CustomPacketPayload {
    private int targetEntityId;

    static int fps = 0;

    public int receiveFps = 0;

    public static void receive(OLFpsKillerC2SPacket data) {
        fps = data.receiveFps;

    }

    public static int getFps() {
        return fps;
    }

    public static final Type<OLFpsKillerC2SPacket> TYPE = new Type<OLFpsKillerC2SPacket>(ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "fps_killer_c2s"));

    public static final StreamCodec<FriendlyByteBuf, OLFpsKillerC2SPacket> STREAM_CODEC =
            CustomPacketPayload.codec(OLFpsKillerC2SPacket::write, OLFpsKillerC2SPacket::new);

    private OLFpsKillerC2SPacket(FriendlyByteBuf buf) {
        this.receiveFps = buf.readInt();
        this.targetEntityId = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.receiveFps);
        buf.writeInt(this.targetEntityId);
    }

    public OLFpsKillerC2SPacket(int receiveFps, LivingEntity targetEntity) {
        this.receiveFps = receiveFps;
        this.targetEntityId = targetEntity.getId();

    }

    public static void handle(final OLFpsKillerC2SPacket data, final IPayloadContext context) {
        fps = data.receiveFps;
        int targetEntityId = data.targetEntityId;
        Player player = context.player();
        if (player != null && player.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(targetEntityId);
            if (entity instanceof LivingEntity livingEntity) {
                // 处理目标实体
                // 例如，对目标实体造成伤害
                ItemStack itemStack = player.getMainHandItem();
                itemStack.getItem().getAttackDamageBonus(livingEntity, 1, player.getLastDamageSource());
                float weaponDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float damage;
                Object2IntMap.Entry<Holder<Enchantment>> itemEnchantment = LinEnchantmentHelper.getEnchantment(OLEnchantments.FPS_KILLER, itemStack);
                if (itemEnchantment != null){
                    int enchantmentLevel = itemEnchantment.getIntValue();
                    if (!Config.invertEnchantmentUsage){
                        if (fps <= Config.startFrame){
                            damage = weaponDamage + enchantmentLevel + Config.maxFpsKillerDamage ;
                        } else {
                            damage = weaponDamage + enchantmentLevel +Config.maxFpsKillerDamage - Config.damagePerFrame * (fps - Config.startFrame);
                        }

                    } else {
                        if (fps <= Config.startFrame){
                            damage = 1 + enchantmentLevel ;
                        } else if (( Config.damagePerFrame * (fps - Config.startFrame)) >= Config.maxFpsKillerDamage ){
                            damage = weaponDamage + enchantmentLevel + Config.maxFpsKillerDamage ;
                        }
                        else {
                            damage = weaponDamage + enchantmentLevel + Config.damagePerFrame * (fps - Config.startFrame);
                        }
                    }
                    if (damage > 0) {
                        livingEntity.hurt(player.damageSources().playerAttack(player), damage);
                    } else {
                        livingEntity.heal(damage);
                    }
                }

            }
        }
        context.enqueueWork(() -> {
            fps = data.receiveFps;
        });
    }



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
