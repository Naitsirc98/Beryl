package naitsirc98.beryl.scenes.environment.skybox.pbr;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.logging.Log;

import static naitsirc98.beryl.util.Asserts.assertThat;

public class SkyboxPBRTextures {

    private static final int DEFAULT_BRDF_TEXTURE_SIZE = 1024;

    private static SkyboxPBRTextureFactory skyboxPBRTextureFactory;

    private static SkyboxPBRTextureFactory getSkyboxPBRTextureFactory() {
        if(skyboxPBRTextureFactory == null) {
            skyboxPBRTextureFactory = SkyboxPBRTextureFactory.create();
        }
        return skyboxPBRTextureFactory;
    }


    private Texture2D brdfTexture;
    private int brdfTextureSize = DEFAULT_BRDF_TEXTURE_SIZE;

    public SkyboxPBRTextures() {
    }

    public Texture2D brdfTexture() {
        if(brdfTexture == null && Graphics.isGraphicsThread()) {
            brdfTexture = getSkyboxPBRTextureFactory().createBRDFTexture(brdfTextureSize);
        }
        return brdfTexture;
    }

    public int brdfTextureSize() {
        return brdfTextureSize;
    }

    public SkyboxPBRTextures brdfTextureSize(int brdfTextureSize) {
        this.brdfTextureSize = assertThat(brdfTextureSize, brdfTextureSize > 0);
        if(brdfTexture != null) {
            Log.warning("BRDF Texture has already been created for this skybox, so setting the BRDF Texture size now has no effect.");
        }
        return this;
    }
}
