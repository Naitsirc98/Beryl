package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.util.types.DataType;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.textures.Texture.WrapMode.*;
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

    public static PixelFormat glToPixelFormat(int glPixelFormat) {
        switch(glPixelFormat) {
            case GL_RED:
                return PixelFormat.RED;
            case GL_RG:
                return PixelFormat.RG;
            case GL_RGB:
                return PixelFormat.RGB;
            case GL_RGBA:
                return PixelFormat.RGBA;
            case GL_R16F:
                return PixelFormat.RED16F;
            case GL_RG16F:
                return PixelFormat.RG16F;
            case GL_RGB16F:
                return PixelFormat.RGB16F;
            case GL_RGBA16F:
                return PixelFormat.RGBA16F;
            case GL_R32F:
                return PixelFormat.RED32F;
            case GL_RG32F:
                return PixelFormat.RG32F;
            case GL_RGB32F:
                return PixelFormat.RGB32F;
            case GL_RGBA32F:
                return PixelFormat.RGBA32F;
        }
        throw new IllegalArgumentException("Unknown GL format " + glPixelFormat);
    }

    public static int toGL(Texture.WrapMode wrapMode) {
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

    public static int toGL(Texture.Filter filter) {
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

    public static Texture.WrapMode glToWrapMode(int glWrapMode) {
        switch(glWrapMode) {
            case GL_REPEAT:
                return REPEAT;
            case GL_MIRRORED_REPEAT:
                return MIRRORED_REPEAT;
            case GL_CLAMP_TO_BORDER:
                return CLAMP_TO_BORDER;
            case GL_CLAMP_TO_EDGE:
                return CLAMP_TO_EDGE;
        }
        throw new IllegalArgumentException("Unknown GL Wrap mode: " + glWrapMode);
    }

    public static Texture.Filter glToFilter(int glFilter) {
        switch(glFilter) {
            case GL_NEAREST:
                return Texture.Filter.NEAREST;
            case GL_LINEAR:
                return Texture.Filter.LINEAR;
            case GL_NEAREST_MIPMAP_NEAREST:
                return Texture.Filter.MIPMAP_NEAREST;
            case GL_LINEAR_MIPMAP_LINEAR:
                return Texture.Filter.MIPMAP_LINEAR;
        }
        throw new IllegalArgumentException("Unknown GL Filter: " + glFilter);
    }

}
