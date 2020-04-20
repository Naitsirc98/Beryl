package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.AnimMeshRenderer;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.meshes.AnimMesh;
import naitsirc98.beryl.meshes.AnimMeshManager;
import naitsirc98.beryl.meshes.Bone;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.meshes.views.AnimMeshView;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.animations.Animator;
import naitsirc98.beryl.scenes.components.meshes.AnimMeshInstance;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Map;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class GLAnimMeshRenderer extends GLIndirectRenderer implements AnimMeshRenderer {

    private GLBuffer bonesBuffer;

    @Override
    public void init() {
        super.init();

        bonesBuffer = new GLBuffer();
    }

    @Override
    public void terminate() {
        super.terminate();
    }

    GLShaderProgram renderShader() {
        return renderShader;
    }

    public void prepare(Scene scene) {
        updateVertexArrayVertexBuffer();
    }

    @Override
    public void render(Scene scene) {
        renderScene(scene, getAnimMeshInstances(scene), vertexArray, renderShader);
    }

    public void render(Scene scene, GLShaderProgram shader) {
        renderScene(scene, getAnimMeshInstances(scene), vertexArray, shader);
    }

    public void render(Scene scene, int drawCount) {
        renderScene(scene, drawCount, vertexArray, renderShader);
    }

    public void render(Scene scene, GLShaderProgram shader, int drawCount) {
        renderScene(scene, drawCount, vertexArray, shader);
    }

    public int performCullingPassCPU(Scene scene, boolean alwaysPass) {
        return performCullingPassCPU(scene, getAnimMeshInstances(scene), vertexArray, alwaysPass);
    }

    private void updateVertexArrayVertexBuffer() {

        AnimMeshManager animMeshManager = MeshManager.get().animMeshManager();

        GLBuffer vertexBuffer = animMeshManager.vertexBuffer();
        GLBuffer indexBuffer = animMeshManager.indexBuffer();

        vertexArray.setVertexBuffer(0, vertexBuffer, AnimMesh.VERTEX_DATA_SIZE);
        vertexArray.setIndexBuffer(indexBuffer);
    }

    public MeshInstanceList<AnimMeshInstance> getAnimMeshInstances(Scene scene) {
        return scene.meshInfo().meshViewsOfType(AnimMeshView.class);
    }

    @Override
    protected void initVertexArray() {

        vertexArray = new GLVertexArray();

        VertexLayout vertexLayout = new VertexLayout.Builder(2)
                .put(0, 0, POSITION3D, NORMAL, TEXCOORDS2D, BONE_IDS, BONE_WEIGHTS)
                .put(1, 5, INDEX, INDEX).instanced(1, true)
                .build();

        for (int i = 0; i < vertexLayout.bindings(); i++) {
            vertexArray.setVertexAttributes(i, vertexLayout.attributeList(i));
        }

        instanceBuffer = new GLBuffer("INSTANCE_VERTEX_BUFFER");
    }

    @Override
    protected void initRenderShader() {
        renderShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect_anim.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.frag")))
                .link();
    }

    protected void renderScene(Scene scene, int drawCount, GLVertexArray vertexArray, GLShaderProgram shader) {

        final GLBuffer lightsUniformBuffer = scene.environment().buffer();
        final GLBuffer materialsBuffer = MaterialManager.get().buffer();
        final GLBuffer cameraUniformBuffer = scene.cameraInfo().cameraBuffer();

        setOpenGLState(scene);

        shader.bind();

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        bonesBuffer.bind(GL_SHADER_STORAGE_BUFFER, 4);

        instanceCommandBuffer.bind(GL_DRAW_INDIRECT_BUFFER);

        vertexArray.bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, drawCount, 0);
    }

    protected void prepareInstanceBuffer(Scene scene, MeshInstanceList<?> instances, GLVertexArray vertexArray) {

        super.prepareInstanceBuffer(scene, instances, vertexArray);

        if(instances == null || instances.size() == 0) {
            return;
        }

        final int bonesBufferMinSize = MeshManager.get().animMeshManager().bonesCount() * Bone.SIZEOF;

        if(bonesBuffer.size() < bonesBufferMinSize) {
            bonesBuffer.reallocate(bonesBufferMinSize);
            bonesBuffer.mapMemory();
        }

        Animator animator = (Animator) instances.get(0).get(Animator.class);

        Map<Integer, Matrix4f> boneTransformations = animator.currentBoneTransformations();

        try(MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.malloc(Bone.SIZEOF);

            boneTransformations.forEach((boneID, transformation) -> {

                transformation.get(buffer);

                bonesBuffer.copy(boneID * Bone.SIZEOF, buffer);
            });
        }
    }
}
