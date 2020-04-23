package naitsirc98.beryl.assets;

import naitsirc98.beryl.audio.AudioClipManager;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.util.types.Singleton;

import static naitsirc98.beryl.util.types.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public class Assets extends BerylSystem {

    @Singleton
    private static Assets instance;

    private final MaterialManager materialManager;
    private final MeshManager meshManager;
    private final AudioClipManager audioClipManager;

    public Assets() {
        materialManager = newAssetManager(MaterialManager.class);
        meshManager = newAssetManager(MeshManager.class);
        audioClipManager = newAssetManager(AudioClipManager.class);
    }

    @Override
    protected void init() {
        materialManager.init();
        meshManager.init();
        audioClipManager.init();
    }

    @Override
    protected void terminate() {
        audioClipManager.terminate();
        meshManager.terminate();
        materialManager.terminate();
    }

    private <T extends AssetManager> T newAssetManager(Class<T> clazz) {
        T instance = newInstance(clazz);
        initSingleton(clazz, instance);
        return instance;
    }
}
