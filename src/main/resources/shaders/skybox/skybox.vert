#version 450 core

layout(std140, binding = 0) uniform Matrices {
	mat4 u_ProjectionMatrix;
	mat4 u_ViewMatrix;
};


layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

layout(location = 0) out vec3 out_FragmentPosition;


void main() {

	out_FragmentPosition = in_Position;

	mat4 rotatedView = mat4(mat3(u_ViewMatrix));
	vec4 clipPosition = u_ProjectionMatrix * rotatedView * vec4(out_FragmentPosition, 1.0);

	gl_Position = clipPosition.xyww;
}
