package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.resources.Resource;

import static naitsirc98.beryl.util.Asserts.assertThat;

public class Skybox extends ManagedResource {

    private static final float DEFAULT_TEXTURE_BLEND_FACTOR = 0.5f;
    private static final float DEFAULT_ROTATION_ANGLE = 0.0f;

    private static final int DEFAULT_BRDF_TEXTURE_SIZE = 512;

    private static final float DEFAULT_MAX_PREFILTER_LOD = 4.0f;
    private static final float DEFAULT_PREFILTER_LOD_BIAS = -0.5f;


    private SkyboxTexture texture1;
    private SkyboxTexture texture2;
    private Texture2D brdfTexture;
    private int brdfTextureSize = DEFAULT_BRDF_TEXTURE_SIZE;
    private float textureBlendFactor = DEFAULT_TEXTURE_BLEND_FACTOR;
    private float rotationAngle = DEFAULT_ROTATION_ANGLE;
    private float maxPrefilterLOD;
    private float prefilterLODBias;
    private boolean enableHDR;

    Skybox(Cubemap texture1, Cubemap texture2) {
        maxPrefilterLOD = DEFAULT_MAX_PREFILTER_LOD;
        prefilterLODBias = DEFAULT_PREFILTER_LOD_BIAS;
        this.texture1 = new SkyboxTexture(this, texture1);
        this.texture2 = new SkyboxTexture(this, texture2);
        brdfTexture = Graphics.graphicsContext().skyboxPBRTextureFactory().createBRDFTexture(brdfTextureSize);
    }

    public SkyboxTexture texture1() {
        return texture1;
    }

    public Skybox texture1(Cubemap texture1) {
        this.texture1.environmentMap(texture1);
        return this;
    }

    public SkyboxTexture texture2() {
        return texture2;
    }

    public Skybox texture2(Cubemap texture2) {
        this.texture1.environmentMap(texture2);
        return this;
    }

    public Skybox swapTextures() {
        final SkyboxTexture tmp = texture1;
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

    public boolean enableHDR() {
        return enableHDR;
    }

    public Skybox enableHDR(boolean enableHDR) {
        this.enableHDR = enableHDR;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Texture2D> T brdfTexture() {
        return (T) brdfTexture;
    }

    public int brdfTextureSize() {
        return brdfTextureSize;
    }

    public Skybox brdfTextureSize(int brdfTextureSize) {
        this.brdfTextureSize = assertThat(brdfTextureSize, brdfTextureSize > 0);
        if(brdfTexture != null) {
            Log.warning("BRDF Texture has already been created for this skybox, so setting the BRDF Texture size now has no effect.");
        }
        return this;
    }

    public float maxPrefilterLOD() {
        return maxPrefilterLOD;
    }

    public Skybox maxPrefilterLOD(float maxPrefilterLOD) {
        this.maxPrefilterLOD = maxPrefilterLOD;
        return this;
    }

    public float prefilterLODBias() {
        return prefilterLODBias;
    }

    public Skybox prefilterLODBias(float prefilterLODBias) {
        this.prefilterLODBias = prefilterLODBias;
        return this;
    }

    @Override
    protected void free() {
        Resource.release(texture1.environmentMap());
        Resource.release(texture2.environmentMap());
        Resource.release(brdfTexture);
        texture1.release();
        texture2.release();
    }
}
