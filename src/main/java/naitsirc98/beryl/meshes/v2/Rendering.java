package naitsirc98.beryl.meshes.v2;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.ShaderStage;
import naitsirc98.beryl.graphics.buffers.GraphicsMappableBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLIndexBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLStorageBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLVertexBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.input.StateTable;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.vertices.VertexAttribute;
import naitsirc98.beryl.meshes.vertices.VertexAttributeList;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.SceneMeshInfo;
import naitsirc98.beryl.util.geometry.Bounds;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.graphics.ShaderStage.*;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.NORMAL;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.libc.LibCString.nmemcpy;

public class Rendering extends RenderingPath {

    private static final int BOUNDS_BUFFER_MIN_SIZE = Bounds.SIZEOF;
    private static final int MATRICES_BUFFER_MIN_SIZE = 16 * FLOAT32_SIZEOF;
    private static final int MATERIALS_BUFFER_MIN_SIZE = PhongMaterial.SIZEOF + 4 * UINT64_SIZEOF;
    private static final int INSTANCE_BUFFER_MIN_SIZE = UINT32_SIZEOF * 2;

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
    private GLVertexArray vertexArray;
    private GLVertexBuffer vertexBuffer;
    private GLIndexBuffer indexBuffer;
    private GLVertexBuffer instanceBuffer; // model matrix + material indices
    private GLStorageBuffer boundsBuffer;
    private GLStorageBuffer meshIDsBuffer;
    private GLStorageBuffer matricesBuffer;
    private GLStorageBuffer materialsBuffer;
    private GLStorageBuffer commandBuffer;

    // Persistent mapped buffers
    private long vertexBufferMemory;
    private long indexBufferMemory;
    private long instanceBufferMemory;
    private long boundsBufferMemory;
    private long meshIDsBufferMemory;
    private long commandBufferMemory;
    private long matricesBufferMemory;
    private long materialsBufferMemory;

    public void init() {

        cullingShader = new GLShaderProgram()
                .attach(new GLShader(COMPUTE_STAGE).source(BerylFiles.getPath("shaders/compute/culling.comp")))
                .link();

        renderShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.frag")))
                .link();

        initVertexArray();

        boundsBuffer = new GLStorageBuffer();
        meshIDsBuffer = new GLStorageBuffer();
        commandBuffer = new GLStorageBuffer();
        matricesBuffer = new GLStorageBuffer();
        materialsBuffer = new GLStorageBuffer();

        vertexBufferMemory = vertexBuffer.mapMemoryPtr(0);
        indexBufferMemory = indexBuffer.mapMemoryPtr(0);
        boundsBufferMemory = boundsBuffer.mapMemoryPtr(0);
        meshIDsBufferMemory = meshIDsBuffer.mapMemoryPtr(0);
        commandBufferMemory = commandBuffer.mapMemoryPtr(0);
        matricesBufferMemory = matricesBuffer.mapMemoryPtr(0);
        materialsBufferMemory = materialsBuffer.mapMemoryPtr(0);
        instanceBufferMemory = instanceBuffer.mapMemoryPtr(0);
    }

    @Override
    protected void terminate() {

    }

    @Override
    public void render(Camera camera, Scene scene) {
        SceneMeshInfo meshInfo = scene.meshInfo();
        prepareBuffers(scene.meshInfo(), meshInstances, meshViews, materials);
        render(camera, meshInfo.meshViews());
    }

    private void render(Camera camera, List<MeshView> meshViews) {

        final int numInstances = meshViews.size();

        performCullingPass(numInstances);

        render();
    }

