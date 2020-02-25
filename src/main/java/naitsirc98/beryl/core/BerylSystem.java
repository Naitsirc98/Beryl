package naitsirc98.beryl.core;

public abstract class BerylSystem {

    boolean initialized;

    protected abstract void init();

    protected abstract void terminate();

    protected CharSequence debugReport() {
        return null;
    }

}
