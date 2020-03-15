package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.GraphicsMapper;
import naitsirc98.beryl.graphics.buffers.GraphicsBuffer;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Sampler.BorderColor;
import naitsirc98.beryl.graphics.textures.Sampler.MinFilter;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.util.collections.EnumMapper;

import java.nio.IntBuffer;
import java.util.EnumMap;

import static org.lwjgl.vulkan.VK11.*;

public class VulkanMapper extends GraphicsMapper {

    @Override
    protected void init() {
        initPixelFormatMapper();
        initWrapModeMapper();
        initMagFilterMapper();
        initCompareOpMapper();
        initBorderColorMapper();
    }

    private void initBorderColorMapper() {

        EnumMap<BorderColor, Integer> map = new EnumMap<>(BorderColor.class);

        map.put(BorderColor.WHITE_INT_OPAQUE, VK_BORDER_COLOR_INT_OPAQUE_WHITE);
        map.put(BorderColor.BLACK_INT_OPAQUE, VK_BORDER_COLOR_INT_OPAQUE_BLACK);

        map.put(BorderColor.WHITE_FLOAT_OPAQUE, VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);
        map.put(BorderColor.BLACK_FLOAT_OPAQUE, VK_BORDER_COLOR_FLOAT_OPAQUE_BLACK);

        map.put(BorderColor.BLACK_INT_TRANSPARENT, VK_BORDER_COLOR_INT_TRANSPARENT_BLACK);
        map.put(BorderColor.BLACK_FLOAT_TRANSPARENT, VK_BORDER_COLOR_FLOAT_TRANSPARENT_BLACK);

        register(BorderColor.class, EnumMapper.of(map));
    }

    public MinFilter mapMinFilterFromAPI(int vkMinFilter, int vkMipmapMode) {

        if(vkMinFilter == VK_FILTER_NEAREST) {

            if(vkMipmapMode == VK_SAMPLER_MIPMAP_MODE_NEAREST) {
                return Sampler.MinFilter.NEAREST_MIPMAP_NEAREST;
            } else if(vkMipmapMode == VK_SAMPLER_MIPMAP_MODE_LINEAR) {
                return Sampler.MinFilter.NEAREST_MIPMAP_LINEAR;
            }

        } else if(vkMinFilter == VK_FILTER_LINEAR) {

            if(vkMipmapMode == VK_SAMPLER_MIPMAP_MODE_NEAREST) {
                return Sampler.MinFilter.LINEAR_MIPMAP_NEAREST;
            } else if(vkMipmapMode == VK_SAMPLER_MIPMAP_MODE_LINEAR) {
                return Sampler.MinFilter.LINEAR_MIPMAP_LINEAR;
            }

        }

        throw new IllegalArgumentException("Unknown Vulkan Min Filter: " + vkMinFilter + ", " + vkMipmapMode);
    }

    public GraphicsBuffer.Type mapBufferTypeFromAPI(int usage) {

        if((usage & VK_BUFFER_USAGE_VERTEX_BUFFER_BIT) == VK_BUFFER_USAGE_VERTEX_BUFFER_BIT) {
            return GraphicsBuffer.Type.VERTEX_BUFFER;
        } else if((usage & VK_BUFFER_USAGE_INDEX_BUFFER_BIT) == VK_BUFFER_USAGE_INDEX_BUFFER_BIT) {
            return GraphicsBuffer.Type.INDEX_BUFFER;
        } else if((usage & VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT) == VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT) {
            return GraphicsBuffer.Type.UNIFORM_BUFFER;
        }

        throw new IllegalArgumentException("Unknown Vulkan buffer usage: " + usage);
    }

