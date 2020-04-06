package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.rendering.PrimitiveTopology;
import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.util.types.DataType;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.rendering.PrimitiveTopology.TRIANGLES;
import static naitsirc98.beryl.meshes.vertices.VertexLayout.VERTEX_LAYOUT_3D;
import static naitsirc98.beryl.util.Asserts.assertTrue;

public abstract class VertexData extends ManagedResource {

    public static Builder builder() {
        return GraphicsFactory.get().newVertexDataBuilder(VERTEX_LAYOUT_3D, TRIANGLES);
    }

    public static Builder builder(VertexLayout layout) {
        return GraphicsFactory.get().newVertexDataBuilder(layout, TRIANGLES);
    }

    public static Builder builder(VertexLayout layout, PrimitiveTopology primitiveTopology) {
        return GraphicsFactory.get().newVertexDataBuilder(layout, primitiveTopology);
    }

    private final VertexLayout layout;
    private final PrimitiveTopology primitiveTopology;
    private final int firstVertex;
    private final int vertexCount;
    private final int indexCount;

    protected VertexData(VertexLayout layout, PrimitiveTopology primitiveTopology, int firstVertex, int vertexCount, int indexCount) {
        this.layout = layout;
        this.primitiveTopology = primitiveTopology;
        this.firstVertex = firstVertex;
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
    }

    public final VertexLayout layout() {
        return layout;
    }

    public final PrimitiveTopology topology() {
        return primitiveTopology;
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

    public boolean instanced() {
        return layout.instanced();
    }

    public static abstract class Builder {

        protected final VertexLayout layout;
        protected final PrimitiveTopology primitiveTopology;
        protected int firstVertex;
        protected int indexCount;
        private int bindingIndex;

        protected Builder(VertexLayout layout, PrimitiveTopology primitiveTopology) {
            this.layout = requireNonNull(layout);
            this.primitiveTopology = primitiveTopology;
        }

        public final VertexLayout vertexLayout() {
            return vertexLayout();
        }

        public final PrimitiveTopology topology() {
            return primitiveTopology;
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

        public abstract Builder indices(ByteBuffer indices, DataType indexType);

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
