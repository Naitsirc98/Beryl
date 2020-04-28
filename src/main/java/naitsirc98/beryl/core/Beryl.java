package naitsirc98.beryl.core;

import naitsirc98.beryl.audio.AudioSystem;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.rendering.APIRenderSystem;
import naitsirc98.beryl.graphics.rendering.RenderSystem;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.util.Version;
import org.lwjgl.system.Configuration;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.SystemInfo.*;
import static naitsirc98.beryl.util.types.TypeUtils.initSingleton;

public final class Beryl {

    static {

        final Runnable setConfigurationMethod = BerylConfiguration.SET_CONFIGURATION_METHOD.get(() -> {});

        if(setConfigurationMethod != null) {
            setConfigurationMethod.run();
        }
    }

    public static final Version VERSION = new Version(1, 0, 0);
    public static final String NAME = "Beryl";

    public static final boolean INTERNAL_DEBUG = BerylConfiguration.INTERNAL_DEBUG.get(false);
    public static final boolean DEBUG = BerylConfiguration.DEBUG.get(INTERNAL_DEBUG);

    public static final boolean SHOW_DEBUG_INFO = BerylConfiguration.SHOW_DEBUG_INFO.get(DEBUG);

    private static final boolean SHOW_DEBUG_INFO_ON_WINDOW_TITLE = BerylConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.get(SHOW_DEBUG_INFO);

    public static final String APPLICATION_NAME = BerylConfiguration.APPLICATION_NAME.get(NAME + " Application");
    public static final Version APPLICATION_VERSION = BerylConfiguration.APPLICATION_VERSION.get(() -> new Version(1, 0, 0));

    public static final boolean MEMORY_USAGE_REPORT = BerylConfiguration.MEMORY_USAGE_REPORT.get(DEBUG);

    private static final int UPDATES_PER_SECOND = 60;
    private static final float IDEAL_FRAME_DELAY = 1.0f / UPDATES_PER_SECOND;

    static {
        setLWJGLConfiguration();
        setJOMLConfiguration();
    }

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
            beryl.error(error);
        } finally {
            beryl.terminate();
        }
    }

    private final BerylApplication application;
    private final BerylSystemManager systems;
    private APIRenderSystem renderSystem;
    private AudioSystem audioSystem;
    private Window window;
    private float updateDelay;
    private int updatesPerSecond;
    private int framesPerSecond;

    private Beryl(BerylApplication application) {
        this.application = application;
        initSingleton(BerylApplication.class, application);
        systems = new BerylSystemManager();
    }

    private void init() throws Throwable {

        setLWJGLConfiguration();

        setJOMLConfiguration();

        application.onInit();

        systems.init();

        Log.info("Beryl Systems initialized successfully");
    }

    private void run() {

        Log.info("Starting Application...");

        application.start();

        renderSystem = systems.renderSystem.apiRenderSystem();
        audioSystem = systems.audioSystem;
        window = Window.get();

        setup();

        final Time time = systems.time;

        float lastFrame = Time.time();
        float lastDebugReport = Time.time();
        float deltaTime;

        while(application.running()) {

            final float now = Time.time();
            time.deltaTime = deltaTime = now - lastFrame;
            lastFrame = now;

            update(deltaTime);

            render();
            ++framesPerSecond;

            ++time.frames;

            if(SHOW_DEBUG_INFO && Time.time() - lastDebugReport >= 1.0f) {
                Log.debug(buildDebugReport(framesPerSecond, updatesPerSecond, deltaTime));
                time.ups = updatesPerSecond;
                time.fps = framesPerSecond;
                updatesPerSecond = 0;
                framesPerSecond = 0;
                lastDebugReport = Time.time();
            }
        }
    }

    private void setup() {

        if(BerylConfiguration.WINDOW_VISIBLE.get(true)) {
            Window.get().show();
        }

        update(IDEAL_FRAME_DELAY);

        render();
    }

    private void update(float deltaTime) {

        final EventManager eventManager = systems.eventManager;
        final Input input = systems.input;
        final SceneManager sceneManager = systems.sceneManager;
        final AudioSystem audio = audioSystem;
        final MaterialManager materials = MaterialManager.get();

        updateDelay += deltaTime;

        int updates = 0;

        boolean wasUpdated = false;

        while(updates < UPDATES_PER_SECOND && updateDelay >= IDEAL_FRAME_DELAY) {

            eventManager.processEvents();

            input.update();

            audio.update();

            sceneManager.update();

            application.onUpdate();

            wasUpdated = true;

            updateDelay -= IDEAL_FRAME_DELAY;
            ++updatesPerSecond;
            ++updates;
        }

        if(wasUpdated) {
            materials.update();
            sceneManager.endUpdate();
        }
    }

    private void render() {

        if(window.visible()) {

            renderSystem.begin();

            application.onRenderBegin();

            systems.sceneManager.render();

            application.onRenderEnd();

            renderSystem.end();
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

    private String buildDebugReport(int fps, int ups, float deltaTime) {

        StringBuilder builder = new StringBuilder(
                format("FPS: %d | UPS: %d | DeltaTime: %.6fs | Time: %s | Graphics API: %s",
                        fps, ups, deltaTime, Time.format(), GraphicsAPI.get()));

        if(SHOW_DEBUG_INFO_ON_WINDOW_TITLE) {
            Window.get().title(APPLICATION_NAME + " | [DEBUG INFO]: "
                    + builder.toString()
                    + " | Memory used: " + memoryUsed() / 1024 / 1024 + " MB"
                    + " | Total memory: " + totalMemory() / 1024 / 1024 + " MB");
        }

        builder.append("\n\t");
        if(MEMORY_USAGE_REPORT) {
            builder.append("[JVM MEMORY]: Used = ").append(memoryUsed() / 1024 / 1024)
                    .append(" MB | Total = ").append(totalMemory() / 1024 / 1024)
                    .append(" MB | Max = ").append(maxMemory() / 1024 / 1024).append(" MB");
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

    private static void setLWJGLConfiguration() {
        Configuration.DEBUG_STREAM.set(new LWJGLDebugStream());
        Configuration.DEBUG.set(INTERNAL_DEBUG);
        // Configuration.DEBUG_STACK.set(INTERNAL_DEBUG);
        Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(INTERNAL_DEBUG);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(INTERNAL_DEBUG);
        Configuration.DEBUG_LOADER.set(INTERNAL_DEBUG);
        Configuration.DEBUG_FUNCTIONS.set(INTERNAL_DEBUG);
        Configuration.GLFW_CHECK_THREAD0.set(INTERNAL_DEBUG);
        Configuration.DISABLE_CHECKS.set(!INTERNAL_DEBUG);
        Configuration.DISABLE_FUNCTION_CHECKS.set(!INTERNAL_DEBUG);
    }

    private static void setJOMLConfiguration() {
        System.setProperty("joml.debug", String.valueOf(INTERNAL_DEBUG));
        System.setProperty("joml.fastmath", String.valueOf(BerylConfiguration.FAST_MATH.get(true)));
        System.setProperty("joml.sinLookup", String.valueOf(BerylConfiguration.FAST_MATH.get(true)));
        System.setProperty("joml.format", String.valueOf(false));
    }
}
