package naitsirc98.beryl.meshes;

public class BoneStorageInfo {

    private int index;
    private long bonesBufferOffset;

    public BoneStorageInfo() {

    }

    public int index() {
        return index;
    }

    void index(int index) {
        this.index = index;
    }

    public long bonesBufferOffset() {
        return bonesBufferOffset;
    }

    void bonesBufferOffset(long bonesBufferOffset) {
        this.bonesBufferOffset = bonesBufferOffset;
    }
}
