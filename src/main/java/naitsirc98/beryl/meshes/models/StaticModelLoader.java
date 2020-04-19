package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMesh;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;

public final class StaticModelLoader extends AssimpLoader {

    private static final int DEFAULT_FLAGS = // aiProcess_OptimizeMeshes
            aiProcess_OptimizeGraph
                             | aiProcess_Triangulate
                             | aiProcess_GenNormals
                             | aiProcess_GenSmoothNormals
                             | aiProcess_GenUVCoords
                             | aiProcess_FlipUVs
                             | aiProcess_JoinIdenticalVertices
                             | aiProcess_FixInfacingNormals;


    private static final StaticModelLoader INSTANCE = new StaticModelLoader();

    private static final StaticVertexHandler DEFAULT_HANDLER = new StaticVertexHandler();

    public static StaticModelLoader get() {
        return INSTANCE;
    }

    private final Map<Path, StaticModel> cache;

    private StaticModelLoader() {
        cache = new HashMap<>();
    }

    public synchronized StaticModel load(String path) {
        return load(Paths.get(path), DEFAULT_HANDLER);
    }

    public synchronized StaticModel load(String path, StaticVertexHandler handler) {
        return load(Paths.get(path), requireNonNull(handler));
    }

    public synchronized StaticModel load(Path path) {
        return load(path, DEFAULT_HANDLER);
    }

    public synchronized StaticModel load(Path path, StaticVertexHandler handler) {

        assertNonNull(handler);

        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File " + path + " does not exists");
        }

        if(cache.containsKey(path)) {
            StaticModel model = cache.get(path);
            if(model.released()) {
                cache.remove(path);
            } else {
                return model;
            }
        }

        float start = System.nanoTime();

        StaticModel model = loadAssimp(path, handler);

        float end = (float) ((System.nanoTime() - start) / 1e6);

        Log.info("Model " + path.getName(path.getNameCount() - 1) + " loaded in " + end + " ms");

        cache.put(path, model);

        return model;
    }

    private StaticModel loadAssimp(Path path, StaticVertexHandler handler) {

        AIScene aiScene = aiImportFile(path.toString(), DEFAULT_FLAGS);

        try {

            if (aiScene == null || aiScene.mRootNode() == null) {
                throw new IllegalStateException("Could not load model: " + aiGetErrorString());
            }

            AINode aiRoot = aiScene.mRootNode();

            StaticModel model = new StaticModel(path, aiScene.mNumMeshes());

            PointerBuffer meshes = aiScene.mMeshes();

            for(int i = 0;i < meshes.capacity();i++) {
                model.meshes()[i] = loadMesh(requireNonNull(AIMesh.createSafe(meshes.get(i))), handler);
            }

            return model;

        } finally {
            aiReleaseImport(aiScene);
        }
    }

    private StaticMesh loadMesh(AIMesh aiMesh, StaticVertexHandler handler) {

        String meshName = aiMesh.mName().dataString();

        MeshManager meshManager = MeshManager.get();

        if(meshManager.exists(meshName)) {
            return meshManager.get(meshName);
        }

        ByteBuffer vertices = memAlloc(StaticMesh.VERTEX_DATA_SIZE * aiMesh.mNumVertices());
        ByteBuffer indices = getIndices(aiMesh);

        processPositionAttribute(aiMesh, handler, vertices);
        processNormalAttribute(aiMesh, handler, vertices);
        processTexCoordsAttribute(aiMesh, handler, vertices);

        return StaticMesh.get(meshName, staticMeshData -> staticMeshData.set(vertices, indices));
    }

}
