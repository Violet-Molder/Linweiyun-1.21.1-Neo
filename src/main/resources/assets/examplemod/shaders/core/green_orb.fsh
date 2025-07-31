// src/main/resources/assets/examplemod/shaders/core/green_orb.fsh
#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float GameTime;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

in vec3 vertex_position;
out vec4 fragColor;


void main() {
    // 标准化顶点位置作为法线
    vec3 normal = normalize(vertex_position);

    // 计算光照
    float diffuse = max(dot(normal, normalize(Light0_Direction)), 0.0) +
    max(dot(normal, normalize(Light1_Direction)), 0.0);
    diffuse = diffuse * 0.3 + 0.7;

    // 脉动效果
    float pulse = sin(GameTime * 5.0) * 0.1 + 0.9;

    // 核心亮点
    float core = 1.0 - length(vertex_position) * 2.0;
    core = pow(max(core, 0.0), 2.0) * 0.5;

    // 边缘发光
    float edge = 1.0 - abs(length(vertex_position) * 2.0 - 1.0);
    edge = pow(max(edge, 0.0), 4.0) * 0.3;

    // 最终颜色
    vec4 color = vec4(0.0, 1.0, 0.0, 0.7);
    color.rgb *= (diffuse * pulse + core);
    color.rgb += vec3(0.3, 1.0, 0.3) * edge;
    color *= ColorModulator;

    if (color.a < 0.01) {
        discard;
    }

    fragColor = color;
}
