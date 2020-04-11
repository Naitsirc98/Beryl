package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.buffers.GraphicsMappableBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLIndexBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLStorageBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLUniformBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLVertexBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshView;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnvironment;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.SceneMeshInfo;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.geometry.AABB;
import naitsirc98.beryl.util.geometry.ISphere;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.List;

import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.graphics.ShaderStage.*;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.system.libc.LibCString.nmemset;

public class Rendering extends RenderingPath {

    // EXPERIMENTAL

    private static final int CAMERA_UNIFORM_BUFFER_SIZE = (16 + 4) * FLOAT32_SIZEOF;
    private static final String CAMERA_UNIFORM_BUFFER_NAME = "Camera";
    private static final int CAMERA_UNIFORM_BUFFER_PROJECTION_VIEW_OFFSET = 0;
    private static final int CAMERA_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET = 16 * FLOAT32_SIZEOF;

    private static final int FRUSTUM_UNIFORM_BUFFER_SIZE = (16 + 4 * 6) * FLOAT32_SIZEOF;
    private static final String FRUSTUM_UNIFORM_BUFFER_NAME = "Frustum";
    private static final int FRUSTUM_UNIFORM_BUFFER_PROJECTION_VIEW_OFFSET = 0;
    private static final int FRUSTUM_UNIFORM_BUFFER_PLANES_OFFSET = 16 * FLOAT32_SIZEOF;

    private static final int MAX_POINT_LIGHTS = 10;
    private static final int MAX_SPOT_LIGHTS = 10;
    private static final int LIGHTS_UNIFORM_BUFFER_SIZE = (1 + MAX_POINT_LIGHTS + MAX_SPOT_LIGHTS) * Light.SIZEOF + INT32_SIZEOF * 2 + FLOAT32_SIZEOF * 4;
    private static final String LIGHTS_UNIFORM_BUFFER_NAME = "Lights";
    private static final int DIRECTIONAL_LIGHT_OFFSET = 0;
    private static final int POINT_LIGHTS_OFFSET = Light.SIZEOF;
    private static final int SPOT_LIGHTS_OFFSET = POINT_LIGHTS_OFFSET + Light.SIZEOF * MAX_POINT_LIGHTS;
    private static final int AMBIENT_COLOR_OFFSET = SPOT_LIGHTS_OFFSET + Light.SIZEOF * MAX_SPOT_LIGHTS;
    private static final int POINT_LIGHTS_COUNT_OFFSET = AMBIENT_COLOR_OFFSET + FLOAT32_SIZEOF * 4;
    private static final int SPOT_LIGHTS_COUNT_OFFSET = POINT_LIGHTS_COUNT_OFFSET + INT32_SIZEOF;

    private static final int BOUNDS_BUFFER_MIN_SIZE = AABB.SIZEOF;
    private static final int MATRICES_BUFFER_MIN_SIZE = 16 * FLOAT32_SIZEOF;
    private static final int MATERIALS_BUFFER_MIN_SIZE = Material.SIZEOF;
    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;

    private static final int MEMORY_BARRIER_FLAGS =
            GL_COMMAND_BARRIER_BIT
                    | GL_SHADER_STORAGE_BARRIER_BIT
                    | GL_BUFFER_UPDATE_BARRIER_BIT
                    | GL_UNIFORM_BARRIER_BIT
                    | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT
                    | GL_ELEMENT_ARRAY_BARRIER_BIT;

    private static final int VERTEX_BUFFER_BINDING = 0;
    private static final int INSTANCE_BUFFER_BINDING = 1;


    private GLShaderProgram cullingShader;
    private GLShaderProgram renderShader;

    private GLUniformBuffer frustumUniformBuffer;
    private GLUniformBuffer cameraUniformBuffer;
    private GLUniformBuffer lightsUniformBuffer;

