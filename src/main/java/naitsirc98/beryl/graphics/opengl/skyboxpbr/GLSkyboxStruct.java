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


    private GLBuffer uniformBuffer;

    public GLSkyboxStruct(GLContext context) {
        this.uniformBuffer = new GLBuffer(context).name("Skybox Struct Uniform Buffer");
        uniformBuffer.allocate(SIZEOF);
        uniformBuffer.mapMemory();
    }

    public GLSkyboxStruct update(Skybox skybox) {

        if(skybox == null) {
            return this;
        }

        final SkyboxTexture skyboxTexture = skybox.texture1();

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer buffer = stack.malloc(SIZEOF);

            buffer.putLong(makeResident(skyboxTexture.irradianceMap()))
                    .putLong(makeResident(skyboxTexture.prefilterMap()))
                    .putLong(makeResident(skybox.brdfTexture()))
                    .putFloat(skybox.maxPrefilterLOD())
                    .putFloat(skybox.prefilterLODBias());

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
