package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.GraphicsMapper;
import naitsirc98.beryl.graphics.rendering.common.PrimitiveTopology;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Sampler.CompareOperation;
import naitsirc98.beryl.graphics.textures.Sampler.MagFilter;
import naitsirc98.beryl.graphics.textures.Sampler.MinFilter;
import naitsirc98.beryl.graphics.textures.Sampler.WrapMode;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.util.collections.EnumMapper;
import naitsirc98.beryl.util.types.DataType;

import java.util.EnumMap;

import static org.lwjgl.opengl.GL45.*;

public class GLMapper extends GraphicsMapper {

    @Override
    protected void init() {

        initDataTypeMapper();
        initPixelFormatMapper();
        initWrapModeMapper();
        initMinFilterMapper();
        initMagFilterMapper();
        initCompareOpMapper();
        initPrimitiveTopologyMapper();
        initCubemapFaceMapper();
    }

    public int mapToSizedInternalFormat(PixelFormat pixelFormat) {
        return toSizedInternalFormat(mapToAPI(pixelFormat));
    }

    public int mapFromSizedInternalFormat(PixelFormat pixelFormat) {
        return fromSizedInternalFormat(mapToAPI(pixelFormat));
    }

    public int mapToFormat(PixelFormat internalImageFormat) {
        return toFormat(mapToAPI(internalImageFormat));
    }

    private void initPrimitiveTopologyMapper() {

        EnumMap<PrimitiveTopology, Integer> map = new EnumMap<>(PrimitiveTopology.class);

        map.put(PrimitiveTopology.POINTS, GL_POINTS);
        map.put(PrimitiveTopology.LINES, GL_LINES);
        map.put(PrimitiveTopology.TRIANGLES, GL_TRIANGLES);
        map.put(PrimitiveTopology.TRIANGLE_STRIP, GL_TRIANGLE_STRIP);

        register(PrimitiveTopology.class, EnumMapper.of(map));
    }

    private int toFormat(int internalFormat) {
        switch (internalFormat) {
            case GL_SRGB:
                return GL_RGB;
            case GL_SRGB_ALPHA:
                return GL_RGBA;
        }
        return fromSizedInternalFormat(internalFormat);
    }

    private int toSizedInternalFormat(int internalFormat) {
        switch(internalFormat) {
            case GL_RED:
                return GL_R8;
            case GL_RG:
                return GL_RG8;
            case GL_RGB:
                return GL_RGB8;
            case GL_SRGB:
                return GL_SRGB8;
            case GL_RGBA:
                return GL_RGBA8;
            case GL_SRGB_ALPHA:
                return GL_SRGB8_ALPHA8;
            default:
                return internalFormat;
        }
    }

    private int fromSizedInternalFormat(int sizedInternalFormat) {
        switch(sizedInternalFormat) {
            case GL_R8:
            case GL_R16F:
            case GL_R32F:
                return GL_RED;
            case GL_RG8:
            case GL_RG16F:
            case GL_RG32F:
                return GL_RG;
            case GL_RGB8:
            case GL_RGB16F:
            case GL_RGB32F:
                return GL_RGB;
            case GL_RGBA8:
            case GL_RGBA16F:
            case GL_RGBA32F:
                return GL_RGBA;
            case GL_SRGB8:
                return GL_SRGB;
            case GL_SRGB8_ALPHA8:
                return GL_SRGB_ALPHA;
            default:
                return sizedInternalFormat;
        }
    }

    private void initDataTypeMapper() {

        EnumMap<DataType, Integer> map = new EnumMap<>(DataType.class);

        map.put(DataType.INT8, GL_BYTE);
        map.put(DataType.UINT8, GL_UNSIGNED_BYTE);
        map.put(DataType.INT16, GL_SHORT);
        map.put(DataType.UINT16, GL_UNSIGNED_SHORT);
        map.put(DataType.INT32, GL_INT);
        map.put(DataType.UINT32, GL_UNSIGNED_INT);
        map.put(DataType.FLOAT16, GL_HALF_FLOAT);
        map.put(DataType.FLOAT32, GL_FLOAT);
        map.put(DataType.DOUBLE, GL_DOUBLE);

        register(DataType.class, EnumMapper.of(map));
    }

