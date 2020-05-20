struct Skybox {
    
    samplerCube irradianceMap;
    samplerCube prefilterMap;
    sampler2D brdfMap;
};