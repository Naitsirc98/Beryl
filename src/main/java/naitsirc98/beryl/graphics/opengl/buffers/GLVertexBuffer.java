package naitsirc98.beryl.graphics.opengl.buffers;

import static org.lwjgl.opengl.GL11.GL_NONE;

public class GLVertexBuffer extends GLBuffer {

    @Override
    public Type type() {
        return Type.VERTEX_BUFFER;
    }

    @Override
    protected int storageFlags() {
        return GL_NONE;
    }
}
