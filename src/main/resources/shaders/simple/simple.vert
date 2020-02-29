#version 410 core
// #extension GL_ARB_separate_shader_objects : enable

uniform mat4 u_MVP;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;


layout(location = 0) out vec3 out_FragColor;

void main() {
    out_FragColor = vec3(1, 1, 0.5);
    gl_Position = u_MVP * vec4(in_Position, 1.0f);
}