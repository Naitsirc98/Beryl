package naitsirc98.beryl.graphics.shaders;

import naitsirc98.beryl.resources.Resource;

import java.nio.ByteBuffer;

import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_release;

public final class SPIRVBytecode implements Resource {

    private final long handle;
    private ByteBuffer bytecode;

    public SPIRVBytecode(long handle, ByteBuffer bytecode) {
        this.handle = handle;
        this.bytecode = bytecode;
    }

    public ByteBuffer bytecode() {
        return bytecode;
    }

    @Override
    public void release() {
        shaderc_result_release(handle);
        bytecode = null; // Help the GC
    }
}
