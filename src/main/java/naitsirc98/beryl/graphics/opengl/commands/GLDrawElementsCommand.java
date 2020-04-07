package naitsirc98.beryl.graphics.opengl.commands;

import naitsirc98.beryl.util.types.ByteSize;

import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.UINT32_SIZEOF;
import static org.lwjgl.system.MemoryUtil.memGetInt;
import static org.lwjgl.system.MemoryUtil.memPutInt;

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

    private long address;

    public GLDrawElementsCommand(long address) {
        this.address = address;
    }

    public int count() {
        return memGetInt(address + COUNT_OFFSET);
    }

    public GLDrawElementsCommand count(int count) {
        memPutInt(address + COUNT_OFFSET, count);
        return this;
    }

    public int primCount() {
        return memGetInt(address + PRIM_COUNT_OFFSET);
    }

    public GLDrawElementsCommand primCount(int primCount) {
        memPutInt(address + PRIM_COUNT_OFFSET, primCount);
        return this;
    }

    public int firstIndex() {
        return memGetInt(address + FIRST_INDEX_OFFSET);
    }

    public GLDrawElementsCommand firstIndex(int firstIndex) {
        memPutInt(address + FIRST_INDEX_OFFSET, firstIndex);
        return this;
    }

    public int baseVertex() {
        return memGetInt(address + BASE_VERTEX_OFFSET);
    }

    public GLDrawElementsCommand baseVertex(int baseVertex) {
        memPutInt(address + BASE_VERTEX_OFFSET, baseVertex);
        return this;
    }

    public int baseInstance() {
        return memGetInt(address + BASE_INSTANCE_OFFSET);
    }

    public GLDrawElementsCommand baseInstance(int baseInstance) {
        memPutInt(address + BASE_INSTANCE_OFFSET, baseInstance);
        return this;
    }

    public long address() {
        return address;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }
}