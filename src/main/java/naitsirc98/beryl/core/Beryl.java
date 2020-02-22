package naitsirc98.beryl.core;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.util.Version;
import org.lwjgl.system.Configuration;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.SystemInfo.*;
import static naitsirc98.beryl.util.TypeUtils.initSingleton;

public final class Beryl {

    public static final Version VERSION = new Version(1, 0, 0);
    public static final String NAME = "Beryl";

    public static final boolean INTERNAL_DEBUG = BerylConfiguration.INTERNAL_DEBUG.get(false);
    public static final boolean DEBUG = BerylConfiguration.DEBUG.get(INTERNAL_DEBUG);

    public static final String APPLICATION_NAME = BerylConfiguration.APPLICATION_NAME.get("");
    public static final Version APPLICATION_VERSION = BerylConfiguration.APPLICATION_VERSION.get(() -> new Version(1, 0, 0));

    public static final boolean MEMORY_USAGE_REPORT = BerylConfiguration.MEMORY_USAGE_REPORT.get(DEBUG);

    private static final int UPDATES_PER_SECOND = 60;
    private static final float IDEAL_FRAME_DELAY = 1.0f / UPDATES_PER_SECOND;

    private static final AtomicBoolean LAUNCHED = new AtomicBoolean(false);

    public static synchronized void launch() {
        launch(new BerylApplication());
    }

    public static synchronized void launch(BerylApplication application) {

        if(!LAUNCHED.compareAndSet(false, true)) {
            throw new ExceptionInInitializerError("Beryl has been already launched");
        }

        Beryl beryl = new Beryl(requireNonNull(application));

        try {
            beryl.init();
            beryl.run();
        } catch (Throwable error) {
            Log.error(error.getMessage(), error);
            beryl.error(error);
        } finally {
            beryl.terminate();
        }
    }

    private final BerylApplication application;
    private final BerylSystemManager systems;
    private float updateDelay;
    private int updatesPerSecond;

    private Beryl(BerylApplication application) {
        this.application = application;
        initSingleton(BerylApplication.class, application);
        systems = new BerylSystemManager();
    }

    private void init() {

        setLWJGLConfiguration();

        application.onInit();

        systems.init();

        Log.info("Beryl Systems initialized successfully");
    }

    private void run() {

        Log.info("Starting Application...");

        application.start();

        final Time time = systems.time;

        float lastFrame = 0.0f;
        float lastDebugReport = 0.0f;
        float deltaTime;

        int framesPerSecond = 0;

        while(application.running()) {

            final float now = Time.time();
            time.deltaTime = deltaTime = now - lastFrame;
            lastFrame = now;

            update(deltaTime);

            render();
            ++framesPerSecond;

            ++time.frames;

            if(DEBUG && Time.time() - lastDebugReport >= 1.0f) {
                Log.debug(buildDebugReport(framesPerSecond, updatesPerSecond, deltaTime));
                time.ups = updatesPerSecond;
                time.fps = framesPerSecond;
                updatesPerSecond = 0;
                framesPerSecond = 0;
                lastDebugReport = Time.time();
            }
        }
    }

    private void update(float deltaTime) {

        final EventManager eventManager = systems.eventManager;
        final Input input = systems.input;
        final SceneManager sceneManager = systems.sceneManager;

        updateDelay += deltaTime;

        while(updateDelay >= IDEAL_FRAME_DELAY) {

            eventManager.processEvents();

            input.update();

            sceneManager.update();

            application.onUpdate();

            updateDelay -= IDEAL_FRAME_DELAY;
            ++updatesPerSecond;
        }
    }

    private void render() {

        systems.sceneManager.render();

        application.onRender();

        // For now just simulate some rendering delay
        for(int i = 0;i < 10000;i++) {
            Math.sin(i);
        }
    }

    private void error(Throwable error) {
        application.onError(error);
    }

    private void terminate() {

        Log.info("Exiting Application...");

        application.onTerminate();

        systems.terminate();
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

    private String buildDebugReport(int fps, int ups, float deltaTime) {

        StringBuilder builder = new StringBuilder(
                format("FPS: %d | UPS: %d | DeltaTime: %.5fs | Time: %.3fs", fps, ups, deltaTime, Time.time()));

        builder.append("\n\t");
        if(MEMORY_USAGE_REPORT) {
            builder.append("[JVM MEMORY]: Used = ").append(getMemoryUsed() / 1024 / 1024)
                    .append(" MB | Total = ").append(getTotalMemory() / 1024 / 1024)
                    .append(" MB | Max = ").append(getMaxMemory() / 1024 / 1024).append(" MB");
        }

        builder.append("\n\t");
        if(EventManager.DEBUG_REPORT_ENABLED) {
            builder.append("[EVENT-MANAGER]: ").append(systems.eventManager.debugReport());
        }

        builder.append("\n\t");
        if(SceneManager.DEBUG_REPORT_ENABLED) {
            builder.append("[SCENE-MANAGER]: ").append(systems.sceneManager.debugReport());
        }

        return builder.toString();
    }
}
