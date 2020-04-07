package naitsirc98.beryl.graphics.opengl.rendering.phong;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.GLMapper;
import naitsirc98.beryl.graphics.opengl.buffers.GLUniformBuffer;
import naitsirc98.beryl.graphics.opengl.rendering.ShadowCascade;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexData;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnvironment;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.SceneMeshInfo;
import naitsirc98.beryl.util.Color;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.List;

import static naitsirc98.beryl.graphics.Graphics.opengl;
import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.libc.LibCString.nmemcpy;

public class GLPhongRenderingPath extends RenderingPath {

    // MVP, ModelMatrix, NormalMatrix, CameraPosition
    private static final int MATRICES_UNIFORM_BUFFER_SIZE = (16 + 16 + 16 + 4) * FLOAT32_SIZEOF;
    private static final String MATRICES_UNIFORM_BUFFER_NAME = "MatricesUniformBuffer";
    private static final int MATRICES_UNIFORM_BUFFER_MVP_OFFSET = 0;
    private static final int MATRICES_UNIFORM_BUFFER_MODEL_MATRIX_OFFSET = 16 * FLOAT32_SIZEOF;
    private static final int MATRICES_UNIFORM_BUFFER_NORMAL_MATRIX_OFFSET = 32 * FLOAT32_SIZEOF;
    private static final int MATRICES_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET = 48 * FLOAT32_SIZEOF;

    private static final int INSTANCED_MATRICES_UNIFORM_BUFFER_SIZE = (16 + 4) * FLOAT32_SIZEOF;
    private static final int INSTANCED_MATRICES_UNIFORM_BUFFER_PROJECTION_VIEW_MATRIX_OFFSET = 0;
    private static final int INSTANCED_MATRICES_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET = 16 * FLOAT32_SIZEOF;

    private static final int MATERIAL_UNIFORM_BUFFER_SIZE = PhongMaterial.SIZEOF;
    private static final String MATERIAL_UNIFORM_BUFFER_NAME = "MaterialUniformBuffer";

    private static final int MAX_POINT_LIGHTS = 10;
    private static final int MAX_SPOT_LIGHTS = 10;
    private static final int LIGHTS_UNIFORM_BUFFER_SIZE = (1 + MAX_POINT_LIGHTS + MAX_SPOT_LIGHTS) * Light.SIZEOF + INT32_SIZEOF * 2 + FLOAT32_SIZEOF * 4;
    private static final String LIGHTS_UNIFORM_BUFFER_NAME = "LightsUniformBuffer";
    private static final int DIRECTIONAL_LIGHT_OFFSET = 0;
    private static final int POINT_LIGHTS_OFFSET = Light.SIZEOF;
    private static final int SPOT_LIGHTS_OFFSET = POINT_LIGHTS_OFFSET + Light.SIZEOF * MAX_POINT_LIGHTS;
    private static final int AMBIENT_COLOR_OFFSET = SPOT_LIGHTS_OFFSET + Light.SIZEOF * MAX_SPOT_LIGHTS;
    private static final int POINT_LIGHTS_COUNT_OFFSET = AMBIENT_COLOR_OFFSET + FLOAT32_SIZEOF * 4;
    private static final int SPOT_LIGHTS_COUNT_OFFSET = POINT_LIGHTS_COUNT_OFFSET + INT32_SIZEOF;

    private static final String UNIFORM_AMBIENT_MAP_NAME = "u_AmbientMap";
    private static final String UNIFORM_DIFFUSE_MAP_NAME = "u_DiffuseMap";
    private static final String UNIFORM_SPECULAR_MAP_NAME =  "u_SpecularMap";
    private static final String UNIFORM_EMISSIVE_MAP_NAME =  "u_EmissiveMap";

    private static final int SHADOW_CASCADES_COUNT = 3;

    private static final int DEPTH_MAP_SIZE = 4096;

    private GLShaderProgram shader;
    private GLShaderProgram instancedShader;
    private GLShaderProgram directionalDepthShader;

    private GLUniformBuffer matricesUniformBuffer;
    private GLUniformBuffer instancedMatricesUniformBuffer;

    private GLUniformBuffer materialUniformBuffer;
    private GLUniformBuffer lightsUniformBuffer;

    private long lightsUniformBufferData;

    private ShadowCascade[] shadowCascades;
    private GLFramebuffer depthFramebuffer;
    private GLTexture2D[] shadowMaps;

    private Matrix4f projectionViewMatrix;

    private GLPhongRenderingPath() {

    }

