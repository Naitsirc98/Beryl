package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.IColor;
import naitsirc98.beryl.util.types.ByteSize;

import java.util.function.Consumer;

import static naitsirc98.beryl.util.types.DataType.*;

/**
 * struct WaterMaterial {
 *
 *     vec4 color;
 *
 *     sampler2D dudvMap;
 *     sampler2D normalMap;
 *     sampler2D reflectionMap;
 *     sampler2D refractionMap;
 *
 *     vec2 tiling;
 *     float _padding0;
 *     float _padding1;
 *
 *     float distortionStrength;
 *     float textureOffset;
 *     float colorStrength;
 *     float _padding2;
 *
 *     int flags;
 * };
 * */
@ByteSize.Static(WaterMaterial.SIZEOF)
public class WaterMaterial extends AbstractMaterial {

    public static final int SIZEOF = VECTOR4_SIZEOF + 4 * SAMPLER_SIZEOF + VECTOR2_SIZEOF + 6 * FLOAT32_SIZEOF + INT32_SIZEOF;

    // FLAGS

    private static final int NORMAL_MAP_PRESENT = 0x1;
    private static final int DUDV_MAP_PRESENT = 0x2;

    // === //

    private static final float DEFAULT_DISTORTION_STRENGTH = 0.02f;
    private static final float DEFAULT_TEXTURE_OFFSET = 0.0f;
    private static final float DEFAULT_COLOR_STRENGTH = 0.2f;

    private static final MaterialFactory<WaterMaterial> FACTORY = new MaterialFactory<>(WaterMaterial.class);

    public static MaterialFactory<WaterMaterial> getFactory() {
        return FACTORY;
    }


    private Color color;

    private Texture2D dudvMap;
    private Texture2D normalMap;
    private Texture2D reflectionMap;
    private Texture2D refractionMap;

    private float distortionStrength;
    private float textureOffset;
    private float colorStrength;

    WaterMaterial(String name) {
        super(name);
        setupDefaults();
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @Override
    public WaterMaterial tiling(float x, float y) {
        super.tiling(x, y);
        return this;
    }

    @Override
    public ShadingModel shadingModel() {
        return null;
    }

    public IColor getColor() {
        return color;
    }

    public WaterMaterial setColor(IColor color) {
        this.color.set(color);
        return this;
    }

    public Texture2D getDudvMap() {
        return getMapOrDefault(dudvMap);
    }

    public WaterMaterial setDudvMap(Texture2D dudvMap) {

        updateTexturesUseCount(this.dudvMap, dudvMap);

        this.dudvMap = dudvMap;

        if(dudvMap != null) {
            setFlag(DUDV_MAP_PRESENT);
        } else {
            removeFlag(DUDV_MAP_PRESENT);
        }

        return this;
    }

    public Texture2D getNormalMap() {
        return getMapOrDefault(normalMap);
    }

    public WaterMaterial setNormalMap(Texture2D normalMap) {

        updateTexturesUseCount(this.normalMap, normalMap);

        this.normalMap = normalMap;

        if(normalMap != null) {
            setFlag(NORMAL_MAP_PRESENT);
        } else {
            removeFlag(NORMAL_MAP_PRESENT);
        }

        return this;
    }

    public Texture2D getReflectionMap() {
        return reflectionMap;
    }

    public Texture2D getRefractionMap() {
        return refractionMap;
    }

    public float getDistortionStrength() {
        return distortionStrength;
    }

    public WaterMaterial setDistortionStrength(float distortionStrength) {
        this.distortionStrength = distortionStrength;
        return this;
    }

    public float getTextureOffset() {
        return textureOffset;
    }

    public WaterMaterial setTextureOffset(float textureOffset) {
        this.textureOffset = textureOffset;
        return this;
    }

    public float getColorStrength() {
        return colorStrength;
    }

    public WaterMaterial setColorStrength(float colorStrength) {
        this.colorStrength = colorStrength;
        return this;
    }

    private void setupDefaults() {

        color = Color.colorWhite();

        GraphicsFactory graphicsFactory = GraphicsFactory.get();

        reflectionMap = graphicsFactory.newTexture2D();
        refractionMap = graphicsFactory.newTexture2D();

        distortionStrength = DEFAULT_DISTORTION_STRENGTH;
        textureOffset = DEFAULT_TEXTURE_OFFSET;
        colorStrength = DEFAULT_COLOR_STRENGTH;
    }
}
