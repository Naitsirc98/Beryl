package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.util.types.DataType;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL30C.*;

public class GLUtils {

    public static int toGL(DataType dataType) {
        switch(dataType) {
            case INT8:
                return GL_BYTE;
            case UINT8:
                return GL_UNSIGNED_BYTE;
            case INT16:
                return GL_SHORT;
            case UINT16:
                return GL_UNSIGNED_SHORT;
            case INT32:
                return GL_INT;
            case UINT32:
                return GL_UNSIGNED_INT;
            case FLOAT16:
            case FLOAT32:
                return GL_FLOAT;
            case DOUBLE:
                return GL_DOUBLE;
            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }

    public static int toGL(PixelFormat pixelFormat) {
        switch(pixelFormat) {
            case RED:
                return GL_RED;
            case RG:
                return GL_RG;
            case RGB:
                return GL_RGB;
            case RGBA:
                return GL_RGBA;
            case RED16F:
                return GL_R16F;
            case RG16F:
                return GL_RG16F;
            case RGB16F:
                return GL_RGB16F;
            case RGBA16F:
                return GL_RGBA16F;
            case RED32F:
                return GL_R32F;
            case RG32F:
                return GL_RG32F;
            case RGB32F:
                return GL_RGB32F;
            case RGBA32F:
                return GL_RGBA32F;
        }
        throw new IllegalArgumentException("Unknown GL format " + pixelFormat);
    }

    public static int toGL(Sampler.WrapMode wrapMode) {
        switch(requireNonNull(wrapMode)) {
            case REPEAT:
                return GL_REPEAT;
            case MIRRORED_REPEAT:
                return GL_MIRRORED_REPEAT;
            case CLAMP_TO_BORDER:
                return GL_CLAMP_TO_BORDER;
            case CLAMP_TO_EDGE:
                return GL_CLAMP_TO_EDGE;
        }
        throw new IllegalArgumentException("Unknown Wrap mode: " + wrapMode);
    }

    public static int toGL(Sampler.Filter filter) {
        switch(requireNonNull(filter)) {
            case NEAREST:
                return GL_NEAREST;
            case LINEAR:
                return GL_LINEAR;
            case MIPMAP_NEAREST:
                return GL_NEAREST_MIPMAP_NEAREST;
            case MIPMAP_LINEAR:
                return GL_LINEAR_MIPMAP_LINEAR;
        }
        throw new IllegalArgumentException("Unknown Filter: " + filter);
    }

}
