package naitsirc98.beryl.graphics.opengl.buffers;

public class GLIndexBuffer extends GLBuffer {

    @Override
    public Type type() {
        return Type.INDEX_BUFFER;
    }

    @Override
    protected int storageFlags() {
        return 0;
    }
}
