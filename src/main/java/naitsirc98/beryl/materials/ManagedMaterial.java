package naitsirc98.beryl.materials;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ManagedMaterial extends AbstractMaterial {

    private final MaterialStorageInfo storageInfo;
    private final AtomicBoolean modified;

    public ManagedMaterial(String name) {
        super(name);
        storageInfo = new MaterialStorageInfo(sizeof());
        modified = new AtomicBoolean();
    }

    @Override
    public Material tiling(float x, float y) {
        super.tiling(x, y);
        markModified();
        return this;
    }

    public MaterialStorageInfo storageInfo() {
        return storageInfo;
    }

    public boolean modified() {
        return modified.get();
    }

    protected void markModified() {
        if(modified.compareAndSet(false, true)) {
            materialManager.markModified(this);
        }
    }

    void markUpdated() {
        modified.set(false);
    }
}
