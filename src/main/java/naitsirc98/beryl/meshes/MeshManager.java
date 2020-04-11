package naitsirc98.beryl.meshes;

import naitsirc98.beryl.assets.AssetManager;
import naitsirc98.beryl.logging.Log;
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

    @Override
    public void init() {
        meshHandleProvider = new AtomicInteger(0);
        meshNames = new ConcurrentHashMap<>();
        staticMeshManager = new StaticMeshManager();
    }

    synchronized StaticMesh createStaticMesh(String name, ByteBuffer vertices, ByteBuffer indices) {

        if(!checkMeshData(name, vertices, indices)) {
            return null;
        }

        return staticMeshManager.create(meshHandleProvider.getAndIncrement(), name, vertices, indices);
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

    private boolean checkMeshData(String name, ByteBuffer vertices, ByteBuffer indices) {

        if(name == null) {
            Log.fatal("Mesh name cannot be null");
            return false;
        }

        if(meshNames.containsKey(name)) {
            Log.fatal("There is already a mesh called");
            return false;
        }

        if(vertices == null) {
            Log.fatal("Vertices cannot be null");
            return false;
        }

        if(indices == null) {
            Log.fatal("Indices cannot be null");
            return false;
        }

        return true;
    }

}
