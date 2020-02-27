package naitsirc98.beryl.meshes;

import naitsirc98.beryl.meshes.vertices.VertexData;

public class Mesh {

    // TODO: material

    private final VertexData vertexData;

    public Mesh(VertexData vertexData) {
        this.vertexData = vertexData;
    }

    public final VertexData vertexData() {
        return vertexData;
    }
}
