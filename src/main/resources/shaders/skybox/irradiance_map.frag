#version 450 core

#pragma debug(on)

#define PI 3.1415926536


uniform samplerCube u_EnvironmentMap;


layout(location = 0) in FragmentData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
} fragment;

layout(location = 0) out vec4 out_FinalColor;


vec3 calculateIrradianceConvolution(vec3 position) {
    
    vec3 irradiance = vec3(0.0);

    float samples = 0.0;

    vec3 N = normalize(position);

    // Tangent space calculation from origin point
    vec3 up = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(up, N);
    up = cross(N, right);

    float sampleDelta = 0.025;

    for(float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta) {
        
        for(float theta = 0.0; theta < 0.5 * PI; theta += sampleDelta) {
            // Spherical to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi), sin(theta) * sin(phi), cos(theta));
            // Tangent space to world
            vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * N;

            irradiance += textureLod(u_EnvironmentMap, sampleVec, 0).rgb * cos(theta) * sin(theta);

            ++samples;
        }
    }

    irradiance = PI * irradiance * (1.0 / samples);

    return irradiance;
}

void main() {

    vec3 irradiance = calculateIrradianceConvolution(fragment.position);

    out_FinalColor = vec4(irradiance, 1.0);
}
