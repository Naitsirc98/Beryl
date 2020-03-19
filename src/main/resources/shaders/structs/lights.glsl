
#define LIGHT_TYPE_DIRECTIONAL 0
#define LIGHT_TYPE_POINT 1
#define LIGHT_TYPE_SPOT 2

#define LIGHT_SIZEOF = 80

struct Light {

    vec4 color;

    vec4 position;
    vec4 direction;

    float constant;
    float linear;
    float quadratic;
    float _pad1;

    float cutOff;
    float outerCutOff;
    float _pad2;
    int type;
};
