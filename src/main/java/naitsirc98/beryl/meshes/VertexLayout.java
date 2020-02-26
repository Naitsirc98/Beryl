package naitsirc98.beryl.meshes;

import static naitsirc98.beryl.util.Asserts.assertThat;
import static naitsirc98.beryl.util.Asserts.assertTrue;

public final class VertexLayout {

    private final VertexAttributeList[] attributes;

    public VertexLayout() {
        this(1);
    }

    public VertexLayout(int bindings) {
        attributes = createVertexAttributeList(bindings);
    }

    public int bindings() {
        return attributes.length;
    }

    public VertexLayout put(int binding, VertexAttribute attribute) {
        assertTrue(binding >= 0);
        assertTrue(binding < bindings());
        attributes[binding].add(attribute);
        return this;
    }

    private VertexAttributeList[] createVertexAttributeList(int bindings) {

        VertexAttributeList[] attributes = new VertexAttributeList[assertThat(bindings, bindings >= 0)];

        for(int i = 0;i < bindings;i++) {
            attributes[i] = new VertexAttributeList();
        }

        return attributes;
    }
}
