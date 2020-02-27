#version 330 core
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 in_FragColor;

layout(location = 0) out vec4 out_FinalColor;

void main() {
    out_FinalColor = vec4(in_FragColor, 1.0);
}