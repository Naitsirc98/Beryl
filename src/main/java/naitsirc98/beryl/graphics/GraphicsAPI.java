package naitsirc98.beryl.graphics;

import naitsirc98.beryl.util.Singleton;

public enum GraphicsAPI {

    VULKAN(1, 1),
    OPENGL(4, 6);

    @Singleton
    private static GraphicsAPI currentGraphicsAPI;

    public static GraphicsAPI get() {
        return currentGraphicsAPI;
    }

    private int versionMajor;
    private int versionMinor;

    GraphicsAPI(int versionMajor, int versionMinor) {
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
    }

    public int versionMajor() {
        return versionMajor;
    }

    public int versionMinor() {
        return versionMinor;
    }
}
