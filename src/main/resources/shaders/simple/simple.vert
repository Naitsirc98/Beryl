#version 450 core
// #extension GL_ARB_separate_shader_objects : enable

layout(push_constant) uniform ModelViewProjectionMatrix
{
    mat4 u_MVP;
};

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;


layout(location = 0) out vec3 out_FragColor;

void main() {
    out_FragColor = in_Position;
    gl_Position = u_MVP * vec4(in_Position, 1.0f);
}