package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.util.types.ByteSize;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class VertexAttributeList implements ByteSize, Iterable<VertexAttribute> {

    private final Map<Integer, VertexAttribute> attributes;
    private int offset;
    private int stride;
    private boolean instanced;

    public VertexAttributeList() {
        attributes = new LinkedHashMap<>();
    }

    public int stride() {
        return stride;
    }

    public int offset() {
        return offset;
    }

    void offset(int offset) {
        this.offset = offset;
    }

    public int count() {
        return attributes.size();
    }

    @Override
    public int sizeof() {
        return stride;
    }

    public boolean instanced() {
        return instanced;
    }

    void instanced(boolean instanced) {
        this.instanced = instanced;
    }

    public VertexAttribute get(int location) {
        return attributes.get(location);
    }

    void put(int location, VertexAttribute attribute) {
        attributes.put(location, attribute);
        stride += attribute.sizeof();
    }

    @Override
    public VertexAttributeIterator iterator() {
        return new VertexAttributeIterator();
    }

    public Stream<VertexAttribute> attributes() {
        return attributes.values().stream();
    }

    public VertexAttributeList copy() {

        VertexAttributeList copy = new VertexAttributeList();

        copy.attributes.putAll(attributes);
        copy.stride = stride;
        copy.instanced = instanced;

        return copy;
    }

    public final class VertexAttributeIterator implements Iterator<VertexAttribute> {

        private final Iterator<Map.Entry<Integer, VertexAttribute>> iterator;
        private VertexAttribute previous;
        private int location;
        private int offset;

        private VertexAttributeIterator() {
            iterator = attributes.entrySet().iterator();
            offset = VertexAttributeList.this.offset;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public VertexAttribute next() {

            Map.Entry<Integer, VertexAttribute> entry = iterator.next();

            location = entry.getKey();
            VertexAttribute attribute = entry.getValue();

            if(previous != null) {
                offset += previous.sizeof();
            }

            previous = attribute;

            return attribute;
        }

        public int location() {
            return location;
        }

        public int offset() {
            return offset;
        }
    }
}
