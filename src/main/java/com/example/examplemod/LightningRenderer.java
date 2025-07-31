package com.example.examplemod;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(value = Dist.CLIENT)
public class LightningRenderer {
    // 控制是否显示闪电
    private static boolean shouldRenderLightning = false;
    // 闪电起点和终点
    private static Vec3 startPoint = Vec3.ZERO;
    private static Vec3 endPoint = Vec3.ZERO;
    // 闪电显示时间
    private static long renderStartTime = 0;
    // 闪电显示持续时间（毫秒）
    private static final long RENDER_DURATION = 2000; // 2秒

    // 生成的闪电节点
    private static List<Vec3> lightningNodes = new ArrayList<>();
    private static final Random random = new Random();

    /**
     * 显示闪电特效从玩家手部发出
     * @param player 当前玩家
     */
    public static void showLightning(Player player) {

        // 计算玩家视线方向前10格的位置
        Vec3 lookVec = player.getLookAngle();
        startPoint = player.position().add(0, player.getEyeHeight() - 0.2, 0).add(lookVec.scale(1.0)); // 从玩家眼部稍下方开始
        endPoint = startPoint.add(lookVec.scale(10.0)); // 延伸10格

        // 生成闪电节点
        generateLightningNodes();

        shouldRenderLightning = true;
        renderStartTime = System.currentTimeMillis();
    }

    /**
     * 生成闪电的节点路径
     */
    private static void generateLightningNodes() {

        lightningNodes.clear();
        lightningNodes.add(startPoint);

        Vec3 direction = endPoint.subtract(startPoint);
        double length = direction.length();
        int segments = (int) (length * 2); // 每格2个节点

        Vec3 currentPoint = startPoint;
        for (int i = 1; i < segments; i++) {
            // 计算直线上的点
            Vec3 straightPoint = startPoint.add(direction.scale((double) i / segments));

            // 添加随机偏移，形成闪电效果
            double offsetStrength = 0.3 * Math.sin(Math.PI * i / segments); // 中间偏移最大
            Vec3 offset = new Vec3(
                    (random.nextFloat() - 0.5) * offsetStrength,
                    (random.nextFloat() - 0.5) * offsetStrength,
                    (random.nextFloat() - 0.5) * offsetStrength
            );

            currentPoint = straightPoint.add(offset);
            lightningNodes.add(currentPoint);
        }

        lightningNodes.add(endPoint);
    }

    /**
     * 渲染世界阶段事件处理
     * @param event 渲染事件
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
//        System.out.println(1);
        // 只在透明度渲染阶段绘制闪电
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || !shouldRenderLightning) {
            return;
        }

        // 检查是否超过显示时间
        if (System.currentTimeMillis() - renderStartTime > RENDER_DURATION) {
            shouldRenderLightning = false;
            lightningNodes.clear();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // 获取摄像机位置
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

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
        RenderSystem.disableDepthTest(); // 确保闪电总是可见

        // 设置闪电颜色 (蓝白色)
        float time = (System.currentTimeMillis() - renderStartTime) / 1000.0f;
//        float alpha = 1.0f - (time / (RENDER_DURATION / 1000.0f)); // 随时间逐渐消失
        float flicker = 0.8f + 0.2f * (float) Math.sin(System.currentTimeMillis() * 0.01); // 闪烁效果

        RenderSystem.setShaderColor(0.6f * flicker, 0.8f * flicker, 1.0f * flicker, 1.0f); // 蓝白色

        // 获取着色器
        RenderSystem.setShader(GameRenderer::getPositionShader);

        // 绘制闪电
        drawLightning(cameraPos);

        // 恢复渲染状态
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
    /**
     * 绘制闪电特效
     * @param cameraPos 摄像机位置
     */

    private static void drawLightning(Vec3 cameraPos) {
        if (lightningNodes.size() < 2) return;

        RenderSystem.lineWidth(5.0f);

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        // 绘制一条简单的线来测试
        if (lightningNodes.size() >= 2) {
            Vec3 start = lightningNodes.get(0);
            Vec3 end = lightningNodes.get(1);

            float startX = (float) (start.x - cameraPos.x);
            float startY = (float) (start.y - cameraPos.y);
            float startZ = (float) (start.z - cameraPos.z);

            float endX = (float) (end.x - cameraPos.x);
            float endY = (float) (end.y - cameraPos.y);
            float endZ = (float) (end.z - cameraPos.z);

            bufferBuilder.addVertex(startX, startY, startZ).setColor(0.0f, 1.0f, 1.0f, 1.0f); // 青色
            bufferBuilder.addVertex(endX, endY, endZ).setColor(0.0f, 1.0f, 1.0f, 1.0f);     // 青色
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.lineWidth(1.0f);
    }

//    private static void drawLightning(Vec3 cameraPos) {
//        if (lightningNodes.size() < 2) return;
//
//        RenderSystem.lineWidth(4.0f); // 设置线条宽度
//
//        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
//
//        // 绘制主闪电链
//        for (int i = 0; i < lightningNodes.size() - 1; i++) {
//            Vec3 start = lightningNodes.get(i);
//            Vec3 end = lightningNodes.get(i + 1);
//
//            float startX = (float) (start.x - cameraPos.x);
//            float startY = (float) (start.y - cameraPos.y);
//            float startZ = (float) (start.z - cameraPos.z);
//
//            float endX = (float) (end.x - cameraPos.x);
//            float endY = (float) (end.y - cameraPos.y);
//            float endZ = (float) (end.z - cameraPos.z);
//
//            // 添加带颜色的顶点
//            bufferBuilder.addVertex(startX, startY, startZ).setColor(0.6f, 0.8f, 1.0f, 1.0f).setNormal(0, 0, 1);
//            bufferBuilder.addVertex(endX, endY, endZ).setColor(0.6f, 0.8f, 1.0f, 1.0f).setNormal(0, 0, 1);
//        }
//
//        // 绘制分支闪电（随机添加一些分支）
//        for (int i = 1; i < lightningNodes.size() - 1; i++) {
//            if (random.nextFloat() < 0.3f) { // 30%概率生成分支
//                Vec3 mainPoint = lightningNodes.get(i);
//                Vec3 direction = lightningNodes.get(i + 1).subtract(lightningNodes.get(i));
//
//                // 创建分支方向（从主闪电稍微偏移）
//                Vec3 branchDirection = direction.normalize()
//                        .add(new Vec3(
//                                random.nextFloat() - 0.5,
//                                random.nextFloat() - 0.5,
//                                random.nextFloat() - 0.5
//                        ).scale(0.5))
//                        .normalize();
//
//                Vec3 branchEnd = mainPoint.add(branchDirection.scale(0.5 + random.nextFloat() * 1.0));
//
//                float startX = (float) (mainPoint.x - cameraPos.x);
//                float startY = (float) (mainPoint.y - cameraPos.y);
//                float startZ = (float) (mainPoint.z - cameraPos.z);
//
//                float endX = (float) (branchEnd.x - cameraPos.x);
//                float endY = (float) (branchEnd.y - cameraPos.y);
//                float endZ = (float) (branchEnd.z - cameraPos.z);
//
//                // 添加带颜色的顶点
//                bufferBuilder.addVertex(startX, startY, startZ).setColor(0.6f, 0.8f, 1.0f, 1.0f).setNormal(0, 0, 1);
//                bufferBuilder.addVertex(endX, endY, endZ).setColor(0.6f, 0.8f, 1.0f, 1.0f).setNormal(0, 0, 1);
//            }
//        }
//
//        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
//        RenderSystem.lineWidth(1.0f); // 恢复线条宽度
//    }
}
