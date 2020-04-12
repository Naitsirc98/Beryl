package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.buffers.MappedGraphicsBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.meshes.*;
import naitsirc98.beryl.meshes.models.StaticMeshLoader;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.scenes.Fog;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnvironment;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.SceneMeshInfo;
import naitsirc98.beryl.util.Color;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.*;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static naitsirc98.beryl.util.Asserts.assertEquals;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Rendering extends RenderingPath {

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

    private GLBuffer frustumUniformBuffer;
    private GLBuffer cameraUniformBuffer;
    private GLBuffer lightsUniformBuffer;

    private GLVertexArray vertexArray;
    private GLBuffer instanceBuffer; // model matrix + material + bounding sphere indices

    private GLBuffer transformsBuffer;
    private GLBuffer meshIndicesBuffer;

    private GLBuffer instanceCommandBuffer;

    private GLBuffer debugBuffer;

    private GLShaderProgram debugShader;
    private GLVertexArray debugVAO;
    private Mesh debugMesh;

    public void init() {

        initVertexArray();

        cullingShader = new GLShaderProgram()
                .attach(new GLShader(COMPUTE_STAGE).source(BerylFiles.getPath("shaders/compute/culling.comp")))
                .link();

        renderShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.frag")))
                .link();

        debugShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/gl/simple/simple.gl.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/gl/simple/simple.gl.frag")))
                .link();

        debugBuffer = new GLBuffer();
        debugBuffer.allocate(1000 * VECTOR4_SIZEOF);

        debugVAO = new GLVertexArray();

        debugMesh = new StaticMeshLoader().load(BerylFiles.getPath("models/cube.obj")).loadedMesh(0).mesh();

        GLBuffer vbo = new GLBuffer();
        vbo.data(debugMesh.vertexData());

        GLBuffer ibo = new GLBuffer();
        ibo.data(debugMesh.indexData());

        debugVAO.addVertexBuffer(0, VertexLayout.VERTEX_LAYOUT_3D.attributeList(0), vbo);
        debugVAO.setIndexBuffer(ibo);

        frustumUniformBuffer = new GLBuffer("FRUSTUM_UNIFORM_BUFFER");
        frustumUniformBuffer.allocate(FRUSTUM_UNIFORM_BUFFER_SIZE);

        cameraUniformBuffer = new GLBuffer("CAMERA_UNIFORM_BUFFER");
        cameraUniformBuffer.allocate(CAMERA_UNIFORM_BUFFER_SIZE);

        lightsUniformBuffer = new GLBuffer("LIGHTS_UNIFORM_BUFFER");
        lightsUniformBuffer.allocate(LIGHTS_UNIFORM_BUFFER_SIZE);

        transformsBuffer = new GLBuffer("TRANSFORMS_STORAGE_BUFFER");
        meshIndicesBuffer = new GLBuffer("MESH_INDICES_STORAGE_BUFFER");

        instanceCommandBuffer = new GLBuffer("INSTANCE_COMMAND_BUFFER");

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

    public void prepare(Camera camera, Scene scene) {
        SceneMeshInfo meshInfo = scene.meshInfo();
        prepareBuffers(meshInfo);
        setLightsUniformBuffer(scene.environment());
        setCameraUniformBuffer(camera);
        setFrustumUniformBuffer(camera);
    }

    @Override
    public void render(Camera camera, Scene scene) {
        performCullingPass(scene.meshInfo().numInstancedMeshViews());
        render(camera, scene.meshInfo());
    }

    private void render(Camera camera, SceneMeshInfo meshInfo) {

        final Color color = camera.clearColor();

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        // glEnable(GL_CULL_FACE);
        glClearColor(color.red(), color.green(), color.blue(), color.alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderShader.bind();

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);

        GLBuffer materialsBuffer = MaterialManager.get().buffer();

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        instanceCommandBuffer.bind(GL_DRAW_INDIRECT_BUFFER);

        vertexArray.bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, meshInfo.numInstancedMeshViews(), 0);

        glFinish();

        ByteBuffer commands = instanceCommandBuffer.get(0);

        for(int i = 0;i < meshInfo.numInstancedMeshViews();i++) {
            System.out.println(i + " = " + new GLDrawElementsCommand(commands));
            commands.position(i * GLDrawElementsCommand.SIZEOF);
        }

        /*

        glFinish();

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);

        debugShader.bind();

        debugVAO.bind();

        FloatBuffer mvpBuffer = BufferUtils.createFloatBuffer(16);

        Matrix4f mvp = new Matrix4f();

        Color[] colors = {
                Color.RED,
                Color.GREEN,
                Color.BLUE
        };

        FloatBuffer frustumData = frustumUniformBuffer.get(0).asFloatBuffer();

        assertEquals(new Matrix4f(frustumData), camera.projectionViewMatrix());

        for(int i = 0;i < 6;i++) {
            assertEquals(new Vector4f(16 + i * 4, frustumData), camera.frustumPlanes()[i]);
        }

        ByteBuffer debug = debugBuffer.get(0);

        int total = 0;

        for (int i = 0; i < meshInfo.instances().size(); i++) {

            MeshInstance instance = meshInfo.instances().get(i);

            Matrix4f modelMatrix = new Matrix4f(instance.modelMatrix());
            Matrix4fc projectionView = camera.projectionViewMatrix();

            Vector3f scale = modelMatrix.getScale(new Vector3f());

            int j = 0;

            for (MeshView meshView : instance) {

                Mesh mesh = meshView.mesh();

                Vector4f center = new Vector4f(mesh.boundingSphere().center(), 1.0f);

                center.mul(modelMatrix);

                System.out.println("invocation cpu = " + new Vector4f(total, total, total, meshView.mesh().index()));
                System.out.println("invocation gpu = " + new Vector4f(debug.getFloat(), debug.getFloat(), debug.getFloat(), debug.getFloat()));

                mvp.translation(center.x, center.y, center.z).scale(mesh.boundingSphere().radius() * scale.x);

                mvp = camera.projectionViewMatrix().mul(mvp, mvp);

                mvp.get(mvpBuffer);

                debugShader.uniformMatrix4f("u_MVP", false, mvpBuffer);
                debugShader.uniformColor("color", colors[j % 3]);

                ++j;
                ++total;

                if (!insideFrustum(camera, center.x, center.y, center.z, mesh.boundingSphere().radius() * scale.x)) {
                    continue;
                }

                glDrawElements(GL_TRIANGLES, debugMesh.indexCount(), GL_UNSIGNED_INT, 0);
            }
        }

         */

    }

    public boolean insideFrustum(Camera camera, float x0, float y0, float z0, float boundingRadius) {
        Vector4fc[] planes = camera.frustumPlanes();
        for (int i = 0; i < planes.length; i++) {
            Vector4fc plane = planes[i];
            if (plane.x() * x0 + plane.y() * y0 + plane.z() * z0 + plane.w() <= -boundingRadius) {
                return false;
            }
        }
        return true;
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
        debugBuffer.bind(GL_SHADER_STORAGE_BUFFER, 6);

        glDispatchCompute(numObjects, 1, 1);

        glMemoryBarrier(GL_COMMAND_BARRIER_BIT | GL_SHADER_STORAGE_BUFFER);
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

        for (int instanceID = 0; instanceID < instances.size(); instanceID++) {

            MeshInstance instance = instances.get(instanceID);

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
            camera.transform().position().get(CAMERA_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET, buffer);

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
