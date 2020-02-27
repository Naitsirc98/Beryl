package naitsirc98.beryl.meshes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

public final class VertexAttributeList implements Iterable<VertexAttribute> {

    private final ArrayList<VertexAttribute> attributes;
    private int stride;
    private boolean instancing;

    public VertexAttributeList() {
        attributes = new ArrayList<>(3);
    }

    public int stride() {
        return stride;
    }

    public boolean instancing() {
        return instancing;
    }

    void instancing(boolean instancing) {
        this.instancing = instancing;
    }

    public VertexAttribute get(int location) {
        return attributes.get(location);
    }

    void add(VertexAttribute attribute) {
        attributes.add(attribute);
        stride += attribute.sizeof();
    }

    @Override
    public Iterator<VertexAttribute> iterator() {
        return new VertexAttributeListIterator();
    }

    public Stream<VertexAttribute> attributes() {
        return attributes.stream();
    }

    public VertexAttributeList copy() {

        VertexAttributeList copy = new VertexAttributeList();

        copy.attributes.addAll(attributes);
        copy.attributes.trimToSize();
        copy.stride = stride;
        copy.instancing = instancing;

        return copy;
    }

    public final class VertexAttributeListIterator implements Iterator<VertexAttribute> {

        private final Iterator<VertexAttribute> iterator;
        private VertexAttribute previous;
        private int offset;

        private VertexAttributeListIterator() {
            iterator = attributes.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public VertexAttribute next() {

            VertexAttribute attribute = iterator.next();

            if(previous != null) {
                offset += previous.sizeof();
            }

            previous = attribute;

            return attribute;
        }

        public int offset() {
            return offset;
        }
    }
}
