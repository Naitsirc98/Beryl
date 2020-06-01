package naitsirc98.beryl.core;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.util.types.Singleton;

public abstract class BerylApplication {

    // Application instance used by Beryl
    @Singleton
    private static BerylApplication instance;

    public static void exit() {
        instance.running = false;
    }

    private volatile boolean running;

    public BerylApplication() {
    }

    public final boolean running() {
        return running;
    }

    protected void onInit() {

    }

    protected abstract void onStart(Scene scene);

    protected void onUpdate() {

    }

    protected void onRenderBegin() {

    }

    protected void onRenderEnd() {

    }

    protected void onError(Throwable error) {
        Log.error("An unexpected error has crashed the Application" + errorMessage(error), error);
    }

    private String errorMessage(Throwable error) {
        String message = error.getMessage();
        return message == null ? "" : message;
    }

    protected void onTerminate() {

    }

    void start(Scene scene) {
        running = true;
        onStart(scene);
    }

}
