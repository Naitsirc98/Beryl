package naitsirc98.beryl.scenes;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.images.PixelFormat;

public class Skybox {

    private Cubemap texture1;
    private Cubemap texture2;
    private float textureBlendFactor = 0.5f;
    private float rotationAngle = 0.0f;

    public Skybox(Cubemap texture1) {
        this.texture1 = texture1;
    }

    public Skybox(Cubemap texture1, Cubemap texture2, float textureBlendFactor) {
        this.texture1 = texture1;
        this.texture2 = texture2;
        this.textureBlendFactor = textureBlendFactor;
        this.rotationAngle = rotationAngle;
    }

    public Skybox(String skyboxImageFolder) {
        texture1 = GraphicsFactory.get().newCubemap(skyboxImageFolder, PixelFormat.RGBA);
        texture1.sampler()
                .wrapMode(Sampler.WrapMode.CLAMP_TO_EDGE)
                .magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
        texture2 = null;
    }

    public Skybox(String skyboxImageFolder1, String skyboxImageFolder2) {

        texture1 = GraphicsFactory.get().newCubemap(skyboxImageFolder1, PixelFormat.RGBA);
        texture1.sampler()
                .wrapMode(Sampler.WrapMode.CLAMP_TO_EDGE)
                .magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);

        texture2 = GraphicsFactory.get().newCubemap(skyboxImageFolder2, PixelFormat.RGBA);
        texture2.sampler()
                .wrapMode(Sampler.WrapMode.CLAMP_TO_EDGE)
                .magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
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