    private void initPixelFormatMapper() {

        EnumMap<PixelFormat, Integer> map = new EnumMap<>(PixelFormat.class);

        map.put(PixelFormat.RED, VK_FORMAT_R8_SRGB);
        map.put(PixelFormat.RG, VK_FORMAT_R8G8_SRGB);
        map.put(PixelFormat.RGB, VK_FORMAT_R8G8B8_SRGB);
        map.put(PixelFormat.RGBA, VK_FORMAT_R8G8B8A8_SRGB);

        map.put(PixelFormat.RED16F, VK_FORMAT_R16_SFLOAT);
        map.put(PixelFormat.RG16F, VK_FORMAT_R16G16_SFLOAT);
        map.put(PixelFormat.RGB16F, VK_FORMAT_R16G16B16_SFLOAT);
        map.put(PixelFormat.RGBA16F, VK_FORMAT_R16G16B16A16_SFLOAT);

        map.put(PixelFormat.RED32F, VK_FORMAT_R32_SFLOAT);
        map.put(PixelFormat.RG32F, VK_FORMAT_R32G32_SFLOAT);
        map.put(PixelFormat.RGB32F, VK_FORMAT_R32G32B32_SFLOAT);
        map.put(PixelFormat.RGBA32F, VK_FORMAT_R32G32B32A32_SFLOAT);

        register(PixelFormat.class, EnumMapper.of(map));
    }

    private void initWrapModeMapper() {

        EnumMap<Sampler.WrapMode, Integer> map = new EnumMap<>(Sampler.WrapMode.class);

        map.put(Sampler.WrapMode.REPEAT, VK_SAMPLER_ADDRESS_MODE_REPEAT);
        map.put(Sampler.WrapMode.MIRRORED_REPEAT, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
        map.put(Sampler.WrapMode.CLAMP_TO_BORDER, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER);
        map.put(Sampler.WrapMode.CLAMP_TO_EDGE, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);

        register(Sampler.WrapMode.class, EnumMapper.of(map));
    }

    private void initMagFilterMapper() {

        EnumMap<Sampler.MagFilter, Integer> map = new EnumMap<>(Sampler.MagFilter.class);

        map.put(Sampler.MagFilter.NEAREST, VK_FILTER_NEAREST);
        map.put(Sampler.MagFilter.LINEAR, VK_FILTER_LINEAR);

        register(Sampler.MagFilter.class, EnumMapper.of(map));
    }


    private void initCompareOpMapper() {

        EnumMap<Sampler.CompareOperation, Integer> map = new EnumMap<>(Sampler.CompareOperation.class);

        map.put(Sampler.CompareOperation.NEVER, VK_COMPARE_OP_NEVER);
        map.put(Sampler.CompareOperation.LESS, VK_COMPARE_OP_LESS);
        map.put(Sampler.CompareOperation.LESS_OR_EQUAL, VK_COMPARE_OP_LESS_OR_EQUAL);
        map.put(Sampler.CompareOperation.EQUAL, VK_COMPARE_OP_EQUAL);
        map.put(Sampler.CompareOperation.NOT_EQUAL, VK_COMPARE_OP_NOT_EQUAL);
        map.put(Sampler.CompareOperation.GREATER, VK_COMPARE_OP_GREATER);
        map.put(Sampler.CompareOperation.GREATER_OR_EQUAL, VK_COMPARE_OP_GREATER_OR_EQUAL);
        map.put(Sampler.CompareOperation.ALWAYS, VK_COMPARE_OP_ALWAYS);

        register(Sampler.CompareOperation.class, EnumMapper.of(map));
    }

    public IntBuffer mapMinFilterToAPI(MinFilter minFilter, IntBuffer output) {

        int vkMinFilter = -1;
        int vkMipmapMode = -1;

        switch(minFilter) {
            case NEAREST_MIPMAP_NEAREST:
                vkMinFilter = VK_FILTER_NEAREST;
                vkMipmapMode = VK_SAMPLER_MIPMAP_MODE_NEAREST;
                break;
            case NEAREST_MIPMAP_LINEAR:
                vkMinFilter = VK_FILTER_NEAREST;
                vkMipmapMode = VK_SAMPLER_MIPMAP_MODE_LINEAR;
                break;
            case LINEAR_MIPMAP_NEAREST:
                vkMinFilter = VK_FILTER_LINEAR;
                vkMipmapMode = VK_SAMPLER_MIPMAP_MODE_NEAREST;
                break;
            case LINEAR_MIPMAP_LINEAR:
                vkMinFilter = VK_FILTER_LINEAR;
                vkMipmapMode = VK_SAMPLER_MIPMAP_MODE_LINEAR;
                break;
        }

        return output.put(0, vkMinFilter).put(1, vkMipmapMode);
    }
}
