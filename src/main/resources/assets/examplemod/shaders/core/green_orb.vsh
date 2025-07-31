// src/main/resources/assets/examplemod/shaders/core/green_orb.vsh
#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 vertex_position;

void main() {
    vertex_position = Position;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