    @Override
    protected void init() {

        shader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong.vert")).compile())
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong.frag")).compile())
                .link();

        instancedShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_instanced.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_instanced.frag")))
                .link();

        directionalDepthShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/depth/directional_depth.vert")).compile())
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/depth/depth.frag")).compile())
                .link();

        matricesUniformBuffer = new GLUniformBuffer();
        matricesUniformBuffer.set(MATRICES_UNIFORM_BUFFER_NAME, shader, 0);
        matricesUniformBuffer.allocate(MATRICES_UNIFORM_BUFFER_SIZE);

        instancedMatricesUniformBuffer = new GLUniformBuffer();
        instancedMatricesUniformBuffer.set(MATRICES_UNIFORM_BUFFER_NAME, instancedShader, 0);
        instancedMatricesUniformBuffer.allocate(INSTANCED_MATRICES_UNIFORM_BUFFER_SIZE);

        materialUniformBuffer = new GLUniformBuffer();
        materialUniformBuffer.set(MATERIAL_UNIFORM_BUFFER_NAME, shader, 1).set(MATERIAL_UNIFORM_BUFFER_NAME, instancedShader, 1);
        materialUniformBuffer.allocate(MATERIAL_UNIFORM_BUFFER_SIZE);

        lightsUniformBuffer = new GLUniformBuffer();
        lightsUniformBuffer.set(LIGHTS_UNIFORM_BUFFER_NAME, shader, 2).set(LIGHTS_UNIFORM_BUFFER_NAME, instancedShader, 2);
        lightsUniformBuffer.allocate(LIGHTS_UNIFORM_BUFFER_SIZE);

        lightsUniformBufferData = lightsUniformBuffer.mapMemory(0).get(0);

        projectionViewMatrix = new Matrix4f();

        shadowCascades = new ShadowCascade[SHADOW_CASCADES_COUNT];

        for(int i = 0;i < SHADOW_CASCADES_COUNT;i++) {
            shadowCascades[i] = new ShadowCascade();
        }

        shadowMaps = new GLTexture2D[SHADOW_CASCADES_COUNT];

        for(int i = 0;i < SHADOW_CASCADES_COUNT;i++) {
            GLTexture2D depthMap = shadowMaps[i] = new GLTexture2D();
            depthMap.allocate(1, DEPTH_MAP_SIZE, DEPTH_MAP_SIZE, GL_DEPTH_COMPONENT24);
        }

        depthFramebuffer = new GLFramebuffer();

        depthFramebuffer.drawBuffer(GL_NONE);
        depthFramebuffer.readBuffer(GL_NONE);
    }

    @Override
    protected void terminate() {

        shader.release();

        lightsUniformBuffer.unmapMemory();
        lightsUniformBufferData = NULL;

        matricesUniformBuffer.release();
        instancedMatricesUniformBuffer.release();
        materialUniformBuffer.release();
        lightsUniformBuffer.release();
    }

