package naitsirc98.beryl.graphics;

import naitsirc98.beryl.util.types.Singleton;

public enum GraphicsAPI {

    VULKAN(1, 1),
    OPENGL(4, 5);

    @Singleton
    private static GraphicsAPI currentGraphicsAPI;

    public static GraphicsAPI get() {
        return currentGraphicsAPI;
    }

    public static <T> T chooseByAPI(T valueIfVulkan, T valueIfOpenGL) {
        return get() == VULKAN ? valueIfVulkan : valueIfOpenGL;
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

    @Override
    public String toString() {
        return name() + " v" + versionMajor + "." + versionMinor;
    }
}
