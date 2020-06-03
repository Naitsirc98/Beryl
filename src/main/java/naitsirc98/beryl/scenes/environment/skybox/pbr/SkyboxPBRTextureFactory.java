package naitsirc98.beryl.scenes.environment.skybox.pbr;

import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.file.Path;

public interface SkyboxPBRTextureFactory {

    Cubemap createEnvironmentMap(Path hdrTexturePath, int size, PixelFormat pixelFormat);

    Cubemap createIrradianceMap(Cubemap environmentMap, int size);

    Cubemap createPrefilterMap(Cubemap environmentMap, int size, float maxLOD);

    Texture2D createBRDFTexture(int size);
}