    @Override
    public void render(Camera camera, Scene scene) {

        Color clearColor = camera.clearColor();

        glEnable(GL_DEPTH_TEST);
        glClearColor(clearColor.red(), clearColor.green(), clearColor.blue(), clearColor.alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        lightsUniformBuffer.bind(instancedShader);
        setLightsUniformBuffer(scene.environment());

        renderInstancedMeshes(camera, scene);

        renderNonInstancedMeshes(camera, scene);
    }

    private void renderInstancedMeshes(Camera camera, Scene scene) {

        final SceneMeshInfo meshInfo = scene.meshInfo();
        final GLMapper mapper = opengl().mapper();

        instancedShader.bind();

        instancedMatricesUniformBuffer.bind(instancedShader);

        materialUniformBuffer.bind(instancedShader);

        try(MemoryStack stack = stackPush()) {

            ByteBuffer materialBufferData = stack.malloc(PhongMaterial.SIZEOF);

            final ByteBuffer matricesBuffer = stack.malloc(INSTANCED_MATRICES_UNIFORM_BUFFER_SIZE);

            camera.projectionViewMatrix().get(matricesBuffer);
            camera.transform().position().get(INSTANCED_MATRICES_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET, matricesBuffer);

            instancedMatricesUniformBuffer.update(0, matricesBuffer);

            for(Mesh mesh : meshInfo.meshes()) {

                if(!mesh.vertexData().layout().instanced()) {
                    continue;
                }

                final List<MeshInstance> meshInstances = meshInfo.instancesOf(mesh);

                GLVertexData vertexData = mesh.vertexData();

                final PhongMaterial material = mesh.material();

                setMaterialUniforms(instancedShader, material, materialBufferData);

                vertexData.bind();

                if (vertexData.indexCount() > 0) {
                    glDrawElementsInstanced(mapper.mapToAPI(vertexData.topology()), vertexData.indexCount(), GL_UNSIGNED_INT, NULL, meshInstances.size());
                } else {
                    glDrawArraysInstanced(mapper.mapToAPI(vertexData.topology()), vertexData.firstVertex(), vertexData.vertexCount(), meshInstances.size());
                }
            }
        }
    }

    private void renderNonInstancedMeshes(Camera camera, Scene scene) {

        final GLShaderProgram shader = this.shader;
        final Matrix4fc projectionView = camera.projectionViewMatrix();
        final Matrix4f mvp = projectionViewMatrix;
        final GLUniformBuffer matricesUniformBuffer = this.matricesUniformBuffer;
        final SceneMeshInfo meshInfo = scene.meshInfo();
        final GLMapper mapper = opengl().mapper();

        shader.bind();

        matricesUniformBuffer.bind(shader);

        materialUniformBuffer.bind(shader);

        try(MemoryStack stack = stackPush()) {

            ByteBuffer materialBufferData = stack.malloc(PhongMaterial.SIZEOF);

            final ByteBuffer matricesBuffer = stack.malloc(MATRICES_UNIFORM_BUFFER_SIZE - 4  * FLOAT32_SIZEOF);

            matricesUniformBuffer.update(MATRICES_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET,
                    camera.transform().position().get(stack.malloc(4 * FLOAT32_SIZEOF)));

            for(Mesh mesh : meshInfo.meshes()) {

                List<MeshInstance> meshInstances = meshInfo.instancesOf(mesh);

                if(mesh.vertexData().instanced()) {
                    continue;
                }

                for(MeshInstance meshInstance : meshInstances) {

                    projectionView.mul(meshInstance.modelMatrix(), mvp).get(MATRICES_UNIFORM_BUFFER_MVP_OFFSET, matricesBuffer);
                    meshInstance.modelMatrix().get(MATRICES_UNIFORM_BUFFER_MODEL_MATRIX_OFFSET, matricesBuffer);
                    meshInstance.normalMatrix().get(MATRICES_UNIFORM_BUFFER_NORMAL_MATRIX_OFFSET, matricesBuffer);

                    matricesUniformBuffer.update(0, matricesBuffer);

                    final GLVertexData vertexData = mesh.vertexData();
                    final PhongMaterial material = mesh.material();

                    vertexData.bind();

                    setMaterialUniforms(shader, material, materialBufferData);

                    if (vertexData.indexCount() > 0) {
                        glDrawElementsInstanced(mapper.mapToAPI(vertexData.topology()), vertexData.indexCount(), GL_UNSIGNED_INT, NULL, 1);
                    } else {
                        glDrawArraysInstanced(mapper.mapToAPI(vertexData.topology()), vertexData.firstVertex(), vertexData.vertexCount(), 1);
                    }

                }
            }
        }
    }

    private void setLightsUniformBuffer(SceneEnvironment environment) {

        final long lightsUniformBufferData = this.lightsUniformBufferData;

        final DirectionalLight directionalLight = environment.directionalLight();
        final int pointLightsCount = environment.pointLightsCount();
        final int spotLightsCount = environment.spotLightsCount();

        try(MemoryStack stack = stackPush()) {

            ByteBuffer directionalLightBuffer = stack.calloc(Light.SIZEOF);

            if(directionalLight != null) {
                directionalLight.get(0, directionalLightBuffer);
            }

            nmemcpy(lightsUniformBufferData + DIRECTIONAL_LIGHT_OFFSET, memAddress(directionalLightBuffer), Light.SIZEOF);

            if(pointLightsCount > 0) {

                ByteBuffer buffer = stack.malloc(pointLightsCount * Light.SIZEOF);

                for(int i = 0;i < pointLightsCount;i++) {
                    environment.pointLight(i).get(i * Light.SIZEOF, buffer);
                }

                nmemcpy(lightsUniformBufferData + POINT_LIGHTS_OFFSET, memAddress(buffer), buffer.limit());
            }

            if(spotLightsCount > 0) {

                ByteBuffer buffer = stack.malloc(spotLightsCount * Light.SIZEOF);

                for(int i = 0;i < spotLightsCount;i++) {
                    environment.spotLight(i).get(i * Light.SIZEOF, buffer);
                }

                nmemcpy(lightsUniformBufferData + SPOT_LIGHTS_OFFSET, memAddress(buffer), buffer.limit());
            }

            ByteBuffer buffer = stack.malloc(Color.SIZEOF + INT32_SIZEOF * 2);

            environment.ambientColor().getRGBA(buffer).putInt(pointLightsCount).putInt(spotLightsCount);

            nmemcpy(lightsUniformBufferData + AMBIENT_COLOR_OFFSET, memAddress0(buffer), buffer.capacity());
        }
    }

    private void setMaterialUniforms(GLShaderProgram shader, PhongMaterial material, ByteBuffer buffer) {

        materialUniformBuffer.update(0, material.get(0, buffer));

        shader.uniformSampler(UNIFORM_AMBIENT_MAP_NAME, material.ambientMap(), 0);
        shader.uniformSampler(UNIFORM_DIFFUSE_MAP_NAME, material.diffuseMap(), 1);
        shader.uniformSampler(UNIFORM_SPECULAR_MAP_NAME, material.specularMap(), 2);
        shader.uniformSampler(UNIFORM_EMISSIVE_MAP_NAME, material.emissiveMap(), 3);
    }

}
