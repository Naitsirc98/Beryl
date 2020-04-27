package naitsirc98.beryl.meshes;

public class AnimMeshStorageHandler extends MeshStorageHandler<AnimMesh> {

    private final BoneStorageHandler boneStorageHandler;

    protected AnimMeshStorageHandler() {
        boneStorageHandler = new BoneStorageHandler();
    }

    public BoneStorageHandler boneStorageHandler() {
        return boneStorageHandler;
    }
}
