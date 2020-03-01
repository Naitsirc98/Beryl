package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.util.GrowableBuffer;

import java.nio.ByteBuffer;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public final class VertexView<T extends Vertex> implements Iterable<Vertex>, Iterator<Vertex> {

    public static <V extends Vertex> VertexView<V> of(Class<V> vertexClass, GrowableBuffer buffer) {
        return new VertexView<>(vertexClass, buffer.data());
    }

    public static <V extends Vertex> VertexView<V> of(Class<V> vertexClass, ByteBuffer buffer) {
        return new VertexView<>(vertexClass, buffer);
    }

    private final ByteBuffer data;
    private final Vertex vertex;
    private final int capacity;
    private int position;

    public VertexView(Class<T> vertexClass, ByteBuffer data) {
        vertex = newInstance(vertexClass);
        this.data = requireNonNull(data);
        capacity = data.limit() / vertex.sizeof();
    }

    public ByteBuffer data() {
        return data;
    }

    @Override
    public boolean hasNext() {
        return position < capacity;
    }

    @Override
    public Vertex next() {
        assertTrue(hasNext());

        if(position > 0) {
            vertex.get(position - 1, data);
        }

        vertex.set(position++, data);

        return vertex;
    }

    @Override
    public Iterator<Vertex> iterator() {
        return this;
    }
}
