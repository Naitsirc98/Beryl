package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.resources.ManagedResource;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

public abstract class VertexData extends ManagedResource {

    public static Builder builder(VertexLayout layout) {
        return GraphicsFactory.get().newVertexDataBuilder(layout);
    }

    protected final VertexLayout layout;
    protected int firstVertex;
    protected int vertexCount;
    protected int indexCount;

    protected VertexData(VertexLayout layout, int firstVertex, int vertexCount, int indexCount) {
        this.layout = layout;
        this.firstVertex = firstVertex;
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
    }

    public final VertexLayout layout() {
        return layout;
    }

    public final int firstVertex() {
        return firstVertex;
    }

    public final int vertexCount() {
        return vertexCount;
    }

    public final int indexCount() {
        return indexCount;
    }


    public static abstract class Builder {

        protected final VertexLayout layout;
        protected int firstVertex;
        private int bindingIndex;

        protected Builder(VertexLayout layout) {
            this.layout = requireNonNull(layout);
        }

        public final VertexLayout vertexLayout() {
            return vertexLayout();
        }

        public final Builder firstVertex(int firstVertex) {
            this.firstVertex = firstVertex;
            return this;
        }

        public final Builder vertices(ByteBuffer vertices) {
            assertTrue(bindingIndex < layout.bindings());
            return vertices(bindingIndex++, vertices);
        }

        public abstract Builder vertices(int binding, ByteBuffer vertices);

        public abstract Builder indices(ByteBuffer indices);

        public abstract VertexData build();

        protected final int getVertexCount(ByteBuffer[] vertices) {

            int vertexCount = 0;

            for(int i = 0;i < vertices.length;i++) {
                vertexCount += vertices[i].remaining() / layout.attributeList(i).sizeof();
            }

            return vertexCount;
        }
    }
}
