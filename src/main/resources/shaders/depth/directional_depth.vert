@beryl

#ifdef OPENGL

uniform mat4 u_DepthMVP;

#else // VULKAN

layout(push_constant) uniform PushConstant {
    mat4 u_DepthMVP;
};

#endif

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

void main() {
    gl_Position = u_DepthMVP * vec4(in_Position, 1.0f);
}
