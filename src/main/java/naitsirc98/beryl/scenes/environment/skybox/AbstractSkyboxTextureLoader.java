package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.logging.Log;

import static java.util.Objects.requireNonNull;

public abstract class AbstractSkyboxTextureLoader implements SkyboxTextureLoader {

    private static final String DEFAULT_IMAGE_EXTENSION = ".png";


    private PixelFormat pixelFormat;
    private String imageExtension;

    public AbstractSkyboxTextureLoader() {
        pixelFormat = PixelFormat.RGBA;
        imageExtension = DEFAULT_IMAGE_EXTENSION;
    }

    @Override
    public PixelFormat pixelFormat() {
        return pixelFormat;
    }

    @Override
    public SkyboxTextureLoader pixelFormat(PixelFormat pixelFormat) {
        this.pixelFormat = requireNonNull(pixelFormat);
        return this;
    }

    @Override
    public String imageExtension() {
        return imageExtension;
    }

    @Override
    public SkyboxTextureLoader imageExtension(String extension) {

        if(imageExtension == null) {
            Log.error("Image extension cannot be null");
            return this;
        }

        if(imageExtension.charAt(0) != '.') {
            imageExtension = '.' + extension;
        } else {
            imageExtension = extension;
        }

        return this;
    }

    protected String getImagePath(String fileName) {
        return fileName + imageExtension;
    }
}
