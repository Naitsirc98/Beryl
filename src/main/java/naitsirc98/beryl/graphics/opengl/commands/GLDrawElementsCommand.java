package naitsirc98.beryl.graphics.opengl.commands;

import naitsirc98.beryl.util.types.ByteSize;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.UINT32_SIZEOF;
import static org.lwjgl.system.MemoryUtil.memAddress0;

@ByteSize.Static(GLDrawElementsCommand.SIZEOF)
public final class GLDrawElementsCommand implements GLDrawCommand {

    public static final int SIZEOF = UINT32_SIZEOF * 5;

    /*
        typedef  struct {
            GLuint  count;
            GLuint  primCount;
            GLuint  firstIndex;
            GLint   baseVertex;
            GLuint  baseInstance;
        } DrawElementsIndirectCommand;
    */

    private static final int COUNT_OFFSET = 0;
    private static final int PRIM_COUNT_OFFSET = COUNT_OFFSET + UINT32_SIZEOF;
    private static final int FIRST_INDEX_OFFSET = PRIM_COUNT_OFFSET + UINT32_SIZEOF;
    private static final int BASE_VERTEX_OFFSET = FIRST_INDEX_OFFSET + UINT32_SIZEOF;
    private static final int BASE_INSTANCE_OFFSET = BASE_VERTEX_OFFSET + INT32_SIZEOF;

    public static GLDrawElementsCommand callocStack(MemoryStack stack) {
        return new GLDrawElementsCommand(stack.calloc(SIZEOF));
    }

    private final long address;
    private final ByteBuffer buffer;

    public GLDrawElementsCommand(ByteBuffer buffer) {
        assertTrue(buffer.capacity() >= GLDrawElementsCommand.SIZEOF);
        this.buffer = requireNonNull(buffer);
        this.address = memAddress0(buffer);
    }

    public int count() {
        return buffer.getInt(COUNT_OFFSET);
    }

    public GLDrawElementsCommand count(int count) {
        buffer.putInt(COUNT_OFFSET, count);
        return this;
    }

    public int primCount() {
        return buffer.getInt(PRIM_COUNT_OFFSET);
    }

    public GLDrawElementsCommand primCount(int primCount) {
        buffer.putInt(PRIM_COUNT_OFFSET, primCount);
        return this;
    }

    public int firstIndex() {
        return buffer.getInt(FIRST_INDEX_OFFSET);
    }

    public GLDrawElementsCommand firstIndex(int firstIndex) {
        buffer.putInt(FIRST_INDEX_OFFSET, firstIndex);
        return this;
    }

    public int baseVertex() {
        return buffer.getInt(BASE_VERTEX_OFFSET);
    }

    public GLDrawElementsCommand baseVertex(int baseVertex) {
        buffer.putInt(BASE_VERTEX_OFFSET, baseVertex);
        return this;
    }

    public int baseInstance() {
        return buffer.getInt(BASE_INSTANCE_OFFSET);
    }

    public GLDrawElementsCommand baseInstance(int baseInstance) {
        buffer.putInt(BASE_INSTANCE_OFFSET, baseInstance);
        return this;
    }

    public long address() {
        return address;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @Override
    public String toString() {
        return "GLDrawElementsCommand {\n" +
                "count = " + count() + "\n" +
                "primCount = " + primCount() + "\n" +
                "firstIndex = " + firstIndex() + "\n" +
                "baseVertex = " + baseVertex() + "\n" +
                "baseInstance = " + baseInstance() + "\n" +
                "\n}";
    }

    public ByteBuffer buffer() {
        return buffer;
    }
}