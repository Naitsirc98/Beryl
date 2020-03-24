package naitsirc98.beryl.graphics;

import naitsirc98.beryl.util.types.Singleton;

public enum GraphicsAPI {

    VULKAN(1, 1, "450 core"),
    OPENGL(4, 5, "330 core");

    @Singleton
    private static GraphicsAPI currentGraphicsAPI;

    public static boolean flipTexCoords() {
        return currentGraphicsAPI == VULKAN;
    }

    public static GraphicsAPI get() {
        return currentGraphicsAPI;
    }

    public static <T> T chooseByAPI(T valueIfVulkan, T valueIfOpenGL) {
        return get() == VULKAN ? valueIfVulkan : valueIfOpenGL;
    }

    private final int versionMajor;
    private final int versionMinor;
    private final String minGLSLVersion;

    GraphicsAPI(int versionMajor, int versionMinor, String minGLSLVersion) {
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.minGLSLVersion = minGLSLVersion;
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

    public String minGLSLVersion() {
        return minGLSLVersion;
    }
}
