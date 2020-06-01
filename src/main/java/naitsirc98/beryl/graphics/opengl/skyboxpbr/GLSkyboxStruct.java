package naitsirc98.beryl.graphics.opengl.skyboxpbr;

import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxTexture;
import naitsirc98.beryl.util.types.StaticByteSize;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.graphics.textures.Texture.makeResident;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;

/*
 * struct Skybox {
 *
 *     layout(bindless_sampler) samplerCube irradianceMap;
 *     layout(bindless_sampler) samplerCube prefilterMap;
 *     layout(bindless_sampler) sampler2D brdfMap;
 *
 *     float maxPrefilterLOD;
 *     float prefilterLODBias;
 * };
 * */
@StaticByteSize(sizeof = GLSkyboxStruct.SIZEOF)
public final class GLSkyboxStruct implements Resource {

    public static final int SIZEOF = 32;

    private static final int SKYBOX_PRESENT_OFFSET = SIZEOF;

    private static final int SKYBOX_UNIFORM_BUFFER_SIZE = SIZEOF + INT32_SIZEOF;


    private GLBuffer uniformBuffer;

    public GLSkyboxStruct(GLContext context) {
        this.uniformBuffer = new GLBuffer(context).name("Skybox Struct Uniform Buffer");
        uniformBuffer.allocate(SKYBOX_UNIFORM_BUFFER_SIZE);
        uniformBuffer.mapMemory();
    }

    public GLSkyboxStruct update(Skybox skybox) {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer buffer = stack.calloc(SKYBOX_UNIFORM_BUFFER_SIZE);

            final boolean skyboxPresent = skybox != null;

            if(skyboxPresent) {

                final SkyboxTexture skyboxTexture = skybox.texture1();

                buffer.putLong(makeResident(skyboxTexture.irradianceMap()))
                        .putLong(makeResident(skyboxTexture.prefilterMap()))
                        .putLong(makeResident(skybox.brdfTexture()))
                        .putFloat(skybox.maxPrefilterLOD())
                        .putFloat(skybox.prefilterLODBias());
            }

            buffer.putInt(SKYBOX_PRESENT_OFFSET, skyboxPresent ? 1 : 0);

            uniformBuffer.copy(0, buffer.rewind());
        }

        return this;
    }

    public void bind(int binding) {
        uniformBuffer.bind(GL_UNIFORM_BUFFER, binding);
    }

    @Override
    public void release() {
        uniformBuffer.release();
    }
}
