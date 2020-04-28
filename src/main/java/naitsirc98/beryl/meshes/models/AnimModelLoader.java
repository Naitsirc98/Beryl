package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.animations.Animation;
import naitsirc98.beryl.animations.KeyFrame;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.AnimMesh;
import naitsirc98.beryl.meshes.Bone;
import naitsirc98.beryl.meshes.MeshManager;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;

public class AnimModelLoader extends AssimpLoader {

    private static final int FLAGS = aiProcess_OptimizeGraph
            | aiProcess_LimitBoneWeights
            | aiProcess_Triangulate
            | aiProcess_GenNormals
            | aiProcess_GenSmoothNormals
            | aiProcess_GenUVCoords
            | aiProcess_FlipUVs
            | aiProcess_FixInfacingNormals;


    private static final StaticVertexHandler DEFAULT_HANDLER = new StaticVertexHandler();
    private static final NameMapper DEFAULT_NAME_MAPPER = name -> name;


    private final Map<Path, AnimModel> cache;

    public AnimModelLoader() {
        cache = new HashMap<>();
    }

    public synchronized AnimModel load(Path path) {
        return load(path, DEFAULT_HANDLER, DEFAULT_NAME_MAPPER);
    }

    public synchronized AnimModel load(Path path, StaticVertexHandler handler) {
        return load(path, handler, DEFAULT_NAME_MAPPER);
    }

    public synchronized AnimModel load(Path path, NameMapper nameMapper) {
        return load(path, DEFAULT_HANDLER, nameMapper);
    }

    public synchronized AnimModel load(Path path, StaticVertexHandler handler, NameMapper nameMapper) {

        assertNonNull(handler);

        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File " + path + " does not exists");
        }

        if(cache.containsKey(path)) {
            AnimModel model = cache.get(path);
            if(model.released()) {
                cache.remove(path);
            } else {
                return model;
            }
        }

        float start = System.nanoTime();

        AnimModel model = loadAssimp(path, handler, nameMapper);

        float end = (float) ((System.nanoTime() - start) / 1e6);

        Log.info("Model " + path.getName(path.getNameCount() - 1) + " loaded in " + end + " ms");

        cache.put(path, model);

