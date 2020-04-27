package naitsirc98.beryl.meshes;

public class MeshStorageInfo {

    private int index;
    private long vertexBufferOffset;
    private long indexBufferOffset;
    private int baseVertex;
    private int firstIndex;

    protected MeshStorageInfo() {

    }

    public int index() {
        return index;
    }

    void index(int index) {
        this.index = index;
    }

    public long vertexBufferOffset() {
        return vertexBufferOffset;
    }

    void vertexBufferOffset(long vertexBufferOffset) {
        this.vertexBufferOffset = vertexBufferOffset;
    }

    public long indexBufferOffset() {
        return indexBufferOffset;
    }

    void indexBufferOffset(long indexBufferOffset) {
        this.indexBufferOffset = indexBufferOffset;
    }

    public int baseVertex() {
        return baseVertex;
    }

    void baseVertex(int baseVertex) {
        this.baseVertex = baseVertex;
    }

    public int firstIndex() {
        return firstIndex;
    }

    void firstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }
}
