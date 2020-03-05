package naitsirc98.beryl.core;

public abstract class BerylSystem {

    private boolean initialized;

    void initialized(boolean initialized) {
        this.initialized = initialized;
    }

    public final boolean initialized() {
        return initialized;
    }

    protected abstract void init();

    protected abstract void terminate();

    protected CharSequence debugReport() {
        return null;
    }

}
