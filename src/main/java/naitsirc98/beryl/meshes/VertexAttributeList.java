package naitsirc98.beryl.meshes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class VertexAttributeList implements Iterable<VertexAttribute> {

    private final List<VertexAttribute> attributes;
    private int stride;

    public VertexAttributeList() {
        attributes = new ArrayList<>(3);
    }

    public int stride() {
        return stride;
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
    }
}
