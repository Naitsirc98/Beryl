package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.GraphicsMapper;
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
}
