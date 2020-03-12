#version 330 core

in vec3 frag_Color;

out vec4 out_FinalColor;

void main() {
    out_FinalColor = vec4(frag_Color, 1.0f);
}