package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.images.PixelFormat;

public interface SkyboxTextureLoader {

    PixelFormat pixelFormat();

    SkyboxTextureLoader pixelFormat(PixelFormat pixelFormat);

    String imageExtension();

    SkyboxTextureLoader imageExtension(String extension);

    Cubemap loadSkyboxTexture(String path);

}
