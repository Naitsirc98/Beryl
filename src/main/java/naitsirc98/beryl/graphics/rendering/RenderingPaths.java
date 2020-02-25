package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Singleton;

import java.util.HashMap;
import java.util.Map;

import static naitsirc98.beryl.graphics.Graphics.opengl;
import static naitsirc98.beryl.graphics.Graphics.vulkan;
import static naitsirc98.beryl.graphics.GraphicsAPI.VULKAN;

public final class RenderingPaths extends BerylSystem {

    public static final int RPATH_SIMPLE = 0;
    public static final int RPATH_DEFAULT = RPATH_SIMPLE;

    @Singleton
    private static RenderingPaths instance;


    public static RenderingPath defaultPath() {
        return instance.renderingPaths.get(instance.defaultPathID);
    }

    public static void defaultPath(int id) {
        if(!instance.renderingPaths.containsKey(id)) {
            Log.error(id + " is not a valid RenderingPath ID");
            return;
        }
        instance.defaultPathID = id;
    }

    public static RenderingPath get(int id) {
        if(!instance.renderingPaths.containsKey(id)) {
            Log.error(id + " is not a valid RenderingPath ID");
            return null;
        }
        return instance.renderingPaths.get(id);
    }

    private final Map<Integer, RenderingPath> renderingPaths;
    private int defaultPathID = RPATH_DEFAULT;

    private RenderingPaths() {
        renderingPaths = new HashMap<>();
    }

    public void put(int index, RenderingPath renderingPath) {
        renderingPaths.put(index, renderingPath);
    }

    @Override
    protected void init() {
        renderingPaths.putAll(getStandardRenderingPaths());
    }

    private Map<? extends Integer,? extends RenderingPath> getStandardRenderingPaths() {
        return GraphicsAPI.get() == VULKAN ? vulkan().renderingPaths() : opengl().renderingPaths();
    }


    @Override
    protected void terminate() {
        for(RenderingPath renderingPath : renderingPaths.values()) {
            if(renderingPath != null && renderingPath.initialized) {
                renderingPath.terminate();
            }
        }
    }
}
