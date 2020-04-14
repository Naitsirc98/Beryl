package naitsirc98.beryl.scenes;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.images.PixelFormat;

public class Skybox {

    private final Cubemap cubemap;
    private float rotationAngle;

    public Skybox(String skyboxImagePath) {
        cubemap = GraphicsFactory.get().newCubemap(skyboxImagePath, PixelFormat.RGBA);
        cubemap.sampler()
                .wrapMode(Sampler.WrapMode.CLAMP_TO_EDGE)
                .magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
    }

    @SuppressWarnings("unchecked")
    public <T extends Cubemap> T texture() {
        return (T) cubemap;
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
}
