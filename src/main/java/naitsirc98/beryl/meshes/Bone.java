package naitsirc98.beryl.meshes;

import org.joml.Matrix4fc;

import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;

public final class Bone {

    public static final int SIZEOF = MATRIX4_SIZEOF;

    public static Bone get(String name, Matrix4fc transformation) {

        AnimMeshManager meshManager = MeshManager.get().animMeshManager();

        Bone bone = meshManager.bone(name);

        if(bone == null) {
            bone = new Bone(name, transformation);
            bone.id = meshManager.setBoneData(bone);
        }

        return bone;
    }


    private int id;
    private final String name;
    private final Matrix4fc transformation;

    private Bone(String name, Matrix4fc transformation) {
        this.name = name;
        this.transformation = transformation;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Matrix4fc transformation() {
        return transformation;
    }
}
