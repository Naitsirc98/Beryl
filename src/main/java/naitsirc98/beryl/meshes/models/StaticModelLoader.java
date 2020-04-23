package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;

public final class StaticModelLoader extends AssimpLoader {

    private static final int DEFAULT_FLAGS = aiProcess_OptimizeGraph
                             | aiProcess_Triangulate
                             | aiProcess_GenNormals
                             | aiProcess_GenSmoothNormals
                             | aiProcess_GenUVCoords
                             | aiProcess_FlipUVs
                             | aiProcess_JoinIdenticalVertices
                             | aiProcess_FixInfacingNormals;


    private static final StaticModelLoader INSTANCE = new StaticModelLoader();

    private static final StaticVertexHandler DEFAULT_HANDLER = new StaticVertexHandler();

    private static final NameMapper DEFAULT_NAME_MAPPER = name -> name;

    public static StaticModelLoader get() {
        return INSTANCE;
    }

    private final Map<Path, StaticModel> cache;

    private StaticModelLoader() {
        cache = new HashMap<>();
    }

    public synchronized StaticModel load(Path path, boolean loadMaterials) {
        return load(path, loadMaterials, DEFAULT_HANDLER, DEFAULT_NAME_MAPPER);
    }

    public synchronized StaticModel load(Path path, boolean loadMaterials, StaticVertexHandler handler) {
        return load(path, loadMaterials, handler, DEFAULT_NAME_MAPPER);
    }

    public synchronized StaticModel load(Path path, boolean loadMaterials, NameMapper nameMapper) {
        return load(path, loadMaterials, DEFAULT_HANDLER, nameMapper);
    }

    public synchronized StaticModel load(Path path, boolean loadMaterials, StaticVertexHandler handler, NameMapper nameMapper) {

        assertNonNull(handler);

        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File " + path + " does not exists");
        }

        if(cache.containsKey(path)) {
            return cache.get(path);
        }

        float start = System.nanoTime();

        StaticModel model = loadAssimp(path, loadMaterials, handler, nameMapper);

        float end = (float) ((System.nanoTime() - start) / 1e6);

        Log.info("Model " + path.getName(path.getNameCount() - 1) + " loaded in " + end + " ms");

        cache.put(path, model);

        return model;
    }

    private StaticModel loadAssimp(Path path, boolean loadMaterials, StaticVertexHandler handler, NameMapper nameMapper) {

        AIScene aiScene = aiImportFile(path.toString(), DEFAULT_FLAGS);

        try {

            if (aiScene == null || aiScene.mRootNode() == null) {
                throw new IllegalStateException("Could not load model: " + aiGetErrorString());
            }

            StaticModel model = new StaticModel(path);

            PointerBuffer meshes = aiScene.mMeshes();

            Path texturesDir = path.getParent();

            Map<String, Texture2D> texturesCache = new HashMap<>();

            for(int i = 0;i < meshes.capacity();i++) {
                AIMesh aiMesh = requireNonNull(AIMesh.createSafe(meshes.get(i)));
                model.addMeshView(loadMeshView(aiScene, aiMesh, texturesDir, loadMaterials, handler, nameMapper, texturesCache));
            }

            return model;

        } finally {
            aiReleaseImport(aiScene);
        }
    }

    private StaticMeshView loadMeshView(AIScene aiScene, AIMesh aiMesh, Path texturesDir,
                                        boolean loadMaterials, StaticVertexHandler handler, NameMapper nameMapper, Map<String, Texture2D> texturesCache) {

        final String meshName = nameMapper.rename(aiMesh.mName().dataString());

        MeshManager meshManager = MeshManager.get();

        StaticMesh mesh = meshManager.get(meshName);

        if(mesh == null) {

            ByteBuffer vertices = memAlloc(StaticMesh.VERTEX_DATA_SIZE * aiMesh.mNumVertices());
            ByteBuffer indices = getIndices(aiMesh);

            processPositionAttribute(aiMesh, handler, vertices, StaticMesh.VERTEX_DATA_SIZE);
            processNormalAttribute(aiMesh, handler, vertices, StaticMesh.VERTEX_DATA_SIZE);
            processTexCoordsAttribute(aiMesh, handler, vertices, StaticMesh.VERTEX_DATA_SIZE);

            mesh = StaticMesh.get(meshName, staticMeshData -> staticMeshData.set(vertices, indices));
        }

        PhongMaterial material = loadMaterials ? loadMaterial(aiScene, aiMesh, meshName, texturesDir, texturesCache) : PhongMaterial.get(meshName);

        return new StaticMeshView(mesh, material);
    }
}
