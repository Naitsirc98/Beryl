package naitsirc98.beryl.graphics.opengl.commands;

import naitsirc98.beryl.util.types.ByteSize;

import static naitsirc98.beryl.util.types.DataType.UINT32_SIZEOF;
import static org.lwjgl.system.MemoryUtil.memGetInt;
import static org.lwjgl.system.MemoryUtil.memPutInt;

@ByteSize.Static(GLDrawArraysCommand.SIZEOF)
public final class GLDrawArraysCommand implements GLDrawCommand {

    public static final int SIZEOF = UINT32_SIZEOF * 4;

    /*
        typedef struct {
            GLuint  count;
            GLuint  primCount;
            GLuint  first;
            GLuint  baseInstance;
        } DrawArraysIndirectCommand;
    */

    private static final int COUNT_OFFSET = 0;
    private static final int PRIM_COUNT_OFFSET = COUNT_OFFSET + UINT32_SIZEOF;
    private static final int FIRST_OFFSET = PRIM_COUNT_OFFSET + UINT32_SIZEOF;
    private static final int BASE_INSTANCE_OFFSET = FIRST_OFFSET + UINT32_SIZEOF;

    private final long address;

    public GLDrawArraysCommand(long address) {
        this.address = address;
    }

    public int count() {
        return memGetInt(address + COUNT_OFFSET);
    }

    public GLDrawArraysCommand count(int count) {
        memPutInt(address + COUNT_OFFSET, count);
        return this;
    }

    public int primCount() {
        return memGetInt(address + PRIM_COUNT_OFFSET);
    }

    public GLDrawArraysCommand primCount(int primCount) {
        memPutInt(address + PRIM_COUNT_OFFSET, primCount);
        return this;
    }

    public int first() {
        return memGetInt(address + FIRST_OFFSET);
    }

    public GLDrawArraysCommand first(int first) {
        memPutInt(address + FIRST_OFFSET, first);
        return this;
    }

    public int baseInstance() {
        return memGetInt(address + BASE_INSTANCE_OFFSET);
    }

    public GLDrawArraysCommand baseInstance(int baseInstance) {
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