    private GLVertexArray vertexArray;
    private GLVertexBuffer vertexBuffer;
    private GLIndexBuffer indexBuffer;
    private GLVertexBuffer instanceBuffer; // model matrix + material indices

    private GLStorageBuffer boundingSpheresBuffer;
    private GLStorageBuffer meshIDsBuffer;
    private GLStorageBuffer matricesBuffer;

    private GLStorageBuffer meshCommandBuffer;
    private GLStorageBuffer instanceCommandBuffer;

    public void init() {

        initVertexArray();

        cullingShader = new GLShaderProgram()
                .attach(new GLShader(COMPUTE_STAGE).source(BerylFiles.getPath("shaders/compute/culling.comp")))
                .link();

        renderShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.frag")))
                .link();

        frustumUniformBuffer = new GLUniformBuffer();
        frustumUniformBuffer.allocate(FRUSTUM_UNIFORM_BUFFER_SIZE);
        frustumUniformBuffer.set(FRUSTUM_UNIFORM_BUFFER_NAME, cullingShader, 5);

        cameraUniformBuffer = new GLUniformBuffer();
        cameraUniformBuffer.allocate(CAMERA_UNIFORM_BUFFER_SIZE);
        cameraUniformBuffer.set(CAMERA_UNIFORM_BUFFER_NAME, renderShader, 0);

        lightsUniformBuffer = new GLUniformBuffer();
        lightsUniformBuffer.allocate(LIGHTS_UNIFORM_BUFFER_SIZE);
        lightsUniformBuffer.set(LIGHTS_UNIFORM_BUFFER_NAME, renderShader, 1);

        boundingSpheresBuffer = new GLStorageBuffer();
        meshIDsBuffer = new GLStorageBuffer();
        matricesBuffer = new GLStorageBuffer();

        meshCommandBuffer = new GLStorageBuffer();
        instanceCommandBuffer = new GLStorageBuffer();
    }

    @Override
    protected void terminate() {

        cullingShader.release();
        renderShader.release();

        frustumUniformBuffer.release();
        cameraUniformBuffer.release();
        lightsUniformBuffer.release();

        vertexBuffer.release();
        indexBuffer.release();
        instanceBuffer.release();
        vertexArray.release();
        boundingSpheresBuffer.release();
        meshIDsBuffer.release();
        matricesBuffer.release();

        meshCommandBuffer.release();
        instanceCommandBuffer.release();
    }

    public void prepare(Camera camera, Scene scene) {
        SceneMeshInfo meshInfo = scene.meshInfo();
        prepareBuffers(meshInfo);
        setLightsUniformBuffer(scene.environment());
        setCameraUniformBuffer(camera);
        setFrustumUniformBuffer(camera);
        performCullingPass(meshInfo.numMeshViewsInstances());
    }

    @Override
    public void render(Camera camera, Scene scene) {
        render(camera, scene.meshInfo());
    }

    private void render(Camera camera, SceneMeshInfo meshInfo) {

        final Color color = camera.clearColor();

        glEnable(GL_DEPTH_TEST);
        // glEnable(GL_CULL_FACE);
        glClearColor(color.red(), color.green(), color.blue(), color.alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderShader.bind();

        cameraUniformBuffer.bind(renderShader);

        lightsUniformBuffer.bind(renderShader);

        matricesBuffer.bind(2);

        GLStorageBuffer materialsBuffer = MaterialManager.get().buffer();

        materialsBuffer.bind(3);

        instanceCommandBuffer.bindIndirect();

        vertexArray.bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, meshInfo.numMeshViewsInstances(), 0);
    }

    private void performCullingPass(int numObjects) {

        cullingShader.bind();

        meshCommandBuffer.bind(0);
        instanceCommandBuffer.bind(1);
        boundingSpheresBuffer.bind(2);
        matricesBuffer.bind(3);
        meshIDsBuffer.bind(4);
        frustumUniformBuffer.bind(cullingShader);

        glDispatchCompute(numObjects, 1, 1);

        glMemoryBarrier(GL_COMMAND_BARRIER_BIT | GL_SHADER_STORAGE_BUFFER);
    }

