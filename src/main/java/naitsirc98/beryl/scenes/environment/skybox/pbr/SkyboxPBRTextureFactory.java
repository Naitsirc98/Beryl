package naitsirc98.beryl.scenes.environment.skybox.pbr;

import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.opengl.skyboxpbr.GLSkyboxPBRTextureFactory;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Texture2D;

public interface SkyboxPBRTextureFactory {

    static SkyboxPBRTextureFactory create() {

        // Only supporting OPENGL for now
        if(GraphicsAPI.get() == GraphicsAPI.OPENGL) {
            return new GLSkyboxPBRTextureFactory();
        }

        throw new RuntimeException("Failed to create a SkyboxPBRTextureFactory for this Graphics API");
    }


    Cubemap createEnvironmentMap(String hdrTexture, int size);

    Cubemap createIrradianceMap(Cubemap environmentMap, int size);

    Cubemap createPrefilterMap(Cubemap environmentMap, int size, float maxLOD);

    Texture2D createBRDFTexture(int size);
}
