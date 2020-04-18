package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMesh;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.UINT32_SIZEOF;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;

public final class StaticMeshLoader {

    private static final int DEFAULT_FLAGS = // aiProcess_OptimizeMeshes
            aiProcess_OptimizeGraph
                             | aiProcess_Triangulate
                             | aiProcess_GenNormals
                             | aiProcess_GenSmoothNormals
                             | aiProcess_GenUVCoords
                             | aiProcess_FlipUVs
                             | aiProcess_JoinIdenticalVertices
                             | aiProcess_FixInfacingNormals;


    private static final StaticMeshLoader INSTANCE = new StaticMeshLoader();

    private static final StaticVertexHandler DEFAULT_HANDLER = new StaticVertexHandler();

    public static StaticMeshLoader get() {
        return INSTANCE;
    }

    private final Map<Path, Model> cache;

    private StaticMeshLoader() {
        cache = new HashMap<>();
    }

    public synchronized Model<StaticMesh> load(String path) {
        return load(Paths.get(path), DEFAULT_HANDLER);
    }

    public synchronized Model<StaticMesh> load(String path, StaticVertexHandler handler) {
        return load(Paths.get(path), requireNonNull(handler));
    }

    public synchronized Model<StaticMesh> load(Path path) {
        return load(path, DEFAULT_HANDLER);
    }

    public synchronized Model<StaticMesh> load(Path path, StaticVertexHandler handler) {

        assertNonNull(handler);

        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File " + path + " does not exists");
        }

        if(cache.containsKey(path)) {
            Model model = cache.get(path);
            if(model.released()) {
                cache.remove(path);
            } else {
                return model;
            }
        }

        double start = System.nanoTime();

        Model<StaticMesh> model = loadAssimp(path, handler);

        double end = (System.nanoTime() - start) / 1e6;

        Log.info("Model " + path.getName(path.getNameCount() - 1) + " loaded in " + end + " ms");

        cache.put(path, model);