    private void prepareBuffers(SceneMeshInfo meshInfo) {
        prepareMeshBuffers(meshInfo.meshes());
        prepareMatricesBuffer(meshInfo);
        prepareInstanceBuffer(meshInfo);
    }

    private void prepareInstanceBuffer(SceneMeshInfo meshInfo) {

        final int numInstances = meshInfo.numMeshViewsInstances();

        final int instanceCommandsMinSize = numInstances * GLDrawElementsCommand.SIZEOF;

        if (instanceCommandBuffer.size() < instanceCommandsMinSize) {
            reallocateBuffer(instanceCommandBuffer, instanceCommandsMinSize);
        }

        nmemset(instanceCommandBuffer.mappedMemory(), 0, instanceCommandsMinSize); // Clear with zeros

        final int instancesMinSize = numInstances * INSTANCE_BUFFER_MIN_SIZE;

        if (instanceBuffer.size() < instancesMinSize) {
            reallocateBuffer(instanceBuffer, instancesMinSize);
            vertexArray.setVertexBuffer(INSTANCE_BUFFER_BINDING, instanceBuffer, INSTANCE_BUFFER_MIN_SIZE);
        }

        final long meshIDsMinSize = numInstances * UINT32_SIZEOF;

        if (meshIDsBuffer.size() < meshIDsMinSize) {
            reallocateBuffer(meshIDsBuffer, meshIDsMinSize);
        }

        final List<MeshInstance> instances = meshInfo.instances();
        int objectIndex = 0;

        for(int instanceID = 0;instanceID < instances.size();instanceID++) {

            MeshInstance instance = instances.get(instanceID);

            for(MeshView meshView : instance) {

                final int meshID = meshInfo.meshes().indexOf(meshView.mesh());

                setInstanceMeshID(objectIndex, meshID);

                final int materialIndex = meshView.material().bufferIndex();

                setInstanceData(objectIndex, instanceID, materialIndex);

                ++objectIndex;
            }

        }
    }

    private void setInstanceMeshID(int instanceID, int meshID) {
        try (MemoryStack stack = stackPush()) {
            nmemcpy(meshIDsBuffer.mappedMemory() + instanceID * UINT32_SIZEOF, memAddress0(stack.ints(meshID)), UINT32_SIZEOF);
        }
    }

    private void setInstanceData(int instanceID, int matrixIndex, int materialIndex) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(INSTANCE_BUFFER_MIN_SIZE);

            buffer.putInt(matrixIndex).putInt(materialIndex);

