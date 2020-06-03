package naitsirc98.beryl.images;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.FileUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * An utility class for creating images.
 */
public final class ImageFactory {

    /**
     * Creates a 1x1 white image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#release()}.
     *
     * @param format the pixel format.
     * @return the new image.
     */
    public static Image newWhiteImage(PixelFormat format) {
        return newWhiteImage(1, 1, format);
    }

    /**
     * Creates a 1x1 black image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#release()}.
     *
     * @param format the pixel format.
     * @return the new image.
     */
    public static Image newBlackImage(PixelFormat format) {
        return newBlackImage(1, 1, format);
    }

    /**
     * Creates a white image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#release()}.
     *
     * @param width  the width.
     * @param height the height.
     * @param format the pixel format.
     * @return the new image.
     */
    public static Image newWhiteImage(int width, int height, PixelFormat format) {
        return newImage(width, height, format, 0xFFFFFFFF);
    }

    /**
     * Creates a black image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#release()}.
     *
     * @param width  the width.
     * @param height the height.
     * @param format the pixel format.
     * @return the new image.
     */
    public static Image newBlackImage(int width, int height, PixelFormat format) {
        return newImage(width, height, format, 0x0);
    }

    /**
     * Creates an image filled with the given value, backed by a new buffer in memory.
     * The buffer must be manually freed by calling {@link Image#release()}.
     *
     * @param width  the width.
     * @param height the height.
     * @param format the pixel format.
     * @param pixelValue the pixel value.
     * @return the new image.
     */
    public static Image newImage(int width, int height, PixelFormat format, int pixelValue) {
        Image image = newImage(width, height, format);
        memSet(image.pixels(), pixelValue);
        return image;
    }

    /**
     * Creates an image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#release()}.
     *
     * @param width  the width.
     * @param height the height.
     * @param format the pixel format.
     * @return the new image.
     */
    public static Image newImage(int width, int height, PixelFormat format) {
        return newImage(width, height, format, memAlloc(width * height * format.sizeof()));
    }

    /**
     * Creates a new image backed by the specified buffer. If the buffer has been allocated with {@link org.lwjgl.system.MemoryUtil},
     * then it must be manually freed by calling {@link Image#release}.
     *
     * @param width  the width.
     * @param height the height.
     * @param format the pixel format.
     * @param buffer the pixel buffer.
     * @return the new image.
     */
    public static Image newImage(int width, int height, PixelFormat format, ByteBuffer buffer) {
        return new BufferedImage(width, height, format, buffer);
    }

    /**
     * Creates a new image suitable for window icons. An icon must be RGBA.
     *
     * @param path the path of the icon.
     * @return the icon image.
     */
    public static Image newIcon(Path path) {
        return newImage(path, PixelFormat.RGBA);
    }

    /**
     * Creates a new image from the specified filename.
     *
     * @param path    the path of the image.
     * @param pixelFormat the pixel format.
     * @return the image.
     */
    public static Image newImage(Path path, PixelFormat pixelFormat) {
        return newImage(path, pixelFormat, false);
    }

    /**
     * Creates a new image from the specified filename.
     *
     * @param path    the path of the image.
     * @param pixelFormat the pixel format.
     * @param flipY whether to flip the image vertically or not.
     * @return the image.
     */
    public static Image newImage(Path path, PixelFormat pixelFormat, boolean flipY) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            int desiredChannels = pixelFormat == null ? STBI_default : pixelFormat.channels();

            ByteBuffer pixels = readPixelsFromFile(path, width, height, channels, desiredChannels, pixelFormat, flipY);

            if(pixels == null) {
                Log.error("Failed to load image " + path + ": " + stbi_failure_reason());
                return null;
            }

            if(pixelFormat == null) {
                pixelFormat = PixelFormat.of(pixels, channels.get(0));
            }

            return new STBImage(width.get(0), height.get(0), pixelFormat, pixels);

        } catch(Throwable e) {
            Log.error("Failed to load image " + path + ": " + stbi_failure_reason(), e);
        }
        return null;
    }

    private static ByteBuffer readPixelsFromFile(Path path,
                                                 IntBuffer width, IntBuffer height,
                                                 IntBuffer channels, int desiredChannels,
                                                 PixelFormat pixelFormat, boolean flipY) {

        stbi_set_flip_vertically_on_load(flipY);

        ByteBuffer fileContents = FileUtils.readAllBytes(path);

        try {

            if(pixelFormat != null && pixelFormat.dataType().decimal()) {

                FloatBuffer pixelsf = stbi_loadf_from_memory(fileContents, width, height, channels, desiredChannels);

                if(pixelsf != null) {
                    return memByteBuffer(pixelsf);
                }

                return null;

            } else {
                return stbi_load_from_memory(fileContents, width, height, channels, desiredChannels);
            }

        } finally {
            memFree(fileContents);
        }
    }

    private static final class STBImage extends Image {

        STBImage(int width, int height, PixelFormat pixelFormat, ByteBuffer pixels) {
            super(width, height, pixelFormat, pixels);
        }

        @Override
        protected void free() {
            nstbi_image_free(memAddress((Buffer)pixels()));
        }
    }

    private static final class BufferedImage extends Image {

        BufferedImage(int width, int height, PixelFormat pixelFormat, ByteBuffer pixels) {
            super(width, height, pixelFormat, pixels);
        }

        @Override
        protected void free() {
            memFree(pixels());
        }
    }

    private ImageFactory() {}

}
