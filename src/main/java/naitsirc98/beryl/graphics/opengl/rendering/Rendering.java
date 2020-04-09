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
import naitsirc98.beryl.materials.PhongMaterial;
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
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.graphics.ShaderStage.*;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static naitsirc98.beryl.util.Asserts.assertEquals;
import static naitsirc98.beryl.util.Asserts.assertTrue;
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
    private static final int MATERIALS_BUFFER_MIN_SIZE = PhongMaterial.SIZEOF + 4 * UINT64_SIZEOF;
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
    private GLStorageBuffer materialsBuffer;

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
        materialsBuffer = new GLStorageBuffer();

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
        materialsBuffer.release();

        meshCommandBuffer.release();
        instanceCommandBuffer.release();
    }

    public void prepare(Camera camera, Scene scene) {
        SceneMeshInfo meshInfo = scene.meshInfo();
        prepareBuffers(meshInfo.meshViews(), meshInfo.instancesTable(), meshInfo.instances(), meshInfo.materials());
        setLightsUniformBuffer(scene.environment());
        setCameraUniformBuffer(camera);
        setFrustumUniformBuffer(camera);
        performCullingPass(meshInfo.instances().size());
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

        materialsBuffer.bind(3);

        instanceCommandBuffer.bindIndirect();

        vertexArray.bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, meshInfo.instances().size(), 0);
    }

    private void performCullingPass(int numInstances) {

        cullingShader.bind();

        meshCommandBuffer.bind(0);
        instanceCommandBuffer.bind(1);
        boundingSpheresBuffer.bind(2);
        matricesBuffer.bind(3);
        meshIDsBuffer.bind(4);
        frustumUniformBuffer.bind(cullingShader);

        glDispatchCompute(numInstances, 1, 1);

        glMemoryBarrier(GL_COMMAND_BARRIER_BIT | GL_SHADER_STORAGE_BUFFER);
    }

    private void prepareBuffers(List<MeshView> meshViews, Map<MeshView, List<MeshInstance>> instancesTable,
                                List<MeshInstance> instances, List<Material> materials) {
        prepareMeshBuffers(meshViews, instancesTable);
        prepareMatricesBuffer(instances);
        prepareMaterialsBuffer(materials);
        prepareInstanceBuffer(meshViews, instancesTable, instances.size(), materials);
    }

    private void prepareInstanceBuffer(List<MeshView> meshViews, Map<MeshView, List<MeshInstance>> instancesTable,
                                       int numInstances, List<Material> materials) {

        final int instanceCommandsMinSize = numInstances * GLDrawElementsCommand.SIZEOF;

        if (instanceCommandBuffer.size() < instanceCommandsMinSize) {
            reallocateBuffer(instanceCommandBuffer, instanceCommandsMinSize);
        }

        nmemset(instanceCommandBuffer.mappedMemory(), 0, instanceCommandsMinSize); // Clear with zeros

        final int matricesMinSize = numInstances * MATRICES_BUFFER_MIN_SIZE;

        if (matricesBuffer.size() < matricesMinSize) {
            reallocateBuffer(matricesBuffer, matricesMinSize);
        }

        final int instancesMinSize = numInstances * INSTANCE_BUFFER_MIN_SIZE;

        if (instanceBuffer.size() < instancesMinSize) {
            reallocateBuffer(instanceBuffer, instancesMinSize);
            vertexArray.setVertexBuffer(INSTANCE_BUFFER_BINDING, instanceBuffer, INSTANCE_BUFFER_MIN_SIZE);
        }

        final long meshIDsMinSize = meshViews.parallelStream().map(MeshView::mesh).distinct().count() * UINT32_SIZEOF;

        if (meshIDsBuffer.size() < meshIDsMinSize) {
            reallocateBuffer(meshIDsBuffer, meshIDsMinSize);
        }

        final AtomicInteger nextInstanceID = new AtomicInteger();

        range(0, meshViews.size()).forEach(meshIndex -> {

            final int meshID = meshIndex;

            final MeshView meshView = meshViews.get(meshIndex);

            final List<MeshInstance> instances = instancesTable.get(meshView);

            for (MeshInstance instance : instances) {

                final int instanceID = nextInstanceID.getAndIncrement();

                setInstanceMeshID(instanceID, meshID);

                setInstanceMatrix(instanceID, instance.modelMatrix());

                final int materialIndex = materials.indexOf(meshView.material());

                setInstanceData(instanceID, instanceID, materialIndex);
            }
        });
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

    private void prepareMeshBuffers(List<MeshView> meshViews, Map<MeshView, List<MeshInstance>> instancesTable) {

        checkMeshesBuffersSize(meshViews);

        final long vertexBufferMemory = vertexBuffer.mappedMemory();
        final long indexBufferMemory = indexBuffer.mappedMemory();
        final long boundingSpheresBufferMemory = boundingSpheresBuffer.mappedMemory();
        final long meshCommandBufferMemory = meshCommandBuffer.mappedMemory();

        long vertexBufferOffset = 0;
        long indexBufferOffset = 0;
        long boundingSpheresOffset = 0;
        long meshCommandBufferOffset = 0;

        try (MemoryStack stack = stackPush()) {

            final ByteBuffer boundingSphereBuffer = stack.calloc(ISphere.SIZEOF);
            final long boundingSphereAddress = memAddress0(boundingSphereBuffer);

            GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

            ByteBuffer pMeshIDsOffset = stack.calloc(UINT32_SIZEOF);

            for (MeshView meshView : meshViews) {

                final Mesh mesh = meshView.mesh();

                final ByteBuffer vertexData = mesh.vertexData();
                final ByteBuffer indexData = mesh.indexData();
                final int verticesSize = vertexData.remaining();
                final int indicesSize = indexData.remaining();

                command.count(mesh.indexCount())
                        .firstIndex((int) (indexBufferOffset / UINT32_SIZEOF))
                        .baseVertex((int) (vertexBufferOffset / StaticMesh.VERTEX_DATA_SIZE));

                mesh.boundingSphere().get(0, boundingSphereBuffer);

                nmemcpy(vertexBufferMemory + vertexBufferOffset, memAddress0(vertexData), verticesSize);
                nmemcpy(indexBufferMemory + indexBufferOffset, memAddress0(indexData), indicesSize);
                nmemcpy(boundingSpheresBufferMemory + boundingSpheresOffset, boundingSphereAddress, ISphere.SIZEOF);
                nmemcpy(meshCommandBufferMemory + meshCommandBufferOffset, command.address(), GLDrawElementsCommand.SIZEOF);

                vertexBufferOffset += verticesSize;
                indexBufferOffset += indicesSize;
                boundingSpheresOffset += ISphere.SIZEOF;
                meshCommandBufferOffset += GLDrawElementsCommand.SIZEOF;
            }
        }
    }

    private void checkMeshesBuffersSize(List<MeshView> meshViews) {

        final long boundsMinSize = meshViews.size() * BOUNDS_BUFFER_MIN_SIZE;

        if (boundingSpheresBuffer.size() < boundsMinSize) {
            reallocateBuffer(boundingSpheresBuffer, boundsMinSize);
        }

        final long verticesMinSize = meshViews.stream().mapToLong(meshView -> meshView.mesh().vertexData().remaining()).sum();

        if (vertexBuffer.size() < verticesMinSize) {
            reallocateBuffer(vertexBuffer, verticesMinSize);
            vertexArray.setVertexBuffer(VERTEX_BUFFER_BINDING, vertexBuffer, StaticMesh.VERTEX_DATA_SIZE);
        }

        final long indicesMinSize = meshViews.stream().mapToLong(meshView -> meshView.mesh().indexData().remaining()).sum();

        if (indexBuffer.size() < indicesMinSize) {
            reallocateBuffer(indexBuffer, indicesMinSize);
            vertexArray.setIndexBuffer(indexBuffer);
        }

        final long commandsMinSize = meshViews.size() * GLDrawElementsCommand.SIZEOF;

        if (meshCommandBuffer.size() < commandsMinSize) {
            reallocateBuffer(meshCommandBuffer, commandsMinSize);
        }
    }

    private void prepareMatricesBuffer(List<MeshInstance> meshInstances) {

        final int minSize = meshInstances.size() * MATRICES_BUFFER_MIN_SIZE;

        if (matricesBuffer.size() < minSize) {
            reallocateBuffer(matricesBuffer, minSize);
        }

        final long matricesBufferMemory = matricesBuffer.mappedMemory();

        range(0, meshInstances.size()).parallel().forEach(index -> {

            try (MemoryStack stack = stackPush()) {

                final ByteBuffer buffer = meshInstances.get(index).modelMatrix().get(stack.malloc(MATRICES_BUFFER_MIN_SIZE));

                nmemcpy(matricesBufferMemory + index * MATRICES_BUFFER_MIN_SIZE, memAddress0(buffer), MATRICES_BUFFER_MIN_SIZE);
            }
        });
    }

    private void prepareMaterialsBuffer(List<Material> materials) {

        final int minSize = materials.size() * MATERIALS_BUFFER_MIN_SIZE;

        if (materialsBuffer.size() < minSize) {
            reallocateBuffer(materialsBuffer, minSize);
        }

        final long materialsBufferMemory = materialsBuffer.mappedMemory();

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(MATERIALS_BUFFER_MIN_SIZE);
            final long srcAddress = memAddress0(buffer);

            for (int i = 0; i < materials.size(); i++) {

                Material material = materials.get(i);

                material.get(0, buffer.rewind());

                PhongMaterial phongMaterial = (PhongMaterial) material;

                GLTexture2D ambientMap = phongMaterial.ambientMap();
                GLTexture2D diffuseMap = phongMaterial.diffuseMap();
                GLTexture2D specularMap = phongMaterial.specularMap();
                GLTexture2D emissiveMap = phongMaterial.emissiveMap();

                buffer.position(PhongMaterial.SIZEOF)
                        .putLong(ambientMap.makeResident())
                        .putLong(diffuseMap.makeResident())
                        .putLong(specularMap.makeResident())
                        .putLong(emissiveMap.makeResident());

                nmemcpy(materialsBufferMemory + i * MATERIALS_BUFFER_MIN_SIZE, srcAddress, MATERIALS_BUFFER_MIN_SIZE);
            }
        }
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

            ByteBuffer buffer = stack.malloc(Color.SIZEOF + INT32_SIZEOF * 2);

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
