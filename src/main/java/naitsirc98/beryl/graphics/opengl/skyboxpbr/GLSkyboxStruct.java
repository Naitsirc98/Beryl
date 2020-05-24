package naitsirc98.beryl.graphics.opengl.skyboxpbr;

import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxTexture;

/*
 * struct Skybox {
 *
 *     samplerCube irradianceMap;
 *     samplerCube prefilterMap;
 *     sampler2D brdfMap;
 *
 *     float maxPrefilterLOD;
 *     float prefilterLODBias;
 * };
 * */
public final class GLSkyboxStruct {

    private static final String SKYBOX_UNIFORM_NAME = "u_Skybox";
    private static final String IRRADIANCE_MAP_UNIFORM_NAME = SKYBOX_UNIFORM_NAME + ".irradianceMap";
    private static final String PREFILTER_MAP_UNIFORM_NAME = SKYBOX_UNIFORM_NAME + ".prefilterMap";
    private static final String BRDF_MAP_UNIFORM_NAME = SKYBOX_UNIFORM_NAME + ".brdfMap";
    private static final String MAX_PREFILTER_LOD_UNIFORM_NAME = SKYBOX_UNIFORM_NAME + ".maxPrefilterLOD";
    private static final String PREFILTER_LOD_BIAS_UNIFORM_NAME = SKYBOX_UNIFORM_NAME + ".prefilterLODBias";


    public static void bind(Skybox skybox, GLShaderProgram shader, int firstTextureUnit) {

        if(skybox == null) {
            return;
        }

        final SkyboxTexture skyboxTexture = skybox.texture1();

        if(skyboxTexture.irradianceMap() != null) {
            shader.uniformSampler(IRRADIANCE_MAP_UNIFORM_NAME, skyboxTexture.irradianceMap(), firstTextureUnit);
        }

        if(skyboxTexture.prefilterMap() != null) {
            shader.uniformSampler(PREFILTER_MAP_UNIFORM_NAME, skyboxTexture.prefilterMap(), firstTextureUnit + 1);
        }

        if(skybox.brdfTexture() != null) {
            shader.uniformSampler(BRDF_MAP_UNIFORM_NAME, skybox.brdfTexture(), firstTextureUnit + 2);
        }

        shader.uniformFloat(MAX_PREFILTER_LOD_UNIFORM_NAME, skybox.maxPrefilterLOD());

        shader.uniformFloat(PREFILTER_LOD_BIAS_UNIFORM_NAME, skybox.prefilterLODBias());
    }

    private GLSkyboxStruct() {}
}
