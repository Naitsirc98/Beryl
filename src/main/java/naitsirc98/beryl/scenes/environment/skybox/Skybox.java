package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.scenes.environment.skybox.pbr.SkyboxPBRTextures;

public class Skybox {

    private static final float DEFAULT_TEXTURE_BLEND_FACTOR = 0.5f;
    private static final float DEFAULT_ROTATION_ANGLE = 0.0f;

    private final SkyboxPBRTextures pbrTextures;
    private Cubemap texture1;
    private Cubemap texture2;
    private float textureBlendFactor = DEFAULT_TEXTURE_BLEND_FACTOR;
    private float rotationAngle = DEFAULT_ROTATION_ANGLE;

    Skybox(Cubemap texture1, Cubemap texture2) {
        this.texture1 = SkyboxHelper.setSkyboxTextureSamplerParameters(texture1);
        this.texture2 = SkyboxHelper.setSkyboxTextureSamplerParameters(texture2);
        pbrTextures = new SkyboxPBRTextures();
    }

    public SkyboxPBRTextures pbrTextures() {
        return pbrTextures;
    }

    @SuppressWarnings("unchecked")
    public <T extends Cubemap> T texture1() {
        return (T) texture1;
    }

    public Skybox texture1(Cubemap texture1) {
        this.texture1 = texture1;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Cubemap> T texture2() {
        return (T) texture2;
    }

    public Skybox texture2(Cubemap texture2) {
        this.texture2 = texture2;
        return this;
    }

    public Skybox swapTextures() {
        final Cubemap tmp = texture1;
        texture1 = texture2;
        texture2 = tmp;
        return this;
    }

    public float rotation() {
        return rotationAngle;
    }

    public Skybox rotation(float radians) {
        this.rotationAngle = radians;
        return this;
    }

    public Skybox rotate(float radians) {
        this.rotationAngle += radians;
        return this;
    }

    public float textureBlendFactor() {
        return textureBlendFactor;
    }

    public Skybox textureBlendFactor(float factor) {
        this.textureBlendFactor = factor;
        return this;
    }
}
