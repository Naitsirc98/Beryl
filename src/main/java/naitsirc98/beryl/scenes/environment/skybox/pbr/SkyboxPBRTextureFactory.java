package naitsirc98.beryl.scenes.environment.skybox.pbr;

import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Texture2D;

public interface SkyboxPBRTextureFactory {

    Cubemap createEnvironmentMap(String hdrTexture, int size);

    Cubemap createIrradianceMap(Cubemap environmentMap, int size);

    Cubemap createPrefilterMap(Cubemap environmentMap, int size, float maxLOD);

    Texture2D createBRDFTexture(int size);
}
