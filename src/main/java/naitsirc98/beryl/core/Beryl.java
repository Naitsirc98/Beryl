package naitsirc98.beryl.core;

public final class Beryl {

    public static final boolean DEBUG = BerylConfiguration.DEBUG.get(false);
    public static final boolean INTERNAL_DEBUG = BerylConfiguration.INTERNAL_DEBUG.get(DEBUG);

    private final BerylSystemManager systemManager;

    private Beryl() {
        systemManager = new BerylSystemManager();
    }

    private void init() {
        systemManager.init();
    }

    private void run() {

    }

    private void terminate() {
        systemManager.terminate();
    }

}
