package com.linweiyun.linweiyun;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(value = Dist.CLIENT)
public class GreenOrbRenderer {
    // 控制是否显示光球
    private static boolean shouldRenderOrb = false;
    // 光球位置
    private static Vec3 orbPosition = Vec3.ZERO;
    // 光球显示时间
    private static long renderStartTime = 0;
    // 光球显示持续时间（毫秒）
    private static final long RENDER_DURATION = 250; // 5秒

    private static final ResourceLocation ORB_TEXTURE = ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "textures/entity/green_orb.png");
    /**
     * 显示光球在玩家面前
     * @param player 当前玩家
     */
    private static float orbYaw = 0.0f;
    private static float orbPitch = 0.0f;
    public static void showOrb(Player player) {
        // 计算玩家视线方向前2格的位置
        Vec3 lookVec = player.getLookAngle();
        orbPosition = player.position().add(0, player.getEyeHeight(), 0).add(lookVec.scale(2.0));
        shouldRenderOrb = true;
        renderStartTime = System.currentTimeMillis();

        // 保存光球的固定朝向（面向玩家）
        orbYaw = (float) Math.atan2(-lookVec.x, -lookVec.z);
        orbPitch = (float) Math.asin(lookVec.y);
    }

    /**
     * 渲染世界阶段事件处理
     * @param event 渲染事件
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 只在透明度渲染阶段绘制光球
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || !shouldRenderOrb) {
            return;
        }

        // 检查是否超过显示时间
        if (System.currentTimeMillis() - renderStartTime > RENDER_DURATION) {
            shouldRenderOrb = false;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // 获取摄像机位置和投影矩阵
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        // 计算光球相对于摄像机的位置
        float relX = (float) (orbPosition.x - cameraPos.x);
        float relY = (float) (orbPosition.y - cameraPos.y);
        float relZ = (float) (orbPosition.z - cameraPos.z);

        // 准备渲染状态
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        // 绑定纹理
        RenderSystem.setShaderTexture(0, ORB_TEXTURE);
        // 设置纹理环绕方式
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        // 设置绿色光球颜色 (RGBA)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // 绿色半透明

        // 获取着色器
        RenderSystem.setShader(GameRenderer::getPositionTexShader);


        RenderSystem.applyModelViewMatrix();


        // 构建球体顶点数据，使用固定的朝向
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        drawSphere(bufferBuilder, relX, relY, relZ, 0.5f, orbYaw, orbPitch); // 半径为0.5的球体，使用固定的朝向
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);  // 允许相等深度的像素通过
    }

    /**
     * 绘制球体
     * @param bufferBuilder 顶点缓冲构建器
     * @param x 球心X坐标（相对于摄像机）
     * @param y 球心Y坐标（相对于摄像机）
     * @param z 球心Z坐标（相对于摄像机）
     * @param radius 球体半径
     */
    private static void drawSphere(BufferBuilder bufferBuilder, float x, float y, float z, float radius, float yaw, float pitch) {
        int segments = 20; // 球体细分程度

        // 通过经纬度方式构建球体面
        for (int i = 0; i < segments; i++) {
            for (int j = 0; j < segments; j++) {
                // 计算经纬度角度
                float theta1 = (float) (i * Math.PI / segments);
                float theta2 = (float) ((i + 1) * Math.PI / segments);
                float phi1 = (float) (j * 2 * Math.PI / segments);
                float phi2 = (float) ((j + 1) * 2 * Math.PI / segments);

                // 计算四个顶点坐标
                float[] v1 = sphereVertex(theta1, phi1, radius);
                float[] v2 = sphereVertex(theta1, phi2, radius);
                float[] v3 = sphereVertex(theta2, phi1, radius);
                float[] v4 = sphereVertex(theta2, phi2, radius);

                // 计算纹理坐标
                float u1 = (float) j / segments;
                float u2 = (float) (j + 1) / segments;
                float v1_tex = (float) i / segments;
                float v2_tex = (float) (i + 1) / segments;

                // 应用固定的朝向旋转
                float[] rotatedV1 = rotateVertex(v1, yaw, pitch);
                float[] rotatedV2 = rotateVertex(v2, yaw, pitch);
                float[] rotatedV3 = rotateVertex(v3, yaw, pitch);
                float[] rotatedV4 = rotateVertex(v4, yaw, pitch);

                // 绘制两个三角形构成一个四边形面片
                bufferBuilder.addVertex(x + rotatedV1[0], y + rotatedV1[1], z + rotatedV1[2]).setUv(u1, v1_tex);
                bufferBuilder.addVertex(x + rotatedV2[0], y + rotatedV2[1], z + rotatedV2[2]).setUv(u2, v1_tex);
                bufferBuilder.addVertex(x + rotatedV3[0], y + rotatedV3[1], z + rotatedV3[2]).setUv(u1, v2_tex);

                bufferBuilder.addVertex(x + rotatedV3[0], y + rotatedV3[1], z + rotatedV3[2]).setUv(u1, v2_tex);
                bufferBuilder.addVertex(x + rotatedV2[0], y + rotatedV2[1], z + rotatedV2[2]).setUv(u2, v1_tex);
                bufferBuilder.addVertex(x + rotatedV4[0], y + rotatedV4[1], z + rotatedV4[2]).setUv(u2, v2_tex);
            }
        }
    }

    // 添加顶点旋转方法
    private static float[] rotateVertex(float[] vertex, float yaw, float pitch) {
        float x = vertex[0];
        float y = vertex[1];
        float z = vertex[2];

        // 应用yaw旋转(Y轴)
        float cosYaw = (float) Math.cos(yaw);
        float sinYaw = (float) Math.sin(yaw);
        float newX = x * cosYaw - z * sinYaw;
        float newZ = x * sinYaw + z * cosYaw;
        x = newX;
        z = newZ;

        // 应用pitch旋转(X轴)
        float cosPitch = (float) Math.cos(pitch);
        float sinPitch = (float) Math.sin(pitch);
        float newY = y * cosPitch - z * sinPitch;
        newZ = y * sinPitch + z * cosPitch;
        y = newY;
        z = newZ;

        return new float[]{x, y, z};
    }

    /**
     * 根据球坐标计算笛卡尔坐标
     * @param theta 极角（从Z轴测量）
     * @param phi 方位角（XY平面内的角度）
     * @param radius 球体半径
     * @return 顶点的[x, y, z]坐标数组
     */
    private static float[] sphereVertex(float theta, float phi, float radius) {
        float x = radius * (float) (Math.sin(theta) * Math.cos(phi));
        float y = radius * (float) Math.cos(theta);
        float z = radius * (float) (Math.sin(theta) * Math.sin(phi));
        return new float[]{x, y, z};
    }
}
