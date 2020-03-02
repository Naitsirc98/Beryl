#version 450 core

layout(location = 0) in vec3 in_FragColor;

layout(location = 0) out vec4 out_FinalColor;

void main() {
    out_FinalColor = vec4(in_FragColor, 1.0f);
}