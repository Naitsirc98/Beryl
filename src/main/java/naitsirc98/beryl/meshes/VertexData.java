package naitsirc98.beryl.meshes;

import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;

public abstract class VertexData implements NativeResource {

    private final VertexLayout layout;

    public VertexData(VertexLayout layout) {
        this.layout = layout;
    }

    public final VertexLayout layout() {
        return layout;
    }

    public abstract VertexData data(ByteBuffer data);

}
