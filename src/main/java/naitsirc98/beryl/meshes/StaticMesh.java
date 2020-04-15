package naitsirc98.beryl.meshes;

import naitsirc98.beryl.assets.Asset;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

public class StaticMesh extends Mesh implements Asset {

    public static final int VERTEX_DATA_SIZE = (3 + 3 + 2) * FLOAT32_SIZEOF;

    public static StaticMesh get(String name, Consumer<StaticMeshData> meshData) {

        MeshManager manager = MeshManager.get();

        if(manager.exists(name)) {
            return manager.get(name);
        }

        StaticMeshData data = new StaticMeshData();
        meshData.accept(data);

        return manager.createStaticMesh(name, data.vertices, data.indices);
    }

    public StaticMesh(int handle, String name, ByteBuffer vertexData, ByteBuffer indexData) {
        super(handle, name, vertexData, indexData, VERTEX_DATA_SIZE);
    }

    @Override
    public Class<? extends Mesh> type() {
        return StaticMesh.class;
    }

    public static final class StaticMeshData {

        private ByteBuffer vertices;
        private ByteBuffer indices;

        public void set(ByteBuffer vertices, ByteBuffer indices) {
            this.vertices = vertices;
            this.indices = indices;
        }
    }

}
