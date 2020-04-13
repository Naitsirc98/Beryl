package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.buffers.MappedGraphicsBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.StaticMeshRenderer;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.MeshView;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.StaticMeshManager;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Fog;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnvironment;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.SceneMeshInfo;
import naitsirc98.beryl.util.Color;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.*;
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

    // EXPERIMENTAL

    private static final int CAMERA_UNIFORM_BUFFER_SIZE = MATRIX4_SIZEOF + VECTOR4_SIZEOF;
    private static final int CAMERA_UNIFORM_BUFFER_PROJECTION_VIEW_OFFSET = 0;
    private static final int CAMERA_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET = MATRIX4_SIZEOF;

    private static final int FRUSTUM_UNIFORM_BUFFER_SIZE = MATRIX4_SIZEOF + 6 * VECTOR4_SIZEOF;
    private static final int FRUSTUM_UNIFORM_BUFFER_PROJECTION_VIEW_OFFSET = 0;
    private static final int FRUSTUM_UNIFORM_BUFFER_PLANES_OFFSET = MATRIX4_SIZEOF;

    private static final int MAX_POINT_LIGHTS = 10;
    private static final int MAX_SPOT_LIGHTS = 10;
    private static final int LIGHTS_UNIFORM_BUFFER_SIZE = Light.SIZEOF + MAX_POINT_LIGHTS * Light.SIZEOF + MAX_SPOT_LIGHTS * Light.SIZEOF + VECTOR4_SIZEOF + Fog.SIZEOF + 2 * INT32_SIZEOF;
    private static final int DIRECTIONAL_LIGHT_OFFSET = 0;
    private static final int POINT_LIGHTS_OFFSET = Light.SIZEOF;
    private static final int SPOT_LIGHTS_OFFSET = POINT_LIGHTS_OFFSET + Light.SIZEOF * MAX_POINT_LIGHTS;
    private static final int AMBIENT_COLOR_OFFSET = SPOT_LIGHTS_OFFSET + Light.SIZEOF * MAX_SPOT_LIGHTS;
    private static final int FOG_OFFSET = AMBIENT_COLOR_OFFSET + FLOAT32_SIZEOF * 4;
    private static final int POINT_LIGHTS_COUNT_OFFSET = FOG_OFFSET + Fog.SIZEOF;
    private static final int SPOT_LIGHTS_COUNT_OFFSET = POINT_LIGHTS_COUNT_OFFSET + INT32_SIZEOF;

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;

    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET = 0;
    private static final int TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET = MATRIX4_SIZEOF;

    private static final int VERTEX_BUFFER_BINDING = 0;
    private static final int INSTANCE_BUFFER_BINDING = 1;


    private GLShaderProgram cullingShader;
    private GLShaderProgram renderShader;

    private GLBuffer frustumUniformBuffer;
    private GLBuffer cameraUniformBuffer;
    private GLBuffer lightsUniformBuffer;

    private GLVertexArray vertexArray;
    private GLBuffer instanceBuffer; // model matrix + material + bounding sphere indices

    private GLBuffer transformsBuffer;
    private GLBuffer meshIndicesBuffer;

    private GLBuffer instanceCommandBuffer;
    private GLBuffer atomicCounterBuffer;

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

        frustumUniformBuffer = new GLBuffer("FRUSTUM_UNIFORM_BUFFER");
        frustumUniformBuffer.allocate(FRUSTUM_UNIFORM_BUFFER_SIZE);

        cameraUniformBuffer = new GLBuffer("CAMERA_UNIFORM_BUFFER");
        cameraUniformBuffer.allocate(CAMERA_UNIFORM_BUFFER_SIZE);

        lightsUniformBuffer = new GLBuffer("LIGHTS_UNIFORM_BUFFER");
        lightsUniformBuffer.allocate(LIGHTS_UNIFORM_BUFFER_SIZE);

        transformsBuffer = new GLBuffer("TRANSFORMS_STORAGE_BUFFER");
        meshIndicesBuffer = new GLBuffer("MESH_INDICES_STORAGE_BUFFER");

        instanceCommandBuffer = new GLBuffer("INSTANCE_COMMAND_BUFFER");

        atomicCounterBuffer = new GLBuffer("ATOMIC_COUNTER_BUFFER");
        atomicCounterBuffer.allocate(UINT32_SIZEOF);
        atomicCounterBuffer.clear();

        frustumUniformBuffer.mapMemory();
        cameraUniformBuffer.mapMemory();
        lightsUniformBuffer.mapMemory();
    }

    @Override
    protected void terminate() {

        cullingShader.release();
        renderShader.release();

        frustumUniformBuffer.release();
        cameraUniformBuffer.release();
        lightsUniformBuffer.release();

        instanceBuffer.release();
        vertexArray.release();

        transformsBuffer.release();
        meshIndicesBuffer.release();

        instanceCommandBuffer.release();
    }

    public void prepare(Scene scene) {
        Camera camera = scene.camera();
        SceneMeshInfo meshInfo = scene.meshInfo();
        prepareBuffers(meshInfo);
        setLightsUniformBuffer(scene.environment());
        setCameraUniformBuffer(camera);
        setFrustumUniformBuffer(camera);
    }

    public void render(Scene scene) {
        performCullingPass(scene.meshInfo().numInstancedMeshViews());
        render(scene.environment().clearColor(), scene.meshInfo());
    }

    private void performCullingPass(int numObjects) {

        StaticMeshManager staticMeshManager = MeshManager.get().staticMeshManager();

        GLBuffer meshCommandBuffer = staticMeshManager.commandBuffer();
        GLBuffer boundingSpheresBuffer = staticMeshManager.boundingSpheresBuffer();

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

    private void render(Color clearColor, SceneMeshInfo meshInfo) {

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        // glEnable(GL_CULL_FACE);
        glClearColor(clearColor.red(), clearColor.green(), clearColor.blue(), clearColor.alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderShader.bind();

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);

        GLBuffer materialsBuffer = MaterialManager.get().buffer();

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        instanceCommandBuffer.bind(GL_DRAW_INDIRECT_BUFFER);

        atomicCounterBuffer.bind(GL_PARAMETER_BUFFER_ARB);

        vertexArray.bind();

        glMultiDrawElementsIndirectCountARB(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, 0, meshInfo.numInstancedMeshViews(), 0);
    }

    private void prepareBuffers(SceneMeshInfo meshInfo) {

        prepareInstanceBuffer(meshInfo);

        StaticMeshManager meshManager = MeshManager.get().staticMeshManager();

        vertexArray.setVertexBuffer(VERTEX_BUFFER_BINDING, meshManager.vertexBuffer(), StaticMesh.VERTEX_DATA_SIZE);
        vertexArray.setIndexBuffer(meshManager.indexBuffer());
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

    private void setCameraUniformBuffer(Camera camera) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(CAMERA_UNIFORM_BUFFER_SIZE);

            camera.projectionViewMatrix().get(CAMERA_UNIFORM_BUFFER_PROJECTION_VIEW_OFFSET, buffer);
            camera.position().get(CAMERA_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET, buffer);

            cameraUniformBuffer.copy(0, buffer);
        }
    }

    private void setFrustumUniformBuffer(Camera camera) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(FRUSTUM_UNIFORM_BUFFER_SIZE);

            camera.projectionViewMatrix().get(FRUSTUM_UNIFORM_BUFFER_PROJECTION_VIEW_OFFSET, buffer);

            for (int i = 0; i < 6; i++) {
                camera.frustumPlanes()[i].get(FRUSTUM_UNIFORM_BUFFER_PLANES_OFFSET + i * VECTOR4_SIZEOF, buffer);
            }

            frustumUniformBuffer.copy(0, buffer);
        }
    }

    private void setLightsUniformBuffer(SceneEnvironment environment) {

        final DirectionalLight directionalLight = environment.directionalLight();
        final int pointLightsCount = environment.pointLightsCount();
        final int spotLightsCount = environment.spotLightsCount();

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(LIGHTS_UNIFORM_BUFFER_SIZE);

            if (directionalLight != null) {
                directionalLight.get(DIRECTIONAL_LIGHT_OFFSET, buffer);
            }

            if (pointLightsCount > 0) {
                for (int i = 0; i < pointLightsCount; i++) {
                    environment.pointLight(i).get(POINT_LIGHTS_OFFSET + i * Light.SIZEOF, buffer);
                }
            }

            if (spotLightsCount > 0) {
                for (int i = 0; i < spotLightsCount; i++) {
                    environment.spotLight(i).get(SPOT_LIGHTS_OFFSET + i * Light.SIZEOF, buffer);
                }
            }

            environment.ambientColor().getRGBA(AMBIENT_COLOR_OFFSET, buffer);
            environment.fog().get(FOG_OFFSET, buffer);
            buffer.putInt(POINT_LIGHTS_COUNT_OFFSET, pointLightsCount);
            buffer.putInt(SPOT_LIGHTS_COUNT_OFFSET, spotLightsCount);

            lightsUniformBuffer.copy(0, buffer);
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
