package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.textures.Cubemap;

public class SkyboxFactory {

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

}
