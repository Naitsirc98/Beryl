package naitsirc98.beryl.meshes;

import java.nio.ByteBuffer;

public final class MeshData {

    private ByteBuffer vertices;
    private ByteBuffer indices;

    public void set(ByteBuffer vertices, ByteBuffer indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public ByteBuffer vertices() {
        return vertices;
    }

    public ByteBuffer indices() {
        return indices;
    }
}