    private void render() {

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        renderShader.bind();

        // TODO: Camera uniform buffer bind to 0

        // TODO: Lights uniform buffer bind to 1

        matricesBuffer.bind(2);

        materialsBuffer.bind(3);

        commandBuffer.bindIndirect();

        vertexArray.bind();

        glMemoryBarrier(MEMORY_BARRIER_FLAGS);

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, 1, 0);
    }

    private void performCullingPass(int numInstances) {

        commandBuffer.bind(0);
        boundsBuffer.bind(1);
        matricesBuffer.bind(2);
        meshIDsBuffer.bind(3);

        cullingShader.bind();

        glDispatchCompute(numInstances, 1, 1);

        // glMultiDrawElementsIndirect(mode, element_type, *indirect, num_cmds, cmd_stride)
        // glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, 1, 0);
    }

    private void prepareBuffers(List<Mesh> meshes, Map<Mesh, List<MeshView>> meshInstances, List<MeshView> meshViews, List<Material> materials) {
        prepareMeshBuffers(meshes, meshInstances);
        prepareMatricesBuffer(meshViews);
        prepareMaterialsBuffer(materials);
        prepareInstanceBuffer(meshes, meshInstances, meshViews.size(), materials);
    }

    private void prepareInstanceBuffer(List<Mesh> meshes, Map<Mesh, List<MeshView>> meshInstances, int numInstances, List<Material> materials) {

        final int minSize = numInstances * INSTANCE_BUFFER_MIN_SIZE;

        if(instanceBuffer.size() < minSize) {
            instanceBufferMemory = reallocateBuffer(instanceBuffer, minSize);
            vertexArray.setVertexBuffer(INSTANCE_BUFFER_BINDING, instanceBuffer, INSTANCE_BUFFER_MIN_SIZE);
        }

        final AtomicInteger matricesBufferIndex = new AtomicInteger(0);

        for(int meshIndex = 0;meshIndex < meshes.size();meshIndex++) {

            final int meshID = meshIndex;

            final List<MeshView> meshViews = meshInstances.get(meshes.get(meshIndex));

            range(0, meshViews.size()).parallel().forEach(instanceID -> {

                final MeshView meshView = meshViews.get(instanceID);

                setInstanceMeshID(instanceID, meshID);

                final int matrixIndex = matricesBufferIndex.getAndIncrement();

                setInstanceMatrix(matrixIndex, meshView.modelMatrix());

                final int materialIndex = materials.indexOf(meshView.material());

                setInstanceData(instanceID, matrixIndex, materialIndex);
            });
        }
    }

    private void setInstanceMeshID(int instanceID, int meshID) {
        try(MemoryStack stack = stackPush()) {
            nmemcpy(meshIDsBufferMemory + instanceID * UINT32_SIZEOF, memAddress0(stack.ints(meshID)), UINT32_SIZEOF);
        }
    }

    private void setInstanceData(int instanceID, int matrixIndex, int materialIndex) {

        try(MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.malloc(INSTANCE_BUFFER_MIN_SIZE);

            buffer.putInt(0, matrixIndex).putInt(1, materialIndex);

            nmemcpy(instanceBufferMemory + instanceID * INSTANCE_BUFFER_MIN_SIZE, memAddress0(buffer), INSTANCE_BUFFER_MIN_SIZE);
        }
    }

    private void setInstanceMatrix(int index, Matrix4fc modelMatrix) {

        try(MemoryStack stack = stackPush()) {

            final ByteBuffer buffer = modelMatrix.get(stack.malloc(MATRICES_BUFFER_MIN_SIZE));

            nmemcpy(matricesBufferMemory + index * MATRICES_BUFFER_MIN_SIZE, memAddress0(buffer), MATRICES_BUFFER_MIN_SIZE);
        }
    }

    private void prepareMeshBuffers(List<Mesh> meshes, Map<Mesh, List<MeshView>> meshInstances) {

        checkMeshesBuffersSize(meshes);

        final long vertexBufferMemory = this.vertexBufferMemory;
        final long indexBufferMemory = this.indexBufferMemory;
        final long boundsBufferMemory = this.boundsBufferMemory;
        final long commandBufferMemory = this.commandBufferMemory;

        long vertexBufferOffset = 0;
        long indexBufferOffset = 0;
        long boundsBufferOffset = 0;
        long commandBufferOffset = 0;

        int baseInstance = 0;

        try(MemoryStack stack = stackPush()) {

            final ByteBuffer boundsBuffer = stack.malloc(Bounds.SIZEOF);
            final long boundsBufferAddress = memAddress0(boundsBuffer);

            GLDrawElementsCommand command = new GLDrawElementsCommand(memAddress(stack.calloc(GLDrawElementsCommand.SIZEOF)));

            for(Mesh mesh : meshes) {

                final ByteBuffer vertexData = mesh.vertexData();
                final ByteBuffer indexData = mesh.indexData();
                final int verticesSize = vertexData.remaining();
                final int indicesSize = indexData.remaining();

                command.count(mesh.indexCount())
                        .primCount(0) // Set by compute shader
                        .firstIndex((int) (indexBufferOffset / UINT32_SIZEOF))
                        .baseVertex((int) (vertexBufferOffset / StaticMesh.VERTEX_DATA_SIZE))
                        .baseInstance(baseInstance);

                mesh.bounds().get(0, boundsBuffer);

                nmemcpy(vertexBufferMemory + vertexBufferOffset, memAddress0(vertexData), verticesSize);
                nmemcpy(indexBufferMemory + indexBufferOffset, memAddress0(indexData), indicesSize);
                nmemcpy(boundsBufferMemory + boundsBufferOffset, boundsBufferAddress, Bounds.SIZEOF);
                nmemcpy(commandBufferMemory + commandBufferOffset, command.address(), command.sizeof());

                vertexBufferOffset += verticesSize;
                indexBufferOffset += indicesSize;
                boundsBufferOffset += Bounds.SIZEOF;
                commandBufferOffset += command.sizeof();
                baseInstance += meshInstances.get(mesh).size();
            }
        }
    }

    private void checkMeshesBuffersSize(List<Mesh> meshes) {

        final long boundsMinSize = meshes.size() * BOUNDS_BUFFER_MIN_SIZE;

        if(boundsBuffer.size() < boundsMinSize) {
            boundsBufferMemory = reallocateBuffer(boundsBuffer, boundsMinSize);
        }

        final long verticesMinSize = meshes.parallelStream().mapToLong(mesh -> mesh.vertexData().remaining()).sum();

        if(vertexBuffer.size() < verticesMinSize) {
            vertexBufferMemory = reallocateBuffer(vertexBuffer, verticesMinSize);
            vertexArray.setVertexBuffer(VERTEX_BUFFER_BINDING, vertexBuffer, StaticMesh.VERTEX_DATA_SIZE);
        }

        final long indicesMinSize = meshes.parallelStream().mapToLong(mesh -> mesh.indexData().remaining()).sum();

        if(indexBuffer.size() < indicesMinSize) {
            indexBufferMemory = reallocateBuffer(indexBuffer, indicesMinSize);
            vertexArray.setIndexBuffer(indexBuffer);
        }

        final long commandsMinSize = meshes.size() * GLDrawElementsCommand.SIZEOF;

        if(commandBuffer.size() < commandsMinSize) {
            commandBufferMemory = reallocateBuffer(commandBuffer, commandsMinSize);
        }
    }

    private void prepareMatricesBuffer(List<MeshView> meshViews) {

        final int minSize = meshViews.size() * MATRICES_BUFFER_MIN_SIZE;

        if(matricesBuffer.size() < minSize) {
            matricesBufferMemory = reallocateBuffer(matricesBuffer, minSize);
        }

        range(0, meshViews.size()).parallel().forEach(index -> {

            try(MemoryStack stack = stackPush()) {

                final ByteBuffer buffer = meshViews.get(index).modelMatrix().get(stack.malloc(MATRICES_BUFFER_MIN_SIZE));

                nmemcpy(matricesBufferMemory + index * MATRICES_BUFFER_MIN_SIZE, memAddress0(buffer), MATRICES_BUFFER_MIN_SIZE);
            }
        });
    }

    private void prepareMaterialsBuffer(List<Material> materials) {

        final int minSize = materials.size() * MATERIALS_BUFFER_MIN_SIZE;

        if(materialsBuffer.size() < minSize) {
            materialsBufferMemory = reallocateBuffer(materialsBuffer, minSize);
        }

        range(0, materials.size()).parallel().forEach(index -> {

            try(MemoryStack stack = stackPush()) {

                Material material = materials.get(index);

                ByteBuffer buffer = stack.calloc(MATERIALS_BUFFER_MIN_SIZE);

                material.get(0, buffer);

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

                nmemcpy(materialsBufferMemory + index * MATERIALS_BUFFER_MIN_SIZE, memAddress0(buffer), buffer.capacity());
            }
        });

    }

    private void initVertexArray() {

        vertexBuffer = new GLVertexBuffer();

        indexBuffer = new GLIndexBuffer();

        instanceBuffer = new GLVertexBuffer();

        vertexArray = new GLVertexArray();

        VertexLayout vertexLayout = new VertexLayout.Builder(2)
                .put(0, 0, POSITION3D, NORMAL, TEXCOORDS2D)
                .put(1, 3, INDEX, INDEX).instanced(1, true)
                .build();

        for(int i = 0;i < vertexLayout.bindings();i++) {
            vertexArray.setVertexAttributes(i, vertexLayout.attributeList(i));
        }
    }

    private long reallocateBuffer(GraphicsMappableBuffer buffer, long size) {
        buffer.unmapMemory();
        buffer.allocate(size);
        return buffer.mapMemory(0).get(0);
    }

}
