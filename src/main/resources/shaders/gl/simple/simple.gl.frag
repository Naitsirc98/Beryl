#version 330 core

uniform vec4 color;

out vec4 out_FinalColor;

void main() {
    out_FinalColor = vec4(color.rgb, 1.0f);
}