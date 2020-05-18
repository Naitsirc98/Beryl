package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.util.types.ByteSize;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static naitsirc98.beryl.util.Asserts.*;

public final class VertexLayout implements ByteSize {

    private final VertexAttributeList[] attributes;
    private final int sizeof;
    private final boolean instanced;

    public VertexLayout(VertexAttributeList[] attributes) {
        this.attributes = attributes;
        sizeof = Arrays.stream(attributes).mapToInt(VertexAttributeList::sizeof).sum();
        instanced = Arrays.stream(attributes).anyMatch(VertexAttributeList::instanced);
    }

    public VertexAttributeList attributeList(int binding) {
        assertTrue(binding >= 0);
        assertTrue(binding < bindings());
        return attributes[binding];
    }

    public Stream<VertexAttributeList> attributeLists() {
        return Arrays.stream(attributes);
    }

    public Stream<VertexAttribute> attributeList() {
        return Arrays.stream(attributes).map(VertexAttributeList::attributes).reduce(Stream::concat).orElse(Stream.empty());
    }

    public int bindings() {
        return attributes.length;
    }

    @Override
    public int sizeof() {
        return sizeof;
    }

    public boolean instanced() {
        return instanced;
    }

    public static final class Builder {

        private final VertexAttributeList[] attributes;

        public Builder() {
            this(1);
        }

        public Builder(int bindings) {
            attributes = new VertexAttributeList[bindings];

            for(int i = 0;i < bindings;i++) {
                attributes[i] = new VertexAttributeList();
            }
        }

        public Builder(VertexLayout other) {
            assertNonNull(other);
            attributes = new VertexAttributeList[other.bindings()];
            for(int i = 0;i < attributes.length;i++) {
                attributes[i] = other.attributes[i].copy();
            }
        }

        public int bindings() {
            return attributes.length;
        }

        public Builder instanced(int binding, boolean instanced) {
            assertTrue(binding >= 0);
            assertTrue(binding < bindings());
            attributes[binding].instanced(instanced);
            return this;
        }

        public Builder offset(int binding, int offset) {
            assertTrue(binding >= 0);
            assertTrue(binding < bindings());
            attributes[binding].offset(offset);
            return this;
        }

        public Builder put(int binding, int location, VertexAttribute attribute) {
            assertTrue(binding >= 0);
            assertTrue(binding < bindings());
            attributes[binding].put(location, attribute);
            return this;
        }

        public Builder put(int binding, int startingLocation, VertexAttribute... attributes) {
            assertTrue(binding >= 0);
            assertTrue(binding < bindings());

            VertexAttributeList list = this.attributes[binding];

            for(int i = 0;i < attributes.length;i++) {
                list.put(startingLocation + i, attributes[i]);
            }

            return this;
        }

        public VertexLayout build() {
            return new VertexLayout(attributes);
        }

        private VertexAttributeList[] createVertexAttributeList(int bindings) {

            VertexAttributeList[] attributes = new VertexAttributeList[assertThat(bindings, bindings >= 0)];

            for(int i = 0;i < bindings;i++) {
                attributes[i] = new VertexAttributeList();
            }

            return attributes;
        }
    }
}
