package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.textures.Cubemap;

public class SkyboxFactory {

    private static final int DEFAULT_HDR_TEXTURE_SIZE = 1024;


    public static Skybox newSkybox(String path1) {

        SkyboxTextureLoader textureLoader = new SimpleSkyboxTextureLoader();

        Cubemap texture1 = textureLoader.loadSkyboxTexture(path1);

        return new Skybox(texture1, null);
    }

    public static Skybox newSkybox(String path1, String path2) {

        SkyboxTextureLoader textureLoader = new SimpleSkyboxTextureLoader();

        Cubemap texture1 = textureLoader.loadSkyboxTexture(path1);

        Cubemap texture2 = textureLoader.loadSkyboxTexture(path2);

        return new Skybox(texture1, texture2);
    }

    public static Skybox newSkyboxHDR(String hdrTexturePath1) {
        return newSkyboxHDR(hdrTexturePath1, DEFAULT_HDR_TEXTURE_SIZE);
    }

    public static Skybox newSkyboxHDR(String hdrTexturePath1, int size) {

        Cubemap environmentMap = SkyboxHelper.getSkyboxPBRTextureFactory().createEnvironmentMap(hdrTexturePath1, size);

        return new Skybox(environmentMap, null);
    }

    public static Skybox newSkyboxHDR(String hdrTexturePath1, String hdrTexturePath2) {
        return newSkyboxHDR(hdrTexturePath1, hdrTexturePath2, DEFAULT_HDR_TEXTURE_SIZE);
    }

    public static Skybox newSkyboxHDR(String hdrTexturePath1, String hdrTexturePath2, int size) {

        Cubemap environmentMap1 = SkyboxHelper.getSkyboxPBRTextureFactory().createEnvironmentMap(hdrTexturePath1, size);

        Cubemap environmentMap2 = SkyboxHelper.getSkyboxPBRTextureFactory().createEnvironmentMap(hdrTexturePath2, size);

        return new Skybox(environmentMap1, environmentMap2);
    }

}
