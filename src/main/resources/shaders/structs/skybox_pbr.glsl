struct Skybox {
    
    samplerCube irradianceMap;
    samplerCube prefilterMap;
    sampler2D brdfMap;

    float maxPrefilterLOD;
    float prefilterLODBias;
};