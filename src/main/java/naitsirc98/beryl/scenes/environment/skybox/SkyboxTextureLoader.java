package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.file.Path;

public interface SkyboxTextureLoader {

    PixelFormat pixelFormat();

    SkyboxTextureLoader pixelFormat(PixelFormat pixelFormat);

    String imageExtension();

    SkyboxTextureLoader imageExtension(String extension);

    Cubemap loadSkyboxTexture(Path path);

}
