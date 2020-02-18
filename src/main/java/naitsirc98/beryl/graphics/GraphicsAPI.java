package naitsirc98.beryl.graphics;

import naitsirc98.beryl.util.Singleton;

public enum GraphicsAPI {

    VULKAN,
    OPENGL;

    @Singleton
    private static GraphicsAPI currentGraphicsAPI;

    public static GraphicsAPI get() {
        return currentGraphicsAPI;
    }

}
