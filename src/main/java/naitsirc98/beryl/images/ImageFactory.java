package naitsirc98.beryl.images;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;

import java.nio.Buffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * An utility class for creating images
 */
public final class ImageFactory {

    /**
     * Creates an image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#free}
     *
     * @param width  the width
     * @param height the height
     * @param format the pixel format
     * @return the new image
     */
    public static Image newImage(int width, int height, PixelFormat format) {
        return newImage(width, height, format, memAlloc(width * height * format.sizeof()));
    }

    /**
     * Creates a new image backed by the specified buffer. If the buffer has been allocated with {@link org.lwjgl.system.MemoryUtil},
     * then it must be manually freed by calling {@link Image#free}
     *
     * @param width  the width
     * @param height the height
     * @param format the pixel format
     * @param buffer the pixel buffer
     * @return the new image
     */
    public static Image newImage(int width, int height, PixelFormat format, Buffer buffer) {
        return new BufferedImage(width, height, format, buffer);
    }

    /**
     * Creates a new image suitable for window icons. An icon must be RGBA
     *
     * @param filename the filename of the icon
     * @param flipY tells whether this image should be flipped vertically on load
     * @return the icon image
     */
    public static Image newIcon(String filename, boolean flipY) {
        return newImage(filename, flipY, PixelFormat.RGBA);
    }

    /**
     * Creates a new image from the specified filename
     *
     * @param filename    the filename of the image
     * @param flipY       tells whether this image should be flipped vertically on load
     * @param pixelFormat the pixel format
     * @return the image
     */
    public static Image newImage(String filename, boolean flipY, PixelFormat pixelFormat) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            int desiredChannels = pixelFormat == null ? STBI_default : pixelFormat.channels();

            Buffer pixels = readPixelsFromFile(filename, width, height, channels, desiredChannels, pixelFormat, flipY);

            if(pixelFormat == null) {
                pixelFormat = PixelFormat.of(pixels, channels.get(0));
            }

            return new STBImage(width.get(0), height.get(0), pixelFormat, pixels);

        } catch(Throwable e) {
            Log.error("Cannot load image " + filename + ": " + stbi_failure_reason(), e);
        }
        return null;
    }

    private static Buffer readPixelsFromFile(String filename,
                                             IntBuffer width, IntBuffer height,
                                             IntBuffer channels, int desiredChannels,
                                             PixelFormat pixelFormat, boolean flipY) {

        stbi_set_flip_vertically_on_load(flipY);

        if(pixelFormat != null && pixelFormat.dataType().isDecimal()) {
            return stbi_loadf(filename, width, height, channels, desiredChannels);
        }

        return stbi_load(filename, width, height, channels, desiredChannels);
    }

    private static final class STBImage extends Image {

        STBImage(int width, int height, PixelFormat pixelFormat, Buffer pixels) {
            super(width, height, pixelFormat, pixels);
        }

        @Override
        public void free() {
            nstbi_image_free(memAddress((Buffer)pixels()));
        }
    }

    private static final class BufferedImage extends Image {

        BufferedImage(int width, int height, PixelFormat pixelFormat, Buffer pixels) {
            super(width, height, pixelFormat, pixels);
        }

        @Override
        public void free() {
            memFree(pixels());
        }
    }

    private ImageFactory() {}

}