    private void initPixelFormatMapper() {

        EnumMap<PixelFormat, Integer> map = new EnumMap<>(PixelFormat.class);

        map.put(PixelFormat.RED, GL_RED);
        map.put(PixelFormat.RG, GL_RG);
        map.put(PixelFormat.RGB, GL_RGB);
        map.put(PixelFormat.RGBA, GL_RGBA);

        map.put(PixelFormat.SRGB, GL_SRGB);
        map.put(PixelFormat.SRGBA, GL_SRGB_ALPHA);

        map.put(PixelFormat.RED16F, GL_R16F);
        map.put(PixelFormat.RG16F, GL_RG16F);
        map.put(PixelFormat.RGB16F, GL_RGB16F);
        map.put(PixelFormat.RGBA16F, GL_RGBA16F);

        map.put(PixelFormat.RED32F, GL_R32F);
        map.put(PixelFormat.RG32F, GL_RG32F);
        map.put(PixelFormat.RGB32F, GL_RGB32F);
        map.put(PixelFormat.RGBA32F, GL_RGBA32F);

        register(PixelFormat.class, EnumMapper.of(map));
    }

    private void initWrapModeMapper() {

        EnumMap<WrapMode, Integer> map = new EnumMap<>(WrapMode.class);

        map.put(WrapMode.REPEAT, GL_REPEAT);
        map.put(WrapMode.MIRRORED_REPEAT, GL_MIRRORED_REPEAT);
        map.put(WrapMode.CLAMP_TO_BORDER, GL_CLAMP_TO_BORDER);
        map.put(WrapMode.CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
        map.put(WrapMode.MIRRORED_CLAMP_TO_EDGE, GL_MIRROR_CLAMP_TO_EDGE);

        register(WrapMode.class, EnumMapper.of(map));
    }

    private void initMinFilterMapper() {

        EnumMap<MinFilter, Integer> map = new EnumMap<>(MinFilter.class);

        map.put(MinFilter.NEAREST_MIPMAP_NEAREST, GL_NEAREST_MIPMAP_NEAREST);
        map.put(MinFilter.NEAREST_MIPMAP_LINEAR, GL_NEAREST_MIPMAP_LINEAR);
        map.put(MinFilter.LINEAR_MIPMAP_NEAREST, GL_LINEAR_MIPMAP_NEAREST);
        map.put(MinFilter.LINEAR_MIPMAP_LINEAR, GL_LINEAR_MIPMAP_LINEAR);

        register(MinFilter.class, EnumMapper.of(map));
    }

    private void initMagFilterMapper() {

        EnumMap<MagFilter, Integer> map = new EnumMap<>(MagFilter.class);

        map.put(MagFilter.NEAREST, GL_NEAREST);
        map.put(MagFilter.LINEAR, GL_LINEAR);

        register(MagFilter.class, EnumMapper.of(map));
    }


    private void initCompareOpMapper() {

        EnumMap<CompareOperation, Integer> map = new EnumMap<>(CompareOperation.class);

        map.put(CompareOperation.NEVER, GL_NEVER);
        map.put(CompareOperation.LESS, GL_LESS);
        map.put(CompareOperation.LESS_OR_EQUAL, GL_LEQUAL);
        map.put(CompareOperation.EQUAL, GL_EQUAL);
        map.put(CompareOperation.NOT_EQUAL, GL_NOTEQUAL);
        map.put(CompareOperation.GREATER, GL_GREATER);
        map.put(CompareOperation.GREATER_OR_EQUAL, GL_GEQUAL);
        map.put(CompareOperation.ALWAYS, GL_ALWAYS);

        register(CompareOperation.class, EnumMapper.of(map));
    }

    private void initCubemapFaceMapper() {

        EnumMap<Cubemap.Face, Integer> map = new EnumMap<>(Cubemap.Face.class);

        map.put(Cubemap.Face.LEFT, GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        map.put(Cubemap.Face.RIGHT, GL_TEXTURE_CUBE_MAP_POSITIVE_X);
        map.put(Cubemap.Face.TOP, GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
        map.put(Cubemap.Face.BOTTOM, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
        map.put(Cubemap.Face.BACK, GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
        map.put(Cubemap.Face.FRONT, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

        register(Cubemap.Face.class, EnumMapper.of(map));
    }
}
