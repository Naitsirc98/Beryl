package naitsirc98.beryl.meshes;

import naitsirc98.beryl.assets.AssetManager;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.util.types.Singleton;

import java.nio.ByteBuffer;
import java.util.HashMap;
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
    private Map<Class<? extends Mesh>, MeshStorageHandler<? extends Mesh>> meshStorageHandlers;

    @Override
    public void init() {
        meshHandleProvider = new AtomicInteger(0);
        meshNames = new ConcurrentHashMap<>();
        meshStorageHandlers = createMeshStorageHandlers();
        loadPrimitiveMeshes();
    }

    @SuppressWarnings("unchecked")
    public <T extends Mesh, U extends MeshStorageHandler<T>> U storageHandler(Class<T> meshType) {
        return (U) meshStorageHandlers.get(meshType);
    }

    protected synchronized StaticMesh createStaticMesh(String name, ByteBuffer vertices, ByteBuffer indices) {

        if(invalidMeshData(name, vertices, indices)) {
            return null;
        }

        final int meshHandle = meshHandleProvider.getAndIncrement();

        StaticMesh mesh = new StaticMesh(meshHandle, name, vertices, indices);

        allocate(mesh);

        return mesh;
    }

    protected synchronized TerrainMesh createTerrainMesh(String name, ByteBuffer vertices, ByteBuffer indices, HeightMap heightMap) {

        if(invalidMeshData(name, vertices, indices)) {
            return null;
        }

        final int meshHandle = meshHandleProvider.getAndIncrement();

        TerrainMesh mesh = new TerrainMesh(meshHandle, name, vertices, indices, heightMap);

        allocate(mesh);

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
    @SuppressWarnings("unchecked")
    public void destroy(Mesh mesh) {

        MeshStorageHandler handler = storageHandler(mesh.type());

        handler.free(mesh);

        meshNames.remove(mesh.name());

        mesh.release();
    }

    @Override
    public void destroyAll() {
        meshStorageHandlers.values().forEach(MeshStorageHandler::clear);
        meshNames.values().forEach(Mesh::release);
        meshNames.clear();
    }

    @Override
    public void terminate() {
        meshStorageHandlers.values().forEach(MeshStorageHandler::terminate);
        meshNames.values().forEach(Mesh::release);
    }

    @SuppressWarnings("unchecked")
    private void allocate(Mesh mesh) {

        MeshStorageHandler handler = storageHandler(mesh.type());

        handler.allocate(mesh);

        meshNames.put(mesh.name(), mesh);
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

    private void loadPrimitiveMeshes() {

        StaticModelLoader loader = new StaticModelLoader();

        loader.load(BerylFiles.getPath("models/cube.obj"), false, name -> PrimitiveMeshNames.CUBE_MESH_NAME);
        loader.load(BerylFiles.getPath("models/quad.obj"), false, name -> PrimitiveMeshNames.QUAD_MESH_NAME);
        loader.load(BerylFiles.getPath("models/sphere.obj"), false, name -> PrimitiveMeshNames.SPHERE_MESH_NAME);
    }

    private Map<Class<? extends Mesh>, MeshStorageHandler<? extends Mesh>> createMeshStorageHandlers() {

        Map<Class<? extends Mesh>, MeshStorageHandler<? extends Mesh>> handlers = new HashMap<>();

        handlers.put(StaticMesh.class, new StaticMeshStorageHandler());

        return handlers;
    }

}
