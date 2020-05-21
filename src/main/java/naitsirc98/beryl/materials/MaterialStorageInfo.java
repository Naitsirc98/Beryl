package naitsirc98.beryl.materials;

public class MaterialStorageInfo {

    // Size in bytes of the material
    private int materialSizeof;
    // This is the index of the material in the material's type list
    private int index;
    // Offset of the material into the materials buffer
    private long offset;

    public MaterialStorageInfo(final int materialSizeof) {
        this.materialSizeof = materialSizeof;
    }

    int materialSizeof() {
        return materialSizeof;
    }

    public int index() {
        return index;
    }

    void index(int index) {
        this.index = index;
    }

    public int bufferIndex() {
        return (int) (offset / materialSizeof);
    }

    public long offset() {
        return offset;
    }

    void offset(long offset) {
        this.offset = offset;
    }
}
