package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;

import java.nio.file.Path;

public class SkyboxFactory {

    private static final int DEFAULT_HDR_TEXTURE_SIZE = 1024;

    private static final PixelFormat DEFAULT_HDR_PIXEL_FORMAT = PixelFormat.RGB16F;


    public static Skybox newSkybox(Path path1) {

        SkyboxTextureLoader textureLoader = new SimpleSkyboxTextureLoader();

        Cubemap texture1 = textureLoader.loadSkyboxTexture(path1);

        return new Skybox(texture1, null);
    }

    public static Skybox newSkybox(Path path1, Path path2) {

        SkyboxTextureLoader textureLoader = new SimpleSkyboxTextureLoader();

        Cubemap texture1 = textureLoader.loadSkyboxTexture(path1);

        Cubemap texture2 = textureLoader.loadSkyboxTexture(path2);

        return new Skybox(texture1, texture2);
    }

    public static Skybox newSkyboxHDR(Path hdrTexturePath1) {
        return newSkyboxHDR(hdrTexturePath1, DEFAULT_HDR_TEXTURE_SIZE, DEFAULT_HDR_PIXEL_FORMAT);
    }

    public static Skybox newSkyboxHDR(Path hdrTexturePath1, PixelFormat pixelFormat) {
        return newSkyboxHDR(hdrTexturePath1, DEFAULT_HDR_TEXTURE_SIZE, pixelFormat);
    }

    public static Skybox newSkyboxHDR(Path hdrTexturePath1, int size, PixelFormat pixelFormat) {

        SkyboxPBRTextureFactory skyboxPBRTextureFactory = Graphics.graphicsContext().skyboxPBRTextureFactory();

        Cubemap environmentMap = skyboxPBRTextureFactory.createEnvironmentMap(hdrTexturePath1, size, pixelFormat);

        return new Skybox(environmentMap, null).enableHDR(true);
    }

    public static Skybox newSkyboxHDR(Path hdrTexturePath1, Path hdrTexturePath2) {
        return newSkyboxHDR(hdrTexturePath1, hdrTexturePath2, DEFAULT_HDR_TEXTURE_SIZE, DEFAULT_HDR_PIXEL_FORMAT);
    }

    public static Skybox newSkyboxHDR(Path hdrTexturePath1, Path hdrTexturePath2, PixelFormat pixelFormat) {
        return newSkyboxHDR(hdrTexturePath1, hdrTexturePath2, DEFAULT_HDR_TEXTURE_SIZE, pixelFormat);
    }

    public static Skybox newSkyboxHDR(Path hdrTexturePath1, Path hdrTexturePath2, int size, PixelFormat pixelFormat) {

        SkyboxPBRTextureFactory skyboxPBRTextureFactory = Graphics.graphicsContext().skyboxPBRTextureFactory();

        Cubemap environmentMap1 = skyboxPBRTextureFactory.createEnvironmentMap(hdrTexturePath1, size, pixelFormat);

        Cubemap environmentMap2 = skyboxPBRTextureFactory.createEnvironmentMap(hdrTexturePath2, size, pixelFormat);

        return new Skybox(environmentMap1, environmentMap2).enableHDR(true);
    }

}
