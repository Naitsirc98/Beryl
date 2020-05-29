package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;

import static naitsirc98.beryl.util.Asserts.assertThat;

public class SkyboxTexture implements Resource {

    private static final int DEFAULT_IRRADIANCE_MAP_SIZE = 32;
    private static final int DEFAULT_PREFILTER_MAP_SIZE = 1024;


    private final Skybox skybox;
    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap prefilterMap;
    private int irradianceMapSize = DEFAULT_IRRADIANCE_MAP_SIZE;
    private int prefilterMapSize = DEFAULT_PREFILTER_MAP_SIZE;

    public SkyboxTexture(Skybox skybox, Cubemap environmentMap) {
        this.skybox = skybox;
        environmentMap(environmentMap);
    }

    @SuppressWarnings("unchecked")
    public <T extends Cubemap> T environmentMap() {
        return (T) environmentMap;
    }

    public SkyboxTexture environmentMap(Cubemap environmentMap) {

        if(this.environmentMap != environmentMap) {
            release();
        }

        this.environmentMap = SkyboxHelper.setSkyboxTextureSamplerParameters(environmentMap);

        if(environmentMap != null) {
            SkyboxPBRTextureFactory skyboxPBRTextureFactory = Graphics.graphicsContext().skyboxPBRTextureFactory();
            irradianceMap = skyboxPBRTextureFactory.createIrradianceMap(environmentMap, irradianceMapSize);
            prefilterMap = skyboxPBRTextureFactory.createPrefilterMap(environmentMap, prefilterMapSize, skybox.maxPrefilterLOD());
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Cubemap> T irradianceMap() {
        return (T) irradianceMap;
    }

    @SuppressWarnings("unchecked")
    public <T extends Cubemap> T prefilterMap() {
        return (T) prefilterMap;
    }

    public int irradianceMapSize() {
        return irradianceMapSize;
    }

    public SkyboxTexture irradianceMapSize(int irradianceMapSize) {
        this.irradianceMapSize = assertThat(irradianceMapSize, irradianceMapSize > 0);
        return this;
    }

    public int prefilterMapSize() {
        return prefilterMapSize;
    }

    public SkyboxTexture prefilterMapSize(int prefilterMapSize) {
        this.prefilterMapSize = assertThat(prefilterMapSize, prefilterMapSize > 0);
        return this;
    }

    @Override
    public void release() {
        Resource.release(irradianceMap);
        Resource.release(prefilterMap);
        environmentMap = null;
        irradianceMap = null;
        prefilterMap = null;
    }
}
