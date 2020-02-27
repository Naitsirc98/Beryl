package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.meshes.vertices.VertexData;
import org.lwjgl.system.NativeResource;

import java.util.Map;

public interface GraphicsContext extends NativeResource {

    void init();

    Renderer renderer();

    Map<Integer, RenderingPath> renderingPaths();

    VertexData.Builder newVertexDataBuilder();
}
