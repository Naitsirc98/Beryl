package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.AnimMeshRenderer;
import naitsirc98.beryl.meshes.*;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.meshes.views.AnimMeshView;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.animations.Animator;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Map;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class GLAnimMeshRenderer extends GLIndirectRenderer implements AnimMeshRenderer {

    private GLBuffer bonesBuffer;

    public GLAnimMeshRenderer(GLShadowsInfo shadowsInfo) {
        super(shadowsInfo);
    }

    @Override
    public void init() {
        super.init();

        bonesBuffer = new GLBuffer();
    }

    @Override
    public void terminate() {
        super.terminate();
    }

    @Override
    protected GLBuffer getVertexBuffer() {
        return MeshManager.get().storageHandler(AnimMesh.class).vertexBuffer();
    }

    @Override
    protected GLBuffer getIndexBuffer() {
        return MeshManager.get().storageHandler(AnimMesh.class).indexBuffer();
    }

    @Override
    protected int getStride() {
        return AnimMesh.VERTEX_DATA_SIZE;
    }

    @Override
    public MeshInstanceList<?> getInstances(Scene scene) {
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
        shader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect_anim.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.frag")))
                .link();
    }

    @Override
    protected void bindShaderBuffers(Scene scene) {
        super.bindShaderBuffers(scene);
        bonesBuffer.bind(GL_SHADER_STORAGE_BUFFER, 4);
    }

    @Override
    protected boolean prepareInstanceBuffer(Scene scene, MeshInstanceList<?> instances) {

        if(!super.prepareInstanceBuffer(scene, instances)) {
            return false;
        }

        setBonesData(scene);

        return true;
    }

    private void setBonesData(Scene scene) {

        checkBonesBuffer();

        scene.animators().parallelStream().unordered().forEach(this::setAnimationData);
    }

    private void setAnimationData(Animator animator) {

        Map<Integer, Matrix4f> boneTransformations = animator.currentBoneTransformations();

        try(MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.malloc(Bone.SIZEOF);

            boneTransformations.forEach((boneID, transformation) -> {

                transformation.get(buffer);

                bonesBuffer.copy(boneID * Bone.SIZEOF, buffer);
            });
        }
    }

    private void checkBonesBuffer() {

        final int bonesBufferMinSize = BoneStorageHandler.get().count() * Bone.SIZEOF;

        if(bonesBuffer.size() < bonesBufferMinSize) {
            bonesBuffer.reallocate(bonesBufferMinSize);
            bonesBuffer.mapMemory();
        }
    }
}
