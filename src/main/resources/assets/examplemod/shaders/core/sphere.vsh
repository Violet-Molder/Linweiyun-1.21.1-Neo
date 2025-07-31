// src/main/resources/assets/examplemod/shaders/core/sphere.vsh
#version 150

// 定义输入属性：顶点位置（3D坐标）
in vec3 Position;

// 定义输入属性：顶点颜色（RGBA）
in vec4 Color;

// 定义统一变量：模型视图矩阵（4x4矩阵，用于坐标变换）
uniform mat4 ModelViewMat1;

// 定义统一变量：投影矩阵（4x4矩阵，用于透视投影）
uniform mat4 ProjMat1;

// 定义统一变量：游戏时间（浮点数，用于动画效果）
uniform float GameTime1;

// 定义输出变量：传递给片段着色器的顶点颜色
out vec4 vertexColor;

// 定义输出变量：传递给片段着色器的顶点位置
out vec3 vertexPosition;

// 顶点着色器主函数
void main() {
    // 将顶点颜色传递给片段着色器
    vertexColor = Color;

    // 将顶点位置传递给片段着色器
    vertexPosition = Position;

    // 应用动画效果：根据时间对顶点位置进行微调
    vec3 animatedPosition = Position;
    // 添加基于时间的正弦波动画，振幅为0.05
    animatedPosition += vec3(
    sin(GameTime1 + Position.x * 10.0) * 0.05,
    cos(GameTime1 + Position.y * 10.0) * 0.05,
    sin(GameTime1 + Position.z * 10.0) * 0.05
    );

    // 计算最终的顶点位置：
    // 1. 首先应用模型视图变换（ModelViewMat * 顶点坐标）
    // 2. 然后应用投影变换（ProjMat * 上一步结果）
    // 3. 最终结果存储在gl_Position中供OpenGL使用
    gl_Position = ProjMat1 * ModelViewMat1 * vec4(animatedPosition, 1.0);
}
