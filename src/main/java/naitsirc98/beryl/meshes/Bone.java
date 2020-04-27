package naitsirc98.beryl.meshes;

import org.joml.Matrix4fc;

import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;

public final class Bone {

    public static final int SIZEOF = MATRIX4_SIZEOF;

    public static Bone get(String name, Matrix4fc transformation) {

        BoneStorageHandler boneStorageHandler = BoneStorageHandler.get();

        Bone bone = boneStorageHandler.bone(name);

        if(bone == null) {
            bone = boneStorageHandler.allocate(name, transformation);
        }

        return bone;
    }

    private final int id;
    private final String name;
    private final Matrix4fc transformation;
    private final BoneStorageInfo storageInfo;

    Bone(int id, String name, Matrix4fc transformation) {
        this.id = id;
        this.name = name;
        this.transformation = transformation;
        storageInfo = new BoneStorageInfo();
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

    public BoneStorageInfo storageInfo() {
        return storageInfo;
    }
}
