package naitsirc98.beryl.graphics.opengl.commands;

import naitsirc98.beryl.graphics.buffers.GraphicsCPUBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import org.lwjgl.PointerBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.ByteSizeUtils.sizeof;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;
import static org.lwjgl.opengl.GL45.glUnmapNamedBuffer;
import static org.lwjgl.opengl.GL45.nglMapNamedBufferRange;

public class GLCommandBuffer<T extends GLDrawCommand> extends GLBuffer implements GraphicsCPUBuffer {

    private final Class<T> drawCommandType;
    private final int sizeofCommand;
    private int count;

    public GLCommandBuffer(Class<T> drawCommandType) {
        this.drawCommandType = requireNonNull(drawCommandType);
        sizeofCommand = sizeof(drawCommandType);
    }

    public void allocateCommands(int count) {
        this.count = count;
        allocate(count * sizeofCommand);
    }

    public GLDrawElementsCommand.Iterator commands() {
        return new GLDrawElementsCommand.Iterator(this);
    }

    public void bind() {
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, handle());
    }

    public int count() {
        return count;
    }

    @Override
    protected int storageFlags() {
        return GL_DYNAMIC_STORAGE_BIT | GL_MAP_WRITE_BIT;
    }

    @Override
    public Type type() {
        return Type.STORAGE_BUFFER;
    }

    @Override
    public PointerBuffer mapMemory(long offset) {
        return PointerBuffer.allocateDirect(1)
                .put(nglMapNamedBufferRange(handle(), offset, size(), GL_MAP_WRITE_BIT));
    }

    @Override
    public void unmapMemory() {
        glUnmapNamedBuffer(handle());
    }
}
