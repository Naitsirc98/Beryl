package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;

import static naitsirc98.beryl.util.Asserts.assertThat;

public class SkyboxTexture implements Resource {

    private static final int DEFAULT_IRRADIANCE_MAP_SIZE = 64;
    private static final int DEFAULT_PREFILTER_MAP_SIZE = 512;


    private final SkyboxPBRTextureFactory pbrTextureFactory;
    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap prefilterMap;
    private int irradianceMapSize = DEFAULT_IRRADIANCE_MAP_SIZE;
    private int prefilterMapSize = DEFAULT_PREFILTER_MAP_SIZE;

    public SkyboxTexture(SkyboxPBRTextureFactory pbrTextureFactory, Cubemap environmentMap) {
        this.pbrTextureFactory = pbrTextureFactory;
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

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Cubemap> T irradianceMap() {
        if(irradianceMap == null && environmentMap != null) {
            irradianceMap = pbrTextureFactory.createIrradianceMap(environmentMap, irradianceMapSize);
        }
        return (T) irradianceMap;
    }

    @SuppressWarnings("unchecked")
    public <T extends Cubemap> T prefilterMap() {
        if(prefilterMap == null && environmentMap != null) {
            prefilterMap = pbrTextureFactory.createPrefilterMap(environmentMap, prefilterMapSize);
        }
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