        return model;
    }

    private Model<StaticMesh> loadAssimp(Path path, StaticVertexHandler handler) {

        AIScene aiScene = aiImportFile(path.toString(), DEFAULT_FLAGS);

        try {

            if (aiScene == null || aiScene.mRootNode() == null) {
                throw new IllegalStateException("Could not load model: " + aiGetErrorString());
            }

            AINode aiRoot = aiScene.mRootNode();

            Model<StaticMesh> model = new Model<>(path, aiScene.mNumMeshes());

            Model.Node rootNode = model.newNode(aiRoot.mName().dataString(), aiRoot.mNumChildren(), aiRoot.mNumMeshes());

            processNode(aiScene, aiRoot, handler, rootNode, model);

            return model;

        } finally {
            aiReleaseImport(aiScene);
        }
    }

    private void processNode(AIScene aiScene, AINode aiNode, StaticVertexHandler handler, Model.Node modelNode, Model<StaticMesh> model) {

        modelNode.transformation(matrix4fc(aiNode.mTransformation()));

        processNodeMeshes(aiScene, aiNode, handler, modelNode, model);

        processNodeChildren(aiScene, aiNode, handler, modelNode, model);
    }

    private void processNodeChildren(AIScene aiScene, AINode aiNode, StaticVertexHandler handler, Model.Node modelNode, Model<StaticMesh> model) {

        if (aiNode.mNumChildren() == 0) {
            return;
        }

        PointerBuffer children = requireNonNull(aiNode.mChildren());

        for(int i = 0;i < children.limit();i++) {
            AINode aiChild = AINode.create(children.get(i));
            Model.Node child = model.newNode(aiChild.mName().dataString(), aiChild.mNumChildren(), aiChild.mNumMeshes());
            processNode(aiScene, aiChild, handler, child, model);
            modelNode.addChild(i, child);
        }
    }

    private void processNodeMeshes(AIScene aiScene, AINode aiNode, StaticVertexHandler handler, Model.Node modelNode, Model<StaticMesh> model) {

        if (aiNode.mNumMeshes() == 0) {
            return;
        }

        PointerBuffer meshes = requireNonNull(aiScene.mMeshes());
        IntBuffer meshIndices = requireNonNull(aiNode.mMeshes());

        for(int i = 0;i < aiNode.mNumMeshes();i++) {
            AIMesh aiMesh = AIMesh.create(meshes.get(meshIndices.get(i)));
            modelNode.addMesh(i, loadMesh(aiScene, aiMesh, handler, model));
        }
    }

    private ModelMesh<StaticMesh> loadMesh(AIScene aiScene, AIMesh aiMesh, StaticVertexHandler handler, Model<StaticMesh> model) {

        String meshName = aiMesh.mName().dataString();

        MeshManager meshManager = MeshManager.get();

        if(meshManager.exists(meshName)) {
            return model.newMesh(meshName, meshManager.get(meshName));
        }

        ByteBuffer vertices = memAlloc(StaticMesh.VERTEX_DATA_SIZE * aiMesh.mNumVertices());
        ByteBuffer indices = getIndices(aiMesh);

        processPositionAttribute(aiMesh, handler, vertices);
        processNormalAttribute(aiMesh, handler, vertices);
        processTexCoordsAttribute(aiMesh, handler, vertices);

        return model.newMesh(meshName, StaticMesh.get(meshName, meshData -> meshData.set(vertices, indices)));
    }

    private ByteBuffer getIndices(AIMesh aiMesh) {

        final int numFaces = aiMesh.mNumFaces();

        if (numFaces == 0) {
            return null;
        }

        ByteBuffer indices = memAlloc(numFaces * 3 * UINT32_SIZEOF);

        AIFace.Buffer aiFaces = aiMesh.mFaces();

        for (int i = 0; i < numFaces; i++) {

            AIFace aiFace = aiFaces.get(i);

            IntBuffer faceIndices = aiFace.mIndices();

            assertTrue(faceIndices.remaining() == 3);

            for (int j = faceIndices.position(); j < faceIndices.remaining(); j++) {
                indices.putInt(faceIndices.get(j));
            }
        }

        return indices.rewind();
    }

    private Matrix4fc matrix4fc(AIMatrix4x4 aiMatrix4) {
         return new Matrix4f(
                aiMatrix4.a1(), aiMatrix4.b1(), aiMatrix4.c1(), aiMatrix4.d1(),
                aiMatrix4.a2(), aiMatrix4.b2(), aiMatrix4.c2(), aiMatrix4.d2(),
                aiMatrix4.a3(), aiMatrix4.b3(), aiMatrix4.c3(), aiMatrix4.d3(),
                aiMatrix4.a4(), aiMatrix4.b4(), aiMatrix4.c4(), aiMatrix4.d4()
        );
    }

    private static void processTexCoordsAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices) {

        AIVector3D.Buffer textureCoordinates = aiMesh.mTextureCoords(0);

        if (textureCoordinates == null) {
            return;
            // throw new IllegalStateException("Number of texture coordinates is zero");
        }

        final int stride = StaticMesh.VERTEX_DATA_SIZE;
        int offset = StaticMesh.VERTEX_TEXCOORDS_OFFSET;

        Vector2f texCoords = new Vector2f();

        for (int i = 0; i < textureCoordinates.remaining(); i++) {

            AIVector3D aiTexCoords = textureCoordinates.get(i);

            texCoords.set(aiTexCoords.x(), aiTexCoords.y());

            handler.mapTextureCoords(texCoords).get(offset, vertices);

            offset += stride;
        }

    }

    private static void processNormalAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices) {

        AIVector3D.Buffer normals = requireNonNull(aiMesh.mNormals());

        if (normals.remaining() == 0) {
            throw new IllegalStateException("Number of normals is zero");
        }

        final int stride = StaticMesh.VERTEX_DATA_SIZE;
        int offset = Mesh.VERTEX_NORMAL_OFFSET;

        Vector3f normal = new Vector3f();

        for (int i = 0; i < normals.remaining(); i++) {

            AIVector3D aiNormal = normals.get(i);

            normal.set(aiNormal.x(), aiNormal.y(), aiNormal.z());

            handler.mapNormal(normal).get(offset, vertices);

            offset += stride;
        }

    }

    private static void processPositionAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices) {

        if (aiMesh.mNumVertices() == 0) {
            throw new IllegalStateException("Number of positions is zero");
        }

        AIVector3D.Buffer positions = aiMesh.mVertices();

        final int stride = StaticMesh.VERTEX_DATA_SIZE;
        int offset = 0;

        Vector3f position = new Vector3f();

        for (int i = 0; i < positions.remaining(); i++) {

            AIVector3D aiPosition = positions.get(i);

            position.set(aiPosition.x(), aiPosition.y(), aiPosition.z());

            handler.mapPosition(position).get(offset, vertices);

            offset += stride;
        }

    }


}
