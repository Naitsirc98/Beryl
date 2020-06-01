struct Skybox {
    
    layout(bindless_sampler) samplerCube irradianceMap;
    layout(bindless_sampler) samplerCube prefilterMap;
    layout(bindless_sampler) sampler2D brdfMap;

    float maxPrefilterLOD;
    float prefilterLODBias;
};