package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.buffers.MappedGraphicsBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLRenderbuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2DMSAA;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.StaticMeshRenderer;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.MeshView;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.StaticMeshManager;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.SceneMeshInfo;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.geometry.Sizec;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static naitsirc98.beryl.core.BerylConfiguration.MSAA_SAMPLES;
import static naitsirc98.beryl.graphics.ShaderStage.*;
import static naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer.DEFAULT_FRAMEBUFFER;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.opengl.ARBIndirectParameters.GL_PARAMETER_BUFFER_ARB;
import static org.lwjgl.opengl.ARBIndirectParameters.glMultiDrawElementsIndirectCountARB;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class GLStaticMeshRenderer extends StaticMeshRenderer {

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;

    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET = 0;
    private static final int TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET = MATRIX4_SIZEOF;

    private static final int VERTEX_BUFFER_BINDING = 0;
    private static final int INSTANCE_BUFFER_BINDING = 1;


    private GLShaderProgram cullingShader;
    private GLShaderProgram renderShader;

    private GLVertexArray vertexArray;
    private GLBuffer instanceBuffer; // model matrix + material + bounding sphere indices

    private GLBuffer transformsBuffer;
    private GLBuffer meshIndicesBuffer;

    private GLBuffer instanceCommandBuffer;
    private GLBuffer atomicCounterBuffer;

    private GLFramebuffer framebuffer;

    GLStaticMeshRenderer() {

    }

    public void init() {

        initVertexArray();

        cullingShader = new GLShaderProgram()
                .attach(new GLShader(COMPUTE_STAGE).source(BerylFiles.getPath("shaders/compute/culling.comp")))
                .link();

        renderShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.frag")))
                .link();

        transformsBuffer = new GLBuffer("TRANSFORMS_STORAGE_BUFFER");
        meshIndicesBuffer = new GLBuffer("MESH_INDICES_STORAGE_BUFFER");

        instanceCommandBuffer = new GLBuffer("INSTANCE_COMMAND_BUFFER");

        atomicCounterBuffer = new GLBuffer("ATOMIC_COUNTER_BUFFER");
        atomicCounterBuffer.allocate(UINT32_SIZEOF);
        atomicCounterBuffer.clear();

        framebuffer = new GLFramebuffer();

        GLTexture2DMSAA colorBuffer = new GLTexture2DMSAA();
        Sizec size = Window.get().size();
        colorBuffer.allocate(MSAA_SAMPLES.get(), size.width(), size.height(), PixelFormat.RGBA);

        GLRenderbuffer depthBuffer = new GLRenderbuffer();
        depthBuffer.storageMultisample(size.width(), size.height(), GL_DEPTH24_STENCIL8, MSAA_SAMPLES.get());

        framebuffer.attach(GL_COLOR_ATTACHMENT0, colorBuffer, 0);
        framebuffer.attach(GL_DEPTH_STENCIL_ATTACHMENT, depthBuffer);

        framebuffer.ensureComplete();

        framebuffer.freeAttachmentsOnRelease(true);
    }

    @Override
    protected void terminate() {

        cullingShader.release();
        renderShader.release();

        instanceBuffer.release();
        vertexArray.release();

        transformsBuffer.release();
        meshIndicesBuffer.release();

        instanceCommandBuffer.release();
    }

    public void prepare(Scene scene) {

        prepareInstanceBuffer(scene.meshInfo());

        StaticMeshManager meshManager = MeshManager.get().staticMeshManager();

        vertexArray.setVertexBuffer(VERTEX_BUFFER_BINDING, meshManager.vertexBuffer(), StaticMesh.VERTEX_DATA_SIZE);
        vertexArray.setIndexBuffer(meshManager.indexBuffer());
    }

    public void render(Scene scene) {
        performCullingPass(scene);
        renderScene(scene);
    }

    private void performCullingPass(Scene scene) {

        final int numObjects = scene.meshInfo().numInstancedMeshViews();

        StaticMeshManager staticMeshManager = MeshManager.get().staticMeshManager();

        GLBuffer meshCommandBuffer = staticMeshManager.commandBuffer();
        GLBuffer boundingSpheresBuffer = staticMeshManager.boundingSpheresBuffer();
        GLBuffer frustumUniformBuffer = scene.cameraInfo().frustumBuffer();

        cullingShader.bind();

        meshCommandBuffer.bind(GL_SHADER_STORAGE_BUFFER, 0);
        instanceCommandBuffer.bind(GL_SHADER_STORAGE_BUFFER, 1);
        boundingSpheresBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);
        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);
        meshIndicesBuffer.bind(GL_SHADER_STORAGE_BUFFER, 4);
        frustumUniformBuffer.bind(GL_UNIFORM_BUFFER, 5);
        atomicCounterBuffer.bind(GL_ATOMIC_COUNTER_BUFFER, 6);

        glDispatchCompute(numObjects, 1, 1);

        glMemoryBarrier(GL_COMMAND_BARRIER_BIT | GL_SHADER_STORAGE_BUFFER | GL_ATOMIC_COUNTER_BARRIER_BIT);
    }

    private void renderScene(Scene scene) {

        final Color clearColor = scene.environment().clearColor();
        final GLBuffer lightsUniformBuffer = scene.environment().buffer();
        final GLBuffer materialsBuffer = MaterialManager.get().buffer();
        final GLBuffer cameraUniformBuffer = scene.cameraInfo().cameraBuffer();

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glClearColor(clearColor.red(), clearColor.green(), clearColor.blue(), clearColor.alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderShader.bind();

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        instanceCommandBuffer.bind(GL_DRAW_INDIRECT_BUFFER);

        atomicCounterBuffer.bind(GL_PARAMETER_BUFFER_ARB);

        vertexArray.bind();

        glMultiDrawElementsIndirectCountARB(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, 0, scene.meshInfo().numInstancedMeshViews(), 0);
    }

    private void prepareInstanceBuffer(SceneMeshInfo meshInfo) {

        final int numObjects = meshInfo.numInstancedMeshViews();

        final int instanceCommandsMinSize = numObjects * GLDrawElementsCommand.SIZEOF;

        if (instanceCommandBuffer.size() < instanceCommandsMinSize) {
            reallocateBuffer(instanceCommandBuffer, instanceCommandsMinSize);
        }

        instanceCommandBuffer.clear();

        final int instancesMinSize = numObjects * INSTANCE_BUFFER_MIN_SIZE;

        if (instanceBuffer.size() < instancesMinSize) {
            reallocateBuffer(instanceBuffer, instancesMinSize);
            vertexArray.setVertexBuffer(INSTANCE_BUFFER_BINDING, instanceBuffer, INSTANCE_BUFFER_MIN_SIZE);
        }

        final long meshIDsMinSize = numObjects * UINT32_SIZEOF;

        if (meshIndicesBuffer.size() < meshIDsMinSize) {
            reallocateBuffer(meshIndicesBuffer, meshIDsMinSize);
        }

        final int transformsMinSize = numObjects * TRANSFORMS_BUFFER_MIN_SIZE;

        if (transformsBuffer.size() < transformsMinSize) {
            reallocateBuffer(transformsBuffer, transformsMinSize);
        }

        final List<MeshInstance> instances = meshInfo.instances();
        int objectIndex = 0;

        for(MeshInstance instance : instances) {

            for (MeshView meshView : instance) {

                if(!(meshView.mesh() instanceof StaticMesh)) {
                    continue;
                }

                final int meshIndex = meshView.mesh().index();

                setInstanceTransform(objectIndex, instance.modelMatrix(), instance.normalMatrix());

                setInstanceMeshIndex(objectIndex, meshIndex);

                final int materialIndex = meshView.material().bufferIndex();

                setInstanceData(objectIndex, objectIndex, materialIndex);

                ++objectIndex;
            }

        }
    }

    private void setInstanceMeshIndex(int objectIndex, int meshIndex) {
        try (MemoryStack stack = stackPush()) {
            meshIndicesBuffer.copy(objectIndex * UINT32_SIZEOF, stack.ints(meshIndex));
        }
    }

    private void setInstanceTransform(int objectIndex, Matrix4fc modelMatrix, Matrix4fc normalMatrix) {
        try(MemoryStack stack = stackPush()) {
            ByteBuffer buffer = stack.malloc(MATRIX4_SIZEOF * 2);
            modelMatrix.get(TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET, buffer);
            normalMatrix.get(TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET, buffer);
            transformsBuffer.copy(objectIndex * TRANSFORMS_BUFFER_MIN_SIZE, buffer);
        }
    }

    private void setInstanceData(int instanceID, int matrixIndex, int materialIndex) {

        try (MemoryStack stack = stackPush()) {

            IntBuffer buffer = stack.mallocInt(2);

            buffer.put(0, matrixIndex).put(1, materialIndex);

            instanceBuffer.copy(instanceID * INSTANCE_BUFFER_MIN_SIZE, buffer);
        }
    }

    private void initVertexArray() {

        vertexArray = new GLVertexArray();

        VertexLayout vertexLayout = new VertexLayout.Builder(2)
                .put(0, 0, POSITION3D, NORMAL, TEXCOORDS2D)
                .put(1, 3, INDEX, INDEX).instanced(1, true)
                .build();

        for (int i = 0; i < vertexLayout.bindings(); i++) {
            vertexArray.setVertexAttributes(i, vertexLayout.attributeList(i));
        }

        instanceBuffer = new GLBuffer("INSTANCE_VERTEX_BUFFER");
    }

    private void reallocateBuffer(MappedGraphicsBuffer buffer, long size) {
        buffer.unmapMemory();
        buffer.reallocate(size);
        buffer.mapMemory();
    }

}
