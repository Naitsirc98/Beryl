package naitsirc98.beryl.images;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
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
     * Creates a 1x1 blank image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#release()}
     *
     * @param format the pixel format
     * @return the new image
     */
    public static Image newBlankImage(PixelFormat format) {
        return newBlankImage(1, 1, format);
    }

    /**
     * Creates a blank image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#release()}
     *
     * @param width  the width
     * @param height the height
     * @param format the pixel format
     * @return the new image
     */
    public static Image newBlankImage(int width, int height, PixelFormat format) {
        return newImage(width, height, format, 0xFFFFFFFF);
    }

    /**
     * Creates an image filled with the given value, backed by a new buffer in memory.
     * The buffer must be manually freed by calling {@link Image#release()}
     *
     * @param width  the width
     * @param height the height
     * @param format the pixel format
     * @param pixelValue the pixel value
     * @return the new image
     */
    public static Image newImage(int width, int height, PixelFormat format, int pixelValue) {
        Image image = newImage(width, height, format);
        if(format.dataType().decimal()) {
            memSet(image.pixelsf(), pixelValue);
        } else {
            memSet(image.pixelsi(), pixelValue);
        }
        return image;
    }

    /**
     * Creates an image backed by a new buffer in memory. The buffer must be manually freed by calling {@link Image#release()}
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
     * then it must be manually freed by calling {@link Image#release}
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
     * @return the icon image
     */
    public static Image newIcon(String filename) {
        return newImage(filename, PixelFormat.RGBA);
    }

    /**
     * Creates a new image from the specified filename
     *
     * @param filename    the filename of the image
     * @param pixelFormat the pixel format
     * @return the image
     */
    public static Image newImage(String filename, PixelFormat pixelFormat) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            int desiredChannels = pixelFormat == null ? STBI_default : pixelFormat.channels();

            Buffer pixels = readPixelsFromFile(filename, width, height, channels, desiredChannels, pixelFormat, false);

            if(pixels == null) {
                throw new IOException();
            }

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

        if(pixelFormat != null && pixelFormat.dataType().decimal()) {
            return stbi_loadf(filename, width, height, channels, desiredChannels);
        }

        return stbi_load(filename, width, height, channels, desiredChannels);
    }

    private static final class STBImage extends Image {

        STBImage(int width, int height, PixelFormat pixelFormat, Buffer pixels) {
            super(width, height, pixelFormat, pixels);
        }

        @Override
        protected void free() {
            nstbi_image_free(memAddress((Buffer)pixels()));
        }
    }

    private static final class BufferedImage extends Image {

        BufferedImage(int width, int height, PixelFormat pixelFormat, Buffer pixels) {
            super(width, height, pixelFormat, pixels);
        }

        @Override
        protected void free() {
            memFree(pixels());
        }
    }

    private ImageFactory() {}

}
