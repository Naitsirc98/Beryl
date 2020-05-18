#version 410 core

#define PI 3.1415926536


uniform samplerCube u_EnvironmentMap;


layout(location = 0) in vec3 frag_WorldPosition;

layout(location = 0) out vec4 out_FinalColor;


vec3 calculateIrradianceConvolution(out float samples) {
    
    vec3 irradiance = vec3(0.0);

    vec3 N = normalize(frag_WorldPosition);

    // Tangent space calculation from origin point
    vec3 up = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(up, N);
    up = cross(N, right);

    float sampleDelta = 0.025;

    for(float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta) {
        
        for(float theta = 0.0f; theta < 0.5 * PI; theta += sampleDelta) {
            // Spherical to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi),  sin(theta) * sin(phi), cos(theta));
            // Tangent space to world
            vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * N;

            irradiance += texture(u_EnvironmentMap, sampleVec).rgb * cos(theta) * sin(theta);

            ++samples;
        }
    }

    return irradiance;
}

void main() {

    float samples = 0.0;

    vec3 irradiance = calculateIrradianceConvolution(samples);

    if(samples != 0.0) {
        irradiance = PI * irradiance * (1.0 / samples);
    } 

    out_FinalColor = vec4(irradiance, 1.0);
}
