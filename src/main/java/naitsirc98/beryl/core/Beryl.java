package naitsirc98.beryl.core;

import org.lwjgl.system.Configuration;

import static java.util.Objects.requireNonNull;

public final class Beryl {

    public static final boolean INTERNAL_DEBUG = BerylConfiguration.INTERNAL_DEBUG.get(false);
    public static final boolean DEBUG = BerylConfiguration.DEBUG.get(INTERNAL_DEBUG);

    public static void launch() {
        launch(new BerylApplication());
    }

    public static void launch(BerylApplication application) {

        Beryl beryl = new Beryl(requireNonNull(application));

        try {
            beryl.init();
            beryl.run();
        } catch (Throwable error) {
            beryl.error(error);
        } finally {
            beryl.terminate();
        }
    }

    private final BerylApplication application;
    private final BerylSystemManager systemManager;

    private Beryl(BerylApplication application) {
        this.application = application;
        systemManager = new BerylSystemManager();
    }

    private void init() {

        application.onInit();

        setLWJGLConfiguration();

        systemManager.init();

        Log.info("Beryl Systems initialized successfully");
    }

    private void run() {

        Log.info("Starting Application...");

        application.onStart();

        // main loop
    }

    private void update() {

    }

    private void render() {

    }

    private void error(Throwable error) {
        application.onError(error);
    }

    private void terminate() {

        Log.info("Exiting Application...");

        application.onTerminate();

        systemManager.terminate();
    }


    private void setLWJGLConfiguration() {
        Configuration.DEBUG.set(INTERNAL_DEBUG);
        Configuration.DEBUG_STACK.set(INTERNAL_DEBUG);
        Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(INTERNAL_DEBUG);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(INTERNAL_DEBUG);
        Configuration.DEBUG_LOADER.set(INTERNAL_DEBUG);
        Configuration.DEBUG_FUNCTIONS.set(INTERNAL_DEBUG);
        Configuration.DEBUG_STREAM.set(INTERNAL_DEBUG);
        Configuration.GLFW_CHECK_THREAD0.set(INTERNAL_DEBUG);
        Configuration.DISABLE_CHECKS.set(!INTERNAL_DEBUG);
        Configuration.DISABLE_FUNCTION_CHECKS.set(!INTERNAL_DEBUG);
    }

}
