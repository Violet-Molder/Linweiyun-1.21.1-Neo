package com.example.examplemod;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class LineRendererTest {
    // 控制是否显示矩形体
    // 用于控制闪电渲染的开关，当为true时才会进行渲染
    private static boolean shouldRenderRect = false;

    // 矩形体位置
    // 存储闪电渲染的位置坐标，初始值为零向量
    private static Vec3 rectPosition = Vec3.ZERO;

    // 矩形体显示时间
    // 记录闪电开始渲染的时间戳，用于计算渲染持续时间
    private static long renderStartTime = 0;

    // 矩形体显示持续时间（毫秒）
    // 定义闪电渲染的总持续时间，7000毫秒=7秒
    private static final long RENDER_DURATION = 7000; // 5秒

    // 矩形体的朝向
    // 存储矩形体的欧拉角旋转信息（yaw偏航角和pitch俯仰角）
    private static float rectYaw = 0.0f;   // 偏航角（绕Y轴旋转）
    private static float rectPitch = 0.0f; // 俯仰角（绕X轴旋转）

    // 在类的字段部分添加新的变量
    // 存储矩形体的局部坐标系基向量，用于确定渲染朝向
    private static float[] rectRight = {1.0f, 0.0f, 0.0f}; // 矩形的右向量（X轴正方向）
    private static float[] rectUp = {0.0f, 1.0f, 0.0f};    // 矩形的上向量（Y轴正方向）
    private static float currentLength = 10.0f; // 默认长度，控制闪电的总长度

    /**
     * 显示矩形体在玩家面前
     * 该方法用于在玩家视线方向生成并显示闪电效果
     *
     * @param player 当前玩家对象，用于获取玩家位置和视线方向
     * @param length 闪电的长度参数，控制生成闪电的总长度
     */
    public static void showRect(Player player, float length) {
        // 保存实际长度参数，供后续渲染使用
        currentLength = length;

        // 计算玩家视线方向前2格的位置
        // getLookAngle()获取玩家的视线单位向量
        // position()获取玩家当前位置，add(0, player.getEyeHeight() - 0.3, 0)将位置调整到眼睛高度略下方
        // lookVec.scale(-0.2)沿着视线反方向偏移0.2个单位（使闪电出现在玩家面前）
        Vec3 lookVec = player.getLookAngle();
        rectPosition = player.position().add(0, player.getEyeHeight() - 0.3, 0).add(lookVec.scale(-0.2));

        // 开启渲染标志，并记录开始时间
        shouldRenderRect = true;
        renderStartTime = System.currentTimeMillis();

        // 计算并保存矩形的固定朝向基向量（在召唤时确定）
        // 获取玩家的视角方向向量的各分量
        float lookX = (float) lookVec.x;
        float lookY = (float) lookVec.y;
        float lookZ = (float) lookVec.z;

        // 标准化视线向量，确保其为单位向量
        float lookLength = (float) Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
        if (lookLength > 0.0000f) {
            lookX /= lookLength;
            lookY /= lookLength;
            lookZ /= lookLength;
        }

        // 使用四元数方法创建正交基向量，避免万向节锁定问题
        // 创建视线方向向量作为前向量
        Vector3f forward = new org.joml.Vector3f(lookX, lookY, lookZ);

        // 选择合适的向上向量作为参考，初始使用世界坐标的上方向(0,1,0)
        Vector3f tempUp = new org.joml.Vector3f(0.0f, 1.0f, 0.0f);

        // 计算右向量 = forward × tempUp（向量叉积）
        // 右向量垂直于前向量和参考上向量构成的平面
        Vector3f right = new org.joml.Vector3f();
        forward.cross(tempUp, right);

        // 检查右向量长度，如果太小说明forward接近与tempUp平行（如直视天空或地面）
        if (right.length() < 0.0001f) {
            // 改用世界Z轴作为参考，避免数值不稳定
            tempUp.set(0.0f, 0.0f, 1.0f);
            forward.cross(tempUp, right);
        }

        // 归一化右向量，确保其为单位向量
        if (right.length() > 0.0000f) {
            right.normalize();
        } else {
            // 最后的后备方案，使用默认右向量
            right.set(1.0f, 0.0f, 0.0f);
        }

        // 保存计算得到的右向量分量
        rectRight = new float[]{right.x, right.y, right.z};

        // 计算上向量 = forward × right（再次使用叉积确保正交性）
        Vector3f up = new Vector3f();
        forward.cross(right, up);

        // 归一化上向量
        if (up.length() > 0.0000f) {
            up.normalize();
        }

        // 保存计算得到的上向量分量
        rectUp = new float[]{up.x, up.y, up.z};
    }

    /**
     * 渲染世界阶段事件处理
     * Minecraft渲染系统的事件回调方法，在特定渲染阶段被调用
     *
     * @param event 渲染事件对象，包含渲染相关的信息和上下文
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 只在透明度渲染阶段绘制矩形体，提高渲染效率
        // AFTER_TRANSLUCENT_BLOCKS阶段用于渲染半透明物体
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || !shouldRenderRect) {
            return;
        }

        // 检查是否超过显示时间，超过则关闭渲染
        if (System.currentTimeMillis() - renderStartTime > RENDER_DURATION) {
            shouldRenderRect = false;
            return;
        }

        // 获取Minecraft客户端实例和玩家对象
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // 获取摄像机位置，用于计算相对坐标
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        // 计算矩形体相对于摄像机的位置，用于视图空间渲染
        float relX = (float) (rectPosition.x - cameraPos.x);
        float relY = (float) (rectPosition.y - cameraPos.y);
        float relZ = (float) (rectPosition.z - cameraPos.z);

        // 准备渲染状态设置
        RenderSystem.enableBlend(); // 启用混合模式，支持透明度
        // 设置混合函数：源颜色*源Alpha + 目标颜色*(1-源Alpha)
        RenderSystem.blendFuncSeparate(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.depthMask(false);  // 禁用深度写入，避免透明物体遮挡问题
        RenderSystem.disableCull();     // 禁用背面剔除，确保所有面都能渲染

        // 设置矩形体颜色 (RGBA) - 绿色半透明
        RenderSystem.setShaderColor(0.0f, 1.0f, 0.0f, 0.7f);

        // 获取位置着色器，用于渲染3D几何体
        RenderSystem.setShader(GameRenderer::getPositionShader);

        // 构建矩形体顶点数据，使用固定的朝向
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
        // 调用绘制方法，传入相对坐标和旋转参数
        drawRect(bufferBuilder, relX, relY, relZ, rectYaw, rectPitch);
        // 提交顶点数据并执行渲染
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        // 恢复渲染状态
        RenderSystem.disableBlend();    // 禁用混合
        RenderSystem.depthMask(true);   // 启用深度写入
        RenderSystem.enableCull();      // 启用背面剔除
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // 恢复默认颜色
        RenderSystem.enableDepthTest(); // 启用深度测试
    }

    /**
     * 绘制矩形体（实际绘制闪电效果）
     * 该方法负责构建闪电的几何形状和顶点数据
     *
     * @param bufferBuilder 顶点缓冲构建器，用于收集顶点数据
     * @param x             矩形体中心X坐标（相对于摄像机）
     * @param y             矩形体中心Y坐标（相对于摄像机）
     * @param z             矩形体中心Z坐标（相对于摄像机）
     * @param yaw           矩形体偏航角（绕Y轴旋转角度）
     * @param pitch         矩形体俯仰角（绕X轴旋转角度）
     */
    private static void drawRect(BufferBuilder bufferBuilder, float x, float y, float z, float yaw, float pitch) {
        // 设置矩形体颜色 (RGBA) - 蓝黄色闪电效果
        RenderSystem.setShaderColor(0.5f, 0.8f, 1.0f, 1.0f); // 蓝白色不透明

        // 长方体的三个维度大小参数
        float width = 0.1f;   // 宽度（窄边）
        float height = 0.7f;  // 起始高度（闪电起始端的高度）
        float endHeight = 0.0f; // 终点高度（闪电结束端的高度，逐渐变细）
        float length = currentLength; // 长度（长边，由外部传入的长度参数决定）

        // 计算垂直于rectRight和rectUp平面的向量（长度方向）
        // 通过右向量和上向量的叉积得到前向量（长度延伸方向）
        org.joml.Vector3f rightVec = new Vector3f(rectRight[0], rectRight[1], rectRight[2]);
        org.joml.Vector3f upVec = new Vector3f(rectUp[0], rectUp[1], rectUp[2]);
        org.joml.Vector3f forwardVec = new Vector3f();
        rightVec.cross(upVec, forwardVec); // 得到垂直于平面的向量

        // 定义固定偏移量（向右偏移0.1f单位），使闪电稍微偏向右侧
        float offsetX = rightVec.x * 0.1f;
        float offsetY = rightVec.y * 0.0f;
        float offsetZ = rightVec.z * 0.1f;

        // 创建分段点列表，用于分段绘制闪电
        List<Float> segmentPoints = new ArrayList<>();
        segmentPoints.add(0f); // 添加起点

        // 初始化分段参数
        float currentPos = 0f;
        float baseSegmentLength = 0.4f; // 基础段长度
        float lengthIncrement = 0.25f;   // 每两段增加的长度
        int segmentIndex = 0;

        // 分段长度计算 - 每两段比前两段长指定长度，实现闪电的不规则分段效果
        while (currentPos < length) {
            // 每两段长度相同，逐渐增加长度
            float segmentLength = baseSegmentLength + (segmentIndex / 2) * lengthIncrement;
            segmentLength = Math.min(segmentLength, length - currentPos); // 确保不超过总长度

            currentPos += segmentLength;
            segmentPoints.add(currentPos);
            segmentIndex++;
        }

        // 为每个分段点计算顶点，考虑偏转方向，形成闪电的曲折效果
        java.util.List<float[]> vertices = new java.util.ArrayList<>();

        for (int i = 0; i < segmentPoints.size(); i++) {
            float pos = segmentPoints.get(i);

            // 根据当前位置计算当前高度（从起始高度线性递减到终点高度）
            // 实现闪电从粗到细的视觉效果
            float currentHeight = height - (height - endHeight) * (pos / length);

            // 根据段索引决定偏转方向（奇数段向上偏转，偶数段向下偏转）
            // 创建闪电的曲折效果
            float verticalOffset = 0f;
            if (i % 2 == 1) {
                // 奇数段向上偏转
                verticalOffset = 0.15f;
            } else {
                // 偶数段向下偏转
                verticalOffset = -0.15f;
            }

            // 计算四个顶点位置（基于当前位置、当前高度和偏转方向）
            // 构建矩形截面的四个顶点
            float[] v1 = {
                    // 左下顶点：宽度向左偏移，高度向下偏移，加上垂直偏转和长度方向偏移
                    -rightVec.x * width / 2 - upVec.x * currentHeight / 2 + upVec.x * verticalOffset
                            + forwardVec.x * pos + offsetX,
                    -rightVec.y * width / 2 - upVec.y * currentHeight / 2 + upVec.y * verticalOffset
                            + forwardVec.y * pos + offsetY,
                    -rightVec.z * width / 2 - upVec.z * currentHeight / 2 + upVec.z * verticalOffset
                            + forwardVec.z * pos + offsetZ
            };

            float[] v2 = {
                    // 右下顶点：宽度向右偏移，高度向下偏移
                    rightVec.x * width / 2 - upVec.x * currentHeight / 2 + upVec.x * verticalOffset
                            + forwardVec.x * pos + offsetX,
                    rightVec.y * width / 2 - upVec.y * currentHeight / 2 + upVec.y * verticalOffset
                            + forwardVec.y * pos + offsetY,
                    rightVec.z * width / 2 - upVec.z * currentHeight / 2 + upVec.z * verticalOffset
                            + forwardVec.z * pos + offsetZ
            };

            float[] v3 = {
                    // 右上顶点：宽度向右偏移，高度向上偏移
                    rightVec.x * width / 2 + upVec.x * currentHeight / 2 + upVec.x * verticalOffset
                            + forwardVec.x * pos + offsetX,
                    rightVec.y * width / 2 + upVec.y * currentHeight / 2 + upVec.y * verticalOffset
                            + forwardVec.y * pos + offsetY,
                    rightVec.z * width / 2 + upVec.z * currentHeight / 2 + upVec.z * verticalOffset
                            + forwardVec.z * pos + offsetZ
            };

            float[] v4 = {
                    // 左上顶点：宽度向左偏移，高度向上偏移
                    -rightVec.x * width / 2 + upVec.x * currentHeight / 2 + upVec.x * verticalOffset
                            + forwardVec.x * pos + offsetX,
                    -rightVec.y * width / 2 + upVec.y * currentHeight / 2 + upVec.y * verticalOffset
                            + forwardVec.y * pos + offsetY,
                    -rightVec.z * width / 2 + upVec.z * currentHeight / 2 + upVec.z * verticalOffset
                            + forwardVec.z * pos + offsetZ
            };

            // 添加这四个顶点到列表，按顺序存储
            vertices.add(v1);
            vertices.add(v2);
            vertices.add(v3);
            vertices.add(v4);
        }

        // 绘制连接面，构建完整的闪电几何体
        for (int i = 0; i < segmentPoints.size() - 1; i++) {
            int baseIndex = i * 4; // 每段有4个顶点，计算基础索引

            // 获取当前段和下一段的顶点
            float[] v1_curr = vertices.get(baseIndex);
            float[] v2_curr = vertices.get(baseIndex + 1);
            float[] v3_curr = vertices.get(baseIndex + 2);
            float[] v4_curr = vertices.get(baseIndex + 3);

            float[] v1_next = vertices.get(baseIndex + 4);
            float[] v2_next = vertices.get(baseIndex + 5);
            float[] v3_next = vertices.get(baseIndex + 6);
            float[] v4_next = vertices.get(baseIndex + 7);

            // 前面 (当前段的起始面) - 只绘制第一段的前面
            if (i == 0) {
                // 绘制第一个三角形面
                bufferBuilder.addVertex(x + v1_curr[0], y + v1_curr[1], z + v1_curr[2]);
                bufferBuilder.addVertex(x + v2_curr[0], y + v2_curr[1], z + v2_curr[2]);
                bufferBuilder.addVertex(x + v3_curr[0], y + v3_curr[1], z + v3_curr[2]);

                // 绘制第二个三角形面，与第一个组成矩形
                bufferBuilder.addVertex(x + v3_curr[0], y + v3_curr[1], z + v3_curr[2]);
                bufferBuilder.addVertex(x + v4_curr[0], y + v4_curr[1], z + v4_curr[2]);
                bufferBuilder.addVertex(x + v1_curr[0], y + v1_curr[1], z + v1_curr[2]);
            }

            // 后面 (下一段的结束面) - 只绘制最后一段的后面
            if (i == segmentPoints.size() - 2) {
                // 绘制最后一个面的两个三角形
                bufferBuilder.addVertex(x + v1_next[0], y + v1_next[1], z + v1_next[2]);
                bufferBuilder.addVertex(x + v2_next[0], y + v2_next[1], z + v2_next[2]);
                bufferBuilder.addVertex(x + v3_next[0], y + v3_next[1], z + v3_next[2]);

                bufferBuilder.addVertex(x + v3_next[0], y + v3_next[1], z + v3_next[2]);
                bufferBuilder.addVertex(x + v4_next[0], y + v4_next[1], z + v4_next[2]);
                bufferBuilder.addVertex(x + v1_next[0], y + v1_next[1], z + v1_next[2]);
            }

            // 侧面连接 - 连接相邻段之间的侧面
            // 左面连接
            bufferBuilder.addVertex(x + v1_curr[0], y + v1_curr[1], z + v1_curr[2]);
            bufferBuilder.addVertex(x + v4_curr[0], y + v4_curr[1], z + v4_curr[2]);
            bufferBuilder.addVertex(x + v4_next[0], y + v4_next[1], z + v4_next[2]);

            bufferBuilder.addVertex(x + v4_next[0], y + v4_next[1], z + v4_next[2]);
            bufferBuilder.addVertex(x + v1_next[0], y + v1_next[1], z + v1_next[2]);
            bufferBuilder.addVertex(x + v1_curr[0], y + v1_curr[1], z + v1_curr[2]);

            // 右面连接
            bufferBuilder.addVertex(x + v2_curr[0], y + v2_curr[1], z + v2_curr[2]);
            bufferBuilder.addVertex(x + v2_next[0], y + v2_next[1], z + v2_next[2]);
            bufferBuilder.addVertex(x + v3_next[0], y + v3_next[1], z + v3_next[2]);

            bufferBuilder.addVertex(x + v3_next[0], y + v3_next[1], z + v3_next[2]);
            bufferBuilder.addVertex(x + v3_curr[0], y + v3_curr[1], z + v3_curr[2]);
            bufferBuilder.addVertex(x + v2_curr[0], y + v2_curr[1], z + v2_curr[2]);

            // 上面连接
            bufferBuilder.addVertex(x + v4_curr[0], y + v4_curr[1], z + v4_curr[2]);
            bufferBuilder.addVertex(x + v3_curr[0], y + v3_curr[1], z + v3_curr[2]);
            bufferBuilder.addVertex(x + v3_next[0], y + v3_next[1], z + v3_next[2]);

            bufferBuilder.addVertex(x + v3_next[0], y + v3_next[1], z + v3_next[2]);
            bufferBuilder.addVertex(x + v4_next[0], y + v4_next[1], z + v4_next[2]);
            bufferBuilder.addVertex(x + v4_curr[0], y + v4_curr[1], z + v4_curr[2]);

            // 下面连接
            bufferBuilder.addVertex(x + v1_curr[0], y + v1_curr[1], z + v1_curr[2]);
            bufferBuilder.addVertex(x + v1_next[0], y + v1_next[1], z + v1_next[2]);
            bufferBuilder.addVertex(x + v2_next[0], y + v2_next[1], z + v2_next[2]);

            bufferBuilder.addVertex(x + v2_next[0], y + v2_next[1], z + v2_next[2]);
            bufferBuilder.addVertex(x + v2_curr[0], y + v2_curr[1], z + v2_curr[2]);
            bufferBuilder.addVertex(x + v1_curr[0], y + v1_curr[1], z + v1_curr[2]);
        }

        // 绘制闪电周围的特效，增强视觉效果
        drawLightningEffects(bufferBuilder, x, y, z, segmentPoints, vertices, rightVec, upVec, forwardVec);
    }

    /**
     * 绘制闪电周围的特效
     * 生成围绕主闪电的分支特效，增强闪电的真实感
     *
     * @param bufferBuilder 顶点缓冲构建器，用于收集顶点数据
     * @param x             矩形体中心X坐标（相对于摄像机）
     * @param y             矩形体中心Y坐标（相对于摄像机）
     * @param z             矩形体中心Z坐标（相对于摄像机）
     * @param segmentPoints 分段点列表，主闪电的分段信息
     * @param vertices      顶点列表，主闪电的顶点数据
     * @param rightVec      右向量，闪电坐标系的X轴
     * @param upVec         上向量，闪电坐标系的Y轴
     * @param forwardVec    前向量，闪电坐标系的Z轴（长度方向）
     */
    private static void drawLightningEffects(BufferBuilder bufferBuilder, float x, float y, float z,
                                             java.util.List<Float> segmentPoints, java.util.List<float[]> vertices,
                                             org.joml.Vector3f rightVec, org.joml.Vector3f upVec, org.joml.Vector3f forwardVec) {
        // 生成基于时间的随机种子，使特效具有时间变化效果
        long time = System.currentTimeMillis() / 250; // 减慢变化速度，约0.5秒一跳
        java.util.Random random = new java.util.Random(time);
        float yOffset = -0.1f; // Y轴下移0.1单位，使特效稍微低于主闪电

        int totalEffects = 120; // 总特效数量，控制特效密度

        // 循环生成每个特效
        for (int i = 0; i < totalEffects; i++) {
            // 均匀分布在线段上，计算当前特效在闪电上的位置比例
            float t = (float) i / (totalEffects - 1);
            float pos = t * currentLength; // 计算实际位置

            // 找到该位置对应的线段段落
            int segmentIndex = 0;
            for (int j = 0; j < segmentPoints.size() - 1; j++) {
                if (pos >= segmentPoints.get(j) && pos <= segmentPoints.get(j + 1)) {
                    segmentIndex = j;
                    break;
                }
            }

            // 获取该段的顶点信息
            int baseIndex = segmentIndex * 4;
            float[] v1_curr = vertices.get(baseIndex);
            float[] v2_curr = vertices.get(baseIndex + 1);
            float[] v3_curr = vertices.get(baseIndex + 2);
            float[] v4_curr = vertices.get(baseIndex + 3);

            // 计算该位置在线段上的插值比例
            float segmentStart = segmentPoints.get(segmentIndex);
            float segmentEnd = segmentPoints.get(segmentIndex + 1);
            float segmentRatio = (segmentEnd - segmentStart > 0) ? (pos - segmentStart) / (segmentEnd - segmentStart) : 0;

            // 获取下一段的顶点（如果存在）
            float[] v1_next = v1_curr;
            float[] v2_next = v2_curr;
            float[] v3_next = v3_curr;
            float[] v4_next = v4_curr;

            if (baseIndex + 4 < vertices.size()) {
                v1_next = vertices.get(baseIndex + 4);
                v2_next = vertices.get(baseIndex + 5);
                v3_next = vertices.get(baseIndex + 6);
                v4_next = vertices.get(baseIndex + 7);
            }

            // 在该位置插值计算中心点坐标
            float centerX = v1_curr[0] + (v1_next[0] - v1_curr[0]) * segmentRatio;
            float centerY = v1_curr[1] + (v1_next[1] - v1_curr[1]) * segmentRatio;
            float centerZ = v1_curr[2] + (v1_next[2] - v1_curr[2]) * segmentRatio;

            // 计算当前位置的主闪电半径，用于确定特效的生成范围
            float currentWidth = 0.1f;
            float currentHeight = 0.7f - (0.7f - 0.0f) * (pos / currentLength);
            float radius = (currentWidth + currentHeight) * 0.5f; // 平均半径

            // 创建环绕主闪电的特效起点（均匀分布在圆环上）
            // 使用角度参数控制特效的环绕分布
            float angle = (float) (t * Math.PI * 8 + random.nextFloat() * Math.PI * 2); // 水平环绕角度
            float verticalAngle = (random.nextFloat() - 0.5f) * (float) Math.PI; // 垂直方向角度

            // 计算环绕方向（垂直于forward方向的平面）
            // 生成在单位球面上的随机方向向量
            org.joml.Vector3f circleDirection = new org.joml.Vector3f(
                    (float) (Math.cos(angle) * Math.cos(verticalAngle)),
                    (float) (Math.sin(verticalAngle)),
                    (float) (Math.sin(angle) * Math.cos(verticalAngle))
            );

            // 将circleDirection转换为基于主闪电方向的坐标系
            // 通过线性组合将球面方向转换到闪电局部坐标系
            org.joml.Vector3f effectDirection = new org.joml.Vector3f();
            effectDirection.x = rightVec.x * circleDirection.x + upVec.x * circleDirection.y + forwardVec.x * circleDirection.z;
            effectDirection.y = rightVec.y * circleDirection.x + upVec.y * circleDirection.y + forwardVec.y * circleDirection.z;
            effectDirection.z = rightVec.z * circleDirection.x + upVec.z * circleDirection.y + forwardVec.z * circleDirection.z;
            effectDirection.normalize(); // 归一化方向向量

            // 特效起点稍微偏离主闪电中心，并在Y轴方向下移
            float offsetDistance = radius * (0.8f + random.nextFloat() * 0.4f); // 随机偏移距离

            float[] effectStart = {
                    centerX + effectDirection.x * offsetDistance,      // 特效起点X坐标
                    centerY + effectDirection.y * offsetDistance + yOffset, // 特效起点Y坐标（带下移）
                    centerZ + effectDirection.z * offsetDistance       // 特效起点Z坐标
            };

            // 根据特效类型决定长度和宽度，创建不同类型的效果
            boolean isLongEffect = random.nextFloat() < 0.3f; // 30%概率生成长特效
            // 长特效和短特效使用不同的尺寸参数
            float branchLength = isLongEffect ?
                    (0.1f + random.nextFloat() * 0.3f) :     // 长特效长度
                    (0.03f + random.nextFloat() * 0.07f);    // 短特效长度

            float effectWidth = isLongEffect ?
                    (0.01f + random.nextFloat() * 0.02f) :   // 长特效宽度
                    (0.003f + random.nextFloat() * 0.007f);  // 短特效宽度

            // 添加一些随机扰动方向，使特效更加自然
            org.joml.Vector3f branchDirection = new org.joml.Vector3f(
                    effectDirection.x + (random.nextFloat() - 0.5f) * 0.5f,  // X方向扰动
                    effectDirection.y + (random.nextFloat() - 0.5f) * 0.5f,  // Y方向扰动
                    effectDirection.z + (random.nextFloat() - 0.5f) * 0.5f   // Z方向扰动
            );
            branchDirection.normalize(); // 归一化扰动方向

            // 计算特效终点坐标
            float[] effectEnd = {
                    effectStart[0] + branchDirection.x * branchLength,  // 终点X坐标
                    effectStart[1] + branchDirection.y * branchLength,  // 终点Y坐标
                    effectStart[2] + branchDirection.z * branchLength   // 终点Z坐标
            };

            // 设置颜色（长特效更亮），增强视觉层次感
            if (isLongEffect) {
                RenderSystem.setShaderColor(0.8f, 0.95f, 1.0f, 1.0f); // 长特效使用较亮的蓝白色
            } else {
                RenderSystem.setShaderColor(0.6f, 0.8f, 1.0f, 1.0f);  // 短特效使用稍暗的蓝白色
            }

            // 绘制四边形表示细线特效
            // 通过两个三角形组成一个矩形面来表示特效线段
            bufferBuilder.addVertex(x + effectStart[0] - rightVec.x * effectWidth,
                    y + effectStart[1] - rightVec.y * effectWidth,
                    z + effectStart[2] - rightVec.z * effectWidth);
            bufferBuilder.addVertex(x + effectStart[0] + rightVec.x * effectWidth,
                    y + effectStart[1] + rightVec.y * effectWidth,
                    z + effectStart[2] + rightVec.z * effectWidth);
            bufferBuilder.addVertex(x + effectEnd[0] + rightVec.x * effectWidth,
                    y + effectEnd[1] + rightVec.y * effectWidth,
                    z + effectEnd[2] + rightVec.z * effectWidth);

            bufferBuilder.addVertex(x + effectEnd[0] + rightVec.x * effectWidth,
                    y + effectEnd[1] + rightVec.y * effectWidth,
                    z + effectEnd[2] + rightVec.z * effectWidth);
            bufferBuilder.addVertex(x + effectEnd[0] - rightVec.x * effectWidth,
                    y + effectEnd[1] - rightVec.y * effectWidth,
                    z + effectEnd[2] - rightVec.z * effectWidth);
            bufferBuilder.addVertex(x + effectStart[0] - rightVec.x * effectWidth,
                    y + effectStart[1] - rightVec.y * effectWidth,
                    z + effectStart[2] - rightVec.z * effectWidth);
        }

        // 恢复默认颜色设置
        RenderSystem.setShaderColor(0.7f, 0.9f, 1.0f, 0.8f);
    }
}
