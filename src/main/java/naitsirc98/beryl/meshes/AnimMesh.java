package naitsirc98.beryl.meshes;

import org.joml.Vector4f;
import org.joml.Vector4i;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static naitsirc98.beryl.util.types.DataType.VECTOR2_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.VECTOR4_SIZEOF;

public class AnimMesh extends Mesh {

    public static final int VERTEX_DATA_SIZE = StaticMesh.VERTEX_DATA_SIZE + VECTOR4_SIZEOF * 2;

    public static final int VERTEX_BONE_IDS_OFFSET = VERTEX_TEXCOORDS_OFFSET + VECTOR2_SIZEOF;
    public static final int VERTEX_BONE_WEIGHTS_OFFSET = VERTEX_BONE_IDS_OFFSET + VECTOR4_SIZEOF;

    public static AnimMesh get(String name, Consumer<MeshData> meshData) {

        MeshManager manager = MeshManager.get();

        if(manager.exists(name)) {
            return manager.get(name);
        }

        MeshData data = new MeshData();
        meshData.accept(data);

        return manager.createAnimMesh(name, data.vertices(), data.indices());
    }


    public AnimMesh(int handle, String name, ByteBuffer vertexData, ByteBuffer indexData) {
        super(handle, name, vertexData, indexData, VERTEX_DATA_SIZE);
    }

    public Vector4i boneIDs(int index, Vector4i dest) {
        return dest.set((index * stride) + VERTEX_BONE_IDS_OFFSET, vertexData);
    }

    public Vector4f weights(int index, Vector4f dest) {
        return dest.set((index * stride) + VERTEX_BONE_WEIGHTS_OFFSET, vertexData);
    }

    @Override
    public Class<? extends Mesh> type() {
        return AnimMesh.class;
    }
}