            nmemcpy(instanceBuffer.mappedMemory() + instanceID * INSTANCE_BUFFER_MIN_SIZE, memAddress0(buffer), INSTANCE_BUFFER_MIN_SIZE);
        }
    }

    private void setInstanceMatrix(int index, Matrix4fc modelMatrix) {

        try (MemoryStack stack = stackPush()) {

            final ByteBuffer buffer = modelMatrix.get(stack.malloc(MATRICES_BUFFER_MIN_SIZE));

            nmemcpy(matricesBuffer.mappedMemory() + index * MATRICES_BUFFER_MIN_SIZE, memAddress0(buffer), MATRICES_BUFFER_MIN_SIZE);
        }
    }

    private void prepareMeshBuffers(List<Mesh> meshes) {

        checkMeshesBuffersSize(meshes);

        final long vertexBufferMemory = vertexBuffer.mappedMemory();
        final long indexBufferMemory = indexBuffer.mappedMemory();
        final long boundingSpheresBufferMemory = boundingSpheresBuffer.mappedMemory();
        final long meshCommandBufferMemory = meshCommandBuffer.mappedMemory();

        long vertexBufferOffset = 0;
        long indexBufferOffset = 0;
        long boundingSpheresOffset = 0;
        long meshCommandBufferOffset = 0;

        int firstIndex = 0;
        int baseVertex = 0;

        try (MemoryStack stack = stackPush()) {

            final ByteBuffer boundingSphereBuffer = stack.calloc(ISphere.SIZEOF);
            final long boundingSphereAddress = memAddress0(boundingSphereBuffer);

            GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

            for (Mesh mesh : meshes) {

                final ByteBuffer vertexData = mesh.vertexData();
                final ByteBuffer indexData = mesh.indexData();
                final int verticesSize = vertexData.remaining();
                final int indicesSize = indexData.remaining();

                command.count(mesh.indexCount())
                        .firstIndex(firstIndex)
                        .baseVertex(baseVertex);

                mesh.boundingSphere().get(0, boundingSphereBuffer);

                nmemcpy(vertexBufferMemory + vertexBufferOffset, memAddress0(vertexData), verticesSize);
                nmemcpy(indexBufferMemory + indexBufferOffset, memAddress0(indexData), indicesSize);
                nmemcpy(boundingSpheresBufferMemory + boundingSpheresOffset, boundingSphereAddress, ISphere.SIZEOF);
                nmemcpy(meshCommandBufferMemory + meshCommandBufferOffset, command.address(), GLDrawElementsCommand.SIZEOF);

                vertexBufferOffset += verticesSize;
                indexBufferOffset += indicesSize;
                boundingSpheresOffset += ISphere.SIZEOF;
                meshCommandBufferOffset += GLDrawElementsCommand.SIZEOF;
                firstIndex += mesh.indexCount();
                baseVertex += mesh.vertexCount();
            }
        }
    }

    private void checkMeshesBuffersSize(List<Mesh> meshes) {

        final long boundsMinSize = meshes.size() * BOUNDS_BUFFER_MIN_SIZE;

        if (boundingSpheresBuffer.size() < boundsMinSize) {
            reallocateBuffer(boundingSpheresBuffer, boundsMinSize);
        }

        final long verticesMinSize = meshes.stream().mapToLong(mesh -> mesh.vertexData().remaining()).sum();

        if (vertexBuffer.size() < verticesMinSize) {
            reallocateBuffer(vertexBuffer, verticesMinSize);
            vertexArray.setVertexBuffer(VERTEX_BUFFER_BINDING, vertexBuffer, StaticMesh.VERTEX_DATA_SIZE);
        }

        final long indicesMinSize = meshes.stream().mapToLong(mesh -> mesh.indexData().remaining()).sum();

        if (indexBuffer.size() < indicesMinSize) {
            reallocateBuffer(indexBuffer, indicesMinSize);
            vertexArray.setIndexBuffer(indexBuffer);
        }

        final long commandsMinSize = meshes.size() * GLDrawElementsCommand.SIZEOF;

        if (meshCommandBuffer.size() < commandsMinSize) {
            reallocateBuffer(meshCommandBuffer, commandsMinSize);
        }
    }

    private void prepareMatricesBuffer(SceneMeshInfo meshInfo) {

        final int minSize = meshInfo.instances().size() * MATRICES_BUFFER_MIN_SIZE;

        if (matricesBuffer.size() < minSize) {
            reallocateBuffer(matricesBuffer, minSize);
        }

        final long matricesBufferMemory = matricesBuffer.mappedMemory();

        range(0, meshInfo.instances().size()).parallel().forEach(index -> {

            try(MemoryStack stack = stackPush()) {

                final ByteBuffer buffer = meshInfo.instances().get(index).modelMatrix().get(stack.malloc(MATRICES_BUFFER_MIN_SIZE));

                nmemcpy(matricesBufferMemory + index * MATRICES_BUFFER_MIN_SIZE, memAddress0(buffer), MATRICES_BUFFER_MIN_SIZE);
            }
        });
    }

    private void setCameraUniformBuffer(Camera camera) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(CAMERA_UNIFORM_BUFFER_SIZE);

            camera.projectionViewMatrix().get(CAMERA_UNIFORM_BUFFER_PROJECTION_VIEW_OFFSET, buffer);
            camera.transform().position().get(CAMERA_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET, buffer);

            nmemcpy(cameraUniformBuffer.mappedMemory(), memAddress0(buffer), CAMERA_UNIFORM_BUFFER_SIZE);
        }
    }

    private void setFrustumUniformBuffer(Camera camera) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(FRUSTUM_UNIFORM_BUFFER_SIZE);

            camera.projectionViewMatrix().get(FRUSTUM_UNIFORM_BUFFER_PROJECTION_VIEW_OFFSET, buffer);

            for (int i = 0; i < 6; i++) {
                camera.frustumPlanes()[i].get(FRUSTUM_UNIFORM_BUFFER_PLANES_OFFSET + i * 4 * FLOAT32_SIZEOF, buffer);
            }

            nmemcpy(frustumUniformBuffer.mappedMemory(), memAddress0(buffer), FRUSTUM_UNIFORM_BUFFER_SIZE);
        }
    }

    private void setLightsUniformBuffer(SceneEnvironment environment) {

        final long lightsUniformBufferMemory = lightsUniformBuffer.mappedMemory();

        final DirectionalLight directionalLight = environment.directionalLight();
        final int pointLightsCount = environment.pointLightsCount();
        final int spotLightsCount = environment.spotLightsCount();

        try (MemoryStack stack = stackPush()) {

            ByteBuffer directionalLightBuffer = stack.calloc(Light.SIZEOF);

            if (directionalLight != null) {
                directionalLight.get(0, directionalLightBuffer);
            }

            nmemcpy(lightsUniformBufferMemory + DIRECTIONAL_LIGHT_OFFSET, memAddress(directionalLightBuffer), Light.SIZEOF);

            if (pointLightsCount > 0) {

                ByteBuffer buffer = stack.malloc(pointLightsCount * Light.SIZEOF);

                for (int i = 0; i < pointLightsCount; i++) {
                    environment.pointLight(i).get(i * Light.SIZEOF, buffer);
                }

                nmemcpy(lightsUniformBufferMemory + POINT_LIGHTS_OFFSET, memAddress(buffer), buffer.limit());
            }

            if (spotLightsCount > 0) {

                ByteBuffer buffer = stack.malloc(spotLightsCount * Light.SIZEOF);

                for (int i = 0; i < spotLightsCount; i++) {
                    environment.spotLight(i).get(i * Light.SIZEOF, buffer);
                }

                nmemcpy(lightsUniformBufferMemory + SPOT_LIGHTS_OFFSET, memAddress(buffer), buffer.limit());
            }

            ByteBuffer buffer = stack.calloc(Color.SIZEOF + INT32_SIZEOF * 2);

            environment.ambientColor().getRGBA(buffer).putInt(pointLightsCount).putInt(spotLightsCount);

            nmemcpy(lightsUniformBufferMemory + AMBIENT_COLOR_OFFSET, memAddress0(buffer), buffer.capacity());
        }
    }

    private void initVertexArray() {

        vertexArray = new GLVertexArray();

        vertexBuffer = new GLVertexBuffer();

        VertexLayout vertexLayout = new VertexLayout.Builder(2)
                .put(0, 0, POSITION3D, NORMAL, TEXCOORDS2D)
                .put(1, 3, INDEX, INDEX).instanced(1, true)
                .build();

        for (int i = 0; i < vertexLayout.bindings(); i++) {
            vertexArray.setVertexAttributes(i, vertexLayout.attributeList(i));
        }

        indexBuffer = new GLIndexBuffer();

        instanceBuffer = new GLVertexBuffer();
    }

    private void reallocateBuffer(GraphicsMappableBuffer buffer, long size) {
        buffer.unmapMemory();
        buffer.allocate(size);
    }

}
