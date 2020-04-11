package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.StaticMesh;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;

public class StaticMeshLoader implements ModelLoader {

    private static final int DEFAULT_FLAGS = // aiProcess_OptimizeMeshes
            // aiProcess_OptimizeGraph
                      aiProcess_Triangulate
                    | aiProcess_GenNormals
                    | aiProcess_GenSmoothNormals
                    | aiProcess_GenUVCoords
                    | aiProcess_FlipUVs
                    | aiProcess_JoinIdenticalVertices
                    | aiProcess_FixInfacingNormals;

    // TODO: bounding boxes??

    @Override
    public Model load(Path path) {

        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File " + path + " does not exists");
        }

        double start = System.nanoTime();

        Model model = loadAssimp(path);

        double end = (System.nanoTime() - start) / 1e6;

        Log.info("Model " + path.getName(path.getNameCount() - 1) + " loaded in " + end + " ms");

        return model;
    }

    private Model loadAssimp(Path path) {

        AIScene aiScene = aiImportFile(path.toString(), DEFAULT_FLAGS);

        try {

            if (aiScene == null || aiScene.mRootNode() == null) {
                throw new IllegalStateException("Could not load model: " + aiGetErrorString());
            }

            AINode aiRoot = aiScene.mRootNode();

            Model model = new Model(path, aiScene.mNumMeshes());

            Model.Node rootNode = model.newNode(aiRoot.mName().dataString(), aiRoot.mNumChildren(), aiRoot.mNumMeshes());

            processNode(aiScene, aiRoot, rootNode, model);

            return model;

        } finally {
            aiReleaseImport(aiScene);
        }
    }

    private void processNode(AIScene aiScene, AINode aiNode, Model.Node modelNode, Model model) {

        modelNode.transformation(matrix4fc(aiNode.mTransformation()));

        processNodeMeshes(aiScene, aiNode, modelNode, model);

        processNodeChildren(aiScene, aiNode, modelNode, model);
    }

    private void processNodeChildren(AIScene aiScene, AINode aiNode, Model.Node modelNode, Model model) {

        if (aiNode.mNumChildren() == 0) {
            return;
        }

        PointerBuffer children = requireNonNull(aiNode.mChildren());

        for(int i = 0;i < children.limit();i++) {
            AINode aiChild = AINode.create(children.get(i));
            Model.Node child = model.newNode(aiChild.mName().dataString(), aiChild.mNumChildren(), aiChild.mNumMeshes());
            processNode(aiScene, aiChild, child, model);
            modelNode.addChild(i, child);
        }
    }

    private void processNodeMeshes(AIScene aiScene, AINode aiNode, Model.Node modelNode, Model model) {

        if (aiNode.mNumMeshes() == 0) {
            return;
        }

        PointerBuffer meshes = requireNonNull(aiScene.mMeshes());
        IntBuffer meshIndices = requireNonNull(aiNode.mMeshes());

        for(int i = 0;i < aiNode.mNumMeshes();i++) {
            AIMesh aiMesh = AIMesh.create(meshes.get(meshIndices.get(i)));
            modelNode.addMesh(i, loadMesh(aiScene, aiMesh, model));
        }
    }

    private Model.LoadedMesh loadMesh(AIScene aiScene, AIMesh aiMesh, Model model) {

        ByteBuffer vertices = memAlloc(StaticMesh.VERTEX_DATA_SIZE * aiMesh.mNumVertices());
        ByteBuffer indices = getIndices(aiMesh);

        processPositionAttribute(aiMesh, vertices);
        processNormalAttribute(aiMesh, vertices);
        processTexCoordsAttribute(aiMesh, vertices);

        String meshName = aiMesh.mName().dataString();

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

    private static void processTexCoordsAttribute(AIMesh aiMesh, ByteBuffer vertices) {

        AIVector3D.Buffer textureCoordinates = aiMesh.mTextureCoords(0);

        if (textureCoordinates == null) {
            return;
            // throw new IllegalStateException("Number of texture coordinates is zero");
        }

        final int stride = StaticMesh.VERTEX_DATA_SIZE;
        int offset = StaticMesh.VERTEX_TEXCOORDS_OFFSET;

        for (int i = 0; i < textureCoordinates.remaining(); i++) {

            AIVector3D texCoords = textureCoordinates.get(i);

            vertices.putFloat(offset, texCoords.x())
                    .putFloat(offset + FLOAT32_SIZEOF, texCoords.y());

            offset += stride;
        }

    }

    private static void processNormalAttribute(AIMesh aiMesh, ByteBuffer vertices) {

        AIVector3D.Buffer normals = requireNonNull(aiMesh.mNormals());

        if (normals.remaining() == 0) {
            throw new IllegalStateException("Number of normals is zero");
        }

        final int stride = StaticMesh.VERTEX_DATA_SIZE;
        int offset = Mesh.VERTEX_NORMAL_OFFSET;

        for (int i = 0; i < normals.remaining(); i++) {

            AIVector3D normal = normals.get(i);

            vertices.putFloat(offset, normal.x())
                    .putFloat(offset + FLOAT32_SIZEOF, normal.y())
                    .putFloat(offset + FLOAT32_SIZEOF * 2, normal.z());

            offset += stride;
        }

    }

    private static void processPositionAttribute(AIMesh aiMesh, ByteBuffer vertices) {

        if (aiMesh.mNumVertices() == 0) {
            throw new IllegalStateException("Number of positions is zero");
        }

        AIVector3D.Buffer positions = aiMesh.mVertices();

        final int stride = StaticMesh.VERTEX_DATA_SIZE;
        int offset = 0;

        for (int i = 0; i < positions.remaining(); i++) {

            AIVector3D position = positions.get(i);

            vertices.putFloat(offset, position.x())
                    .putFloat(offset + FLOAT32_SIZEOF, position.y())
                    .putFloat(offset + FLOAT32_SIZEOF * 2, position.z());

            offset += stride;
        }

    }

}
