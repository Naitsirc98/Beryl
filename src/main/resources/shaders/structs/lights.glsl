
#define LIGHT_TYPE_DIRECTIONAL 1
#define LIGHT_TYPE_POINT 2
#define LIGHT_TYPE_SPOT 3

#define NULL 0

#define LIGHT_SIZEOF = 80

struct Light {

    vec4 color;

    vec4 position;
    vec4 direction;

    float constant;
    float linear;
    float quadratic;

    float cutOff;
    float outerCutOff;

    float nearPlane;
    float farPlane;

    int type;
};
