package naitsirc98.beryl.meshes;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.system.MemoryStack.stackPush;

public class BoneStorageHandler {

    private static final int BONES_ID_INITIAL_CAPACITY = 10 * Bone.SIZEOF;

    public static BoneStorageHandler get() {
        AnimMeshStorageHandler animMeshStorageHandler = MeshManager.get().storageHandler(AnimMesh.class);
        return animMeshStorageHandler.boneStorageHandler();
    }

    private final AtomicInteger boneIDsProvider;
    private final List<Bone> bones;
    private final Map<String, Bone> boneNames;
    private final StorageBuffer bonesBuffer;
    private long bonesBufferOffset;

    BoneStorageHandler() {
        boneIDsProvider = new AtomicInteger();
        bones = new ArrayList<>();
        boneNames = new HashMap<>();
        bonesBuffer = createBonesBuffer();
        bonesBufferOffset = 0;
    }

    public Bone bone(int index) {
        return bones.get(index);
    }

    public Bone bone(String name) {
        return boneNames.get(name);
    }

    public synchronized Bone allocate(String name, Matrix4fc transformation) {

        if(bonesBufferOffset >= bonesBuffer.size()) {
            bonesBuffer.resize(bonesBuffer.size() * 2);
        }

        final int boneID = boneIDsProvider.getAndIncrement();

        Bone bone = new Bone(boneID, name, transformation);

        setBoneData(bone);

        bone.storageInfo().index(bones.size());

        bones.add(bone);
        boneNames.put(bone.name(), bone);

        return bone;
    }

    public synchronized void free(Bone bone) {

        BoneStorageInfo boneStorageInfo = bone.storageInfo();

        final int index = boneStorageInfo.index();
        bonesBufferOffset = boneStorageInfo.bonesBufferOffset();

        for(int i = index;i < bones.size();i++) {

            final Bone nextBone = bones.get(i);

            setBoneData(nextBone);

            nextBone.storageInfo().index(i);
        }

        bones.remove(index);
    }

    public int count() {
        return bones.size();
    }

    private void setBoneData(Bone bone) {

        try(MemoryStack stack = stackPush()) {

            ByteBuffer data = stack.malloc(Bone.SIZEOF);

            bone.transformation().get(data);

            bonesBuffer.update(bonesBufferOffset, data);

            bone.storageInfo().bonesBufferOffset(bonesBufferOffset);

            bonesBufferOffset += Bone.SIZEOF;
        }
    }

    private StorageBuffer createBonesBuffer() {
        StorageBuffer bonesBuffer = GraphicsFactory.get().newStorageBuffer();
        bonesBuffer.allocate(BONES_ID_INITIAL_CAPACITY);
        return bonesBuffer;
    }
}
