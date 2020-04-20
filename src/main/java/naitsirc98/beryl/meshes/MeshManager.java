package naitsirc98.beryl.meshes;

import naitsirc98.beryl.assets.AssetManager;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.util.types.Singleton;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class MeshManager implements AssetManager<Mesh> {

    @Singleton
    private static MeshManager instance;

    public static MeshManager get() {
        return instance;
    }

    private AtomicInteger meshHandleProvider;
    private Map<String, Mesh> meshNames;
    private StaticMeshManager staticMeshManager;
    private AnimMeshManager animMeshManager;

    @Override
    public void init() {
        meshHandleProvider = new AtomicInteger(0);
        meshNames = new ConcurrentHashMap<>();
        staticMeshManager = new StaticMeshManager();
        animMeshManager = new AnimMeshManager();
        loadBasicMeshes();
    }

    synchronized StaticMesh createStaticMesh(String name, ByteBuffer vertices, ByteBuffer indices) {

        if(invalidMeshData(name, vertices, indices)) {
            return null;
        }

        StaticMesh mesh = new StaticMesh(meshHandleProvider.getAndIncrement(), name, vertices, indices);

        staticMeshManager.setStaticMeshInfo(mesh);

        meshNames.put(name, mesh);

        return mesh;
    }

    synchronized AnimMesh createAnimMesh(String name, ByteBuffer vertices, ByteBuffer indices) {

        if(invalidMeshData(name, vertices, indices)) {
            return null;
        }

        AnimMesh mesh = new AnimMesh(meshHandleProvider.getAndIncrement(), name, vertices, indices);

        animMeshManager.setAnimMeshInfo(mesh);

        meshNames.put(name, mesh);

        return mesh;
    }

    synchronized TerrainMesh createTerrainMesh(String name, ByteBuffer vertices, ByteBuffer indices,
                                               float size, float[][] heightMap, float minY, float maxY) {

        if(invalidMeshData(name, vertices, indices)) {
            return null;
        }

        TerrainMesh mesh = new TerrainMesh(meshHandleProvider.getAndIncrement(), name, vertices, indices, size, heightMap, minY, maxY);

        staticMeshManager.setStaticMeshInfo(mesh);

        staticMeshManager.setStaticMeshInfo(mesh);

        meshNames.put(name, mesh);

        return mesh;
    }

    @Override
    public int count() {
        return meshNames.size();
    }

    @Override
    public boolean exists(String assetName) {
        return meshNames.containsKey(assetName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K extends Mesh> K get(String assetName) {
        return (K) meshNames.get(assetName);
    }

    @Override
    public void destroy(Mesh mesh) {

        if(mesh instanceof StaticMesh) {
            staticMeshManager.destroy((StaticMesh) mesh);
        }

        meshNames.remove(mesh.name());

        mesh.release();
    }

    @Override
    public void destroyAll() {
        meshNames.values().forEach(mesh -> {

            if(mesh instanceof StaticMesh) {
                staticMeshManager.destroy((StaticMesh) mesh);
            }

            mesh.release();
        });

        meshNames.clear();

        staticMeshManager.clear();
    }

    @Override
    public void terminate() {
        destroyAll();
        staticMeshManager.terminate();
    }

    public StaticMeshManager staticMeshManager() {
        return staticMeshManager;
    }

    public AnimMeshManager animMeshManager() {
        return animMeshManager;
    }

    private boolean invalidMeshData(String name, ByteBuffer vertices, ByteBuffer indices) {

        if(name == null) {
            Log.fatal("Mesh name cannot be null");
            return true;
        }

        if(meshNames.containsKey(name)) {
            Log.fatal("There is already a mesh called");
            return true;
        }

        if(vertices == null) {
            Log.fatal("Vertices cannot be null");
            return true;
        }

        if(indices == null) {
            Log.fatal("Indices cannot be null");
            return true;
        }

        return false;
    }

    private void loadBasicMeshes() {

        StaticModelLoader loader = StaticModelLoader.get();

        loader.load(BerylFiles.getPath("models/cube.obj"), name -> "CUBE");
        loader.load(BerylFiles.getPath("models/quad.obj"), name -> "QUAD");
        loader.load(BerylFiles.getPath("models/sphere.obj"), name -> "SPHERE");
    }

}
