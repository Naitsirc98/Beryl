package naitsirc98.beryl.images;

import naitsirc98.beryl.resources.ManagedResource;
import org.lwjgl.system.NativeResource;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Class representing image objects. They have width, height, number of channels per pixel and pixel format.
 * All the images are backed by a pixel buffer. Is implementation dependent how these buffers are created and freed.
 *
 * You should always call {@link Image#free} when no longer needed to release the resources used by an image. It can
 * be used inside a try-with-resources block to automatically free the memory
 */

public abstract class Image extends ManagedResource {

    private final int width;
    private final int height;
    private final PixelFormat pixelFormat;
    private Buffer pixels;

    /**
     * Instantiates a new Image.
     *
     * @param width       the width
     * @param height      the height
     * @param pixelFormat the pixel format
     * @param pixels      the pixel buffer
     */
    Image(int width, int height, PixelFormat pixelFormat, Buffer pixels) {
        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
        this.pixels = pixels;
    }

    /**
     * Returns the width of this image
     *
     * @return the width
     */
    public int width() {
        return width;
    }

    /**
     * Returns the height of this image
     *
     * @return the height
     */
    public int height() {
        return height;
    }

    /**
     * Returns the number of channels of this image
     *
     * @return the int
     */
    public int channels() {
        return pixelFormat.channels();
    }

    /**
     * Returns the pixel format of this image
     *
     * @return the pixel format
     */
    public PixelFormat pixelFormat() {
        return pixelFormat;
    }

    /**
     * Returns the pixel buffer
     *
     * @param <T> the type of buffer containing this image's pixels
     * @return the pixel buffer
     */
    @SuppressWarnings("unchecked")
    public <T extends Buffer> T pixels() {
        return (T) pixels;
    }

    /**
     * Returns the pixel buffer as a {@link ByteBuffer}
     *
     * @return the pixel byte buffer
     */
    public ByteBuffer pixelsi() {
        return (ByteBuffer) pixels;
    }

    /**
     * Returns the pixel buffer as a {@link FloatBuffer}
     *
     * @return the pixel float buffer
     */
    public FloatBuffer pixelsf() {
        return (FloatBuffer) pixels;
    }
}