        return model;
    }

    private AnimModel loadAssimp(Path path, StaticVertexHandler handler, NameMapper nameMapper) {

        AIScene aiScene = aiImportFile(path.toString(), FLAGS);

        try {

            if (aiScene == null || aiScene.mRootNode() == null) {
                Log.error("Could not load model", new IllegalStateException(aiGetErrorString()));
                return null;
            }

            if(aiScene.mNumAnimations() == 0) {
                Log.error("This model does not contains animations. Load it with the StaticModelLoader", new RuntimeException());
                return null;
            }

            AINode aiRoot = requireNonNull(aiScene.mRootNode());

            AnimModel model = new AnimModel(path);

            AnimNode root = new AnimNode(model, nameMapper.rename(aiRoot.mName().dataString()), null, aiRoot.mNumChildren(), aiRoot.mNumMeshes());

            model.addNode(root);

            processNode(aiScene, aiRoot, root, model, handler, nameMapper);

            loadAnimations(aiScene, aiRoot, model, nameMapper);

            return model;

        } finally {
            aiReleaseImport(aiScene);
        }
    }

    private void processNode(AIScene aiScene, AINode aiNode, AnimNode node, AnimModel model, StaticVertexHandler handler, NameMapper nameMapper) {

        processNodeMeshes(aiScene, aiNode, node, model, handler, nameMapper);

        processNodeChildren(aiScene, aiNode, node, model, handler, nameMapper);
    }

    private void processNodeChildren(AIScene aiScene, AINode aiNode, AnimNode node, AnimModel model, StaticVertexHandler handler, NameMapper nameMapper) {

        if(aiNode.mNumChildren() == 0) {
            return;
        }

        PointerBuffer children = requireNonNull(aiNode.mChildren());

        for(int i = 0;i < children.limit();i++) {
            AINode aiChild = AINode.create(children.get(i));
            AnimNode child = new AnimNode(node.model(), nameMapper.rename(aiChild.mName().dataString()), node, aiChild.mNumChildren(), aiChild.mNumMeshes());
            processNode(aiScene, aiChild, child, model, handler, nameMapper);
            node.addChild(i, child);
        }
    }

    private void processNodeMeshes(AIScene aiScene, AINode aiNode, AnimNode node, AnimModel model, StaticVertexHandler handler, NameMapper nameMapper) {

        if(aiNode.mNumMeshes() == 0) {
            return;
        }

        PointerBuffer meshes = requireNonNull(aiScene.mMeshes());
        IntBuffer meshIndices = requireNonNull(aiNode.mMeshes());

        for(int i = 0;i < aiNode.mNumMeshes();i++) {
            AIMesh aiMesh = AIMesh.create(meshes.get(meshIndices.get(i)));
            node.addMesh(i, loadMesh(aiMesh, model, handler, nameMapper));
        }
    }

    private AnimMesh loadMesh(AIMesh aiMesh, AnimModel model, StaticVertexHandler handler, NameMapper nameMapper) {

        final String meshName = nameMapper.rename(aiMesh.mName().dataString());

        MeshManager meshManager = MeshManager.get();

        if(meshManager.exists(meshName)) {
            return meshManager.get(meshName);
        }

        ByteBuffer vertices = memAlloc(AnimMesh.VERTEX_DATA_SIZE * aiMesh.mNumVertices());
        ByteBuffer indices = getIndices(aiMesh);

        processPositionAttribute(aiMesh, handler, vertices, AnimMesh.VERTEX_DATA_SIZE);
        processNormalAttribute(aiMesh, handler, vertices, AnimMesh.VERTEX_DATA_SIZE);
        processTexCoordsAttribute(aiMesh, handler, vertices, AnimMesh.VERTEX_DATA_SIZE);
        processBonesAndWeightsAttributes(aiMesh, model, vertices, AnimMesh.VERTEX_DATA_SIZE);

        return AnimMesh.get(meshName, meshData -> meshData.set(vertices, indices));
    }

    private void processBonesAndWeightsAttributes(AIMesh aiMesh, AnimModel model, ByteBuffer vertices, int stride) {

        if(aiMesh.mNumBones() == 0) {
            return;
        }

        PointerBuffer bones = requireNonNull(aiMesh.mBones());

        Map<Integer, Integer> offsets = new HashMap<>();

        for(int boneID = 0;boneID < bones.capacity();boneID++) {

            AIBone aiBone = AIBone.create(bones.get(boneID));

            Bone bone = Bone.get(aiBone.mName().dataString(), matrix4fc(aiBone.mOffsetMatrix()));

            model.addBone(bone);

            AIVertexWeight.Buffer weights = aiBone.mWeights();

            final int numWeights = weights.capacity();

            for(int j = 0;j < numWeights;j++) {

                AIVertexWeight aiVertexWeight = weights.get(j);

                final int vertexID = aiVertexWeight.mVertexId();
                final float weight = aiVertexWeight.mWeight();

                final int offset = offsets.getOrDefault(vertexID, 0);

                if(offset < 4) {

                    vertices.putInt(vertexID * stride + AnimMesh.VERTEX_BONE_IDS_OFFSET + offset * INT32_SIZEOF, boneID);
                    vertices.putFloat(vertexID * stride + AnimMesh.VERTEX_BONE_WEIGHTS_OFFSET + offset * FLOAT32_SIZEOF, weight);

                    offsets.put(vertexID, offset + 1);
                }
            }
        }
    }

    private void loadAnimations(AIScene aiScene, AINode aiRootNode, AnimModel model, NameMapper nameMapper) {

        PointerBuffer animations = requireNonNull(aiScene.mAnimations());

        for(int i = 0;i < animations.capacity();i++) {

            AIAnimation aiAnimation = requireNonNull(AIAnimation.createSafe(animations.get(i)));

            final float duration = (float) aiAnimation.mDuration();

            PointerBuffer channels = requireNonNull(aiAnimation.mChannels());

            for(int j = 0;j < channels.capacity();j++) {

                AINodeAnim aiNodeAnim = requireNonNull(AINodeAnim.createSafe(channels.get(j)));

                AnimNode node = model.node(nameMapper.rename(aiNodeAnim.mNodeName().dataString()));

                setNodeTransformations(aiNodeAnim, node);
            }

            KeyFrame[] keyFrames = getAnimationKeyFrames(aiAnimation, model, matrix4fc(aiRootNode.mTransformation()));
            Animation animation = new Animation(nameMapper.rename(aiAnimation.mName().dataString()), keyFrames, duration);
            model.addAnimation(animation);
        }
    }

    private KeyFrame[] getAnimationKeyFrames(AIAnimation aiAnimation, AnimModel model, Matrix4fc rootTransformation) {

        final int numKeyFrames = model.node(0).numAnimationKeyFrames();

        AIVectorKey.Buffer aiFrameKeys = AINodeAnim.createSafe(aiAnimation.mChannels().get(0)).mPositionKeys();

        KeyFrame[] keyFrames = new KeyFrame[numKeyFrames];

        Matrix4f transformation = new Matrix4f();

        for(int i = 0;i < numKeyFrames;i++) {

            final int numBones = model.bones().size();

            Matrix4fc[] transformations = new Matrix4fc[numBones];

            for(int j = 0;j < numBones;j++) {

                Bone bone = model.bone(j);

                AnimNode node = model.node(bone.name());

                node.computeTransformationAt(i, transformation.identity()).mul(bone.transformation());

                transformations[j] = new Matrix4f(rootTransformation).mul(transformation);
            }

            final float keyFrameTime = (float) aiFrameKeys.get(i).mTime();// * Time.IDEAL_DELTA_TIME;

            keyFrames[i] = new KeyFrame(keyFrameTime, transformations);
        }

        return keyFrames;
    }

    private void setNodeTransformations(AINodeAnim aiNodeAnim, AnimNode node) {

        final int numFrames = aiNodeAnim.mNumPositionKeys();

        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        Quaternionf rotation = new Quaternionf();

        for(int i = 0; i < numFrames; i++) {

            AIVector3D translation = positionKeys.get(i).mValue();

            Matrix4f transformation = new Matrix4f().translate(translation.x(), translation.y(), translation.z());

            if(i < aiNodeAnim.mNumRotationKeys()) {
                AIQuaternion aiQuaternion = rotationKeys.get(i).mValue();
                rotation.set(aiQuaternion.x(), aiQuaternion.y(), aiQuaternion.z(), aiQuaternion.w());
            } else {
                rotation.identity();
            }

            transformation.rotate(rotation);

            if(i < aiNodeAnim.mNumScalingKeys()) {
                AIVector3D scale = scalingKeys.get(i).mValue();
                transformation.scale(scale.x(), scale.y(), scale.z());
            }

            node.addTransformation(transformation);
        }
    }

}
