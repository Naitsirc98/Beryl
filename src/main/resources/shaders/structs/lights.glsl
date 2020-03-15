
#define DIRECTIONAL_LIGHT_SIZE 7
#define POINT_LIGHT_SIZE 10
#define SPOT_LIGHT_SIZE 15

#define LIGHT_TYPE_DIRECTIONAL 0
#define LIGHT_TYPE_POINT 1
#define LIGHT_TYPE_SPOT 2

struct DirectionalLight {

    vec3 direction;
    
    vec4 color;
};

struct PointLight {

    vec3 position;

    vec4 color;

    float constant;
    float linear;
    float quadratic;
};

struct SpotLight {

    vec3 position;
    vec3 direction;

    vec4 color;

    float constant;
    float linear;
    float quadratic;

    float cutOff;
    float outerCutOff;
};