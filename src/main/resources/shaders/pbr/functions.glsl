/*
Functions for PBR calculations.
*/

#define PI 3.1415926536

// http://holger.dammertz.org/stuff/notes_hammersleyOnHemisphere.html
// Efficient VanDerCorpus calculation.
// Van Der Corpus sequence mirrors a decimal binary representation
// around its decimal point.
float radicalInverseVanDerCorpus(uint bits) {   

     bits = (bits << 16u) | (bits >> 16u);
     bits = ((bits & 0x55555555u) << 1u) | ((bits & 0xAAAAAAAAu) >> 1u);
     bits = ((bits & 0x33333333u) << 2u) | ((bits & 0xCCCCCCCCu) >> 2u);
     bits = ((bits & 0x0F0F0F0Fu) << 4u) | ((bits & 0xF0F0F0F0u) >> 4u);
     bits = ((bits & 0x00FF00FFu) << 8u) | ((bits & 0xFF00FF00u) >> 8u);

     return float(bits) * 2.3283064365386963e-10; // / 0x100000000
}

// Computes the Hammersley sequence. Gives us the low-discrepancy sample i of the total sample set of size N.
// pre-compute the specular portion of the indirect reflectance equation using importance
// sampling given a random low-discrepancy sequence
vec2 hammersley(uint i, uint N) {
	return vec2(float(i) / float(N), radicalInverseVanDerCorpus(i));
}

/*
To build a sample vector, we need some way of orienting and biasing the sample vector
towards the specular lobe of some surface roughness. We can take the NDF as described in the Theory
tutorial and combine the GGX NDF in the spherical sample vector process as described by Epic Games
*/
vec3 importanceSampleGGX(vec2 Xi, vec3 N, float roughness) {

	float a = roughness * roughness;

	float phi = 2.0 * PI * Xi.x;
	float cosTheta = sqrt((1.0 - Xi.y) / (1.0 + (a * a - 1.0) * Xi.y));
	float sinTheta = sqrt(1.0 - cosTheta * cosTheta);

	// From spherical coordinates to cartesian coordinates - halfway vector
	vec3 H;
	H.x = cos(phi) * sinTheta;
	H.y = sin(phi) * sinTheta;
	H.z = cosTheta;

	// From tangent-space H vector to world-space sample vector
	vec3 up = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
	vec3 tangent = normalize(cross(up, N));
	vec3 bitangent = cross(N, tangent);

	vec3 sampleVec = tangent * H.x + bitangent * H.y + N * H.z;

	return normalize(sampleVec);
}

// Geometry function
float geometrySchlickGGX(float NdotV, float roughness) {

    // Note that we use a different k for IBL
    float a = roughness;
    float k = (a * a) / 2.0;

    float nom   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return nom / denom;
}

float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {

    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = geometrySchlickGGX(NdotV, roughness);
    float ggx1 = geometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

float distributionGGX(vec3 N, vec3 H, float roughness) {

    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH * NdotH;

    float nom = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return nom / denom;
}

