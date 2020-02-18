package naitsirc98.beryl.graphics;

import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.util.Singleton;

public class Graphics extends BerylSystem {

    @Singleton
    private static Graphics instance;

    public static API getGraphicsAPI() {
        return instance.graphicsAPI;
    }

    private API graphicsAPI;

    private Graphics() {

    }

    @Override
    protected void init() {
        graphicsAPI = BerylConfiguration.GRAPHICS_API.get(API.VULKAN);
        // TODO: init graphics context and window
    }

    @Override
    protected void terminate() {
        // TODO: release graphics resources
    }

    public enum API {

        VULKAN,
        OPENGL

    }
}
