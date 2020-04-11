package naitsirc98.beryl.assets;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.util.types.Singleton;

public class Assets extends BerylSystem {

    @Singleton
    private static Assets instance;

    public static MaterialManager materialManager() {
        return instance.materialManager;
    }

    private final MaterialManager materialManager;

    public Assets() {
        materialManager = new MaterialManager();
    }

    @Override
    protected void init() {
        materialManager.init();
    }

    @Override
    protected void terminate() {
        materialManager.terminate();
    }
}
