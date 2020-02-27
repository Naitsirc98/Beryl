package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.graphics.Graphics;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

public abstract class VertexData implements NativeResource {

    public static Builder builder(VertexLayout layout) {
        return Graphics.graphicsContext().newVertexDataBuilder(layout);
    }

    private final VertexLayout layout;

    protected VertexData(VertexLayout layout) {
        this.layout = layout;
    }

    public final VertexLayout layout() {
        return layout;
    }

    public abstract void bind();

    public static abstract class Builder {

        protected final VertexLayout layout;

        protected Builder(VertexLayout layout) {
            this.layout = requireNonNull(layout);
        }

        public abstract Builder vertices(int binding, ByteBuffer vertices);

        public abstract Builder indices(ByteBuffer indices);

        public abstract VertexData build();
    }
}
