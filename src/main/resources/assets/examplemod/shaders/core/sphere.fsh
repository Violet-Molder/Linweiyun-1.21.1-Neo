// src/main/resources/assets/examplemod/shaders/core/sphere.fsh
#version 150

// 导入Minecraft的雾效处理函数
#moj_import <fog.glsl>

// 定义输入变量：从顶点着色器传递过来的顶点颜色
in vec4 vertexColor;

// 定义输入变量：从顶点着色器传递过来的顶点位置
in vec3 vertexPosition;

// 定义统一变量：游戏时间（用于动画效果）
uniform float GameTime1;

// 定义输出变量：片段最终颜色
out vec4 fragColor;

// 片段着色器主函数
void main() {
    // 标准化顶点位置向量，用于光照计算
    vec3 normal = normalize(vertexPosition);

    // 创建基于法线的光照效果
    // 使用法线的Y分量创建从上到下的亮度渐变
    float light = normal.y * 0.5 + 0.5;

    // 创建脉动效果：使用正弦函数基于时间创建周期性变化
    // sin(GameTime * 5.0)生成-1到1之间的值
    // * 0.1 + 0.9将范围调整为0.8到1.0
    float pulse = sin(GameTime1 * 5.0) * 0.1 + 0.9;

    // 创建边缘发光效果：
    // length(vertexPosition)计算顶点到中心的距离
    // 1.0 - 距离值创建从中心到边缘的渐变
    float edgeGlow = 1.0 - length(vertexPosition);
    // 使用幂函数增强边缘效果
    edgeGlow = pow(edgeGlow, 2.0) * 0.3;

    // 计算最终颜色：
    // 1. 使用顶点颜色作为基础颜色
    vec4 finalColor = vertexColor;

    // 2. 应用光照效果
    finalColor.rgb *= (light * pulse);

    // 3. 添加边缘发光效果
    finalColor.rgb += vec3(0.3, 1.0, 0.3) * edgeGlow;

    // 4. 应用透明度调整
    finalColor.a = vertexColor.a * pulse;

//    // 如果片段透明度太低，则丢弃该片段（不渲染）
//    if (finalColor.a < 0.01) {
//        discard;
//    }
    if(GameTime1 > 4.0f) {
        fragColor = vec4(0.5f,0.0f,0.0f,1.0f);
    } else if(GameTime1 > 3.0f){
        // 输出最终颜色
        fragColor = vec4(0.0f,0.5f,0.0f,1.0f);
    } else if(GameTime1 > 2.0f){
        fragColor = vec4(0.5f,0.5f,0.0f,1.0f);
    } else if(GameTime1 > 0.85f){
        fragColor = vec4(0.5f,0.0f,0.5f,1.0f);
    } else if(GameTime1 > 0.84f){
        fragColor = vec4(0.5f, 0.5f, 0.5f, 1.0f);
    }


}
