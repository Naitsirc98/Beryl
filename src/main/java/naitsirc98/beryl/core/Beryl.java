package naitsirc98.beryl.core;

import naitsirc98.beryl.audio.AudioSystem;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.rendering.APIRenderSystem;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.util.Version;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.core.BerylConfigConstants.*;
import static naitsirc98.beryl.util.SystemInfo.*;
import static naitsirc98.beryl.util.types.TypeUtils.initSingleton;

public final class Beryl {

    public static final Version BERYL_VERSION = new Version(1, 0, 0);
    public static final String BERYL_NAME = "Beryl";

    private static final int UPDATES_PER_SECOND = 60;
    private static final float IDEAL_FRAME_DELAY = 1.0f / UPDATES_PER_SECOND;

    public static final String GRAPHICS_THREAD_NAME = Thread.currentThread().getName();

    public static final AtomicBoolean LAUNCHED = new AtomicBoolean(false);

    public static synchronized void launch() {
        launch(new BerylApplication());
    }

    public static synchronized void launch(BerylApplication application) {

        if(!LAUNCHED.compareAndSet(false, true)) {
            throw new ExceptionInInitializerError("Beryl has been already launched");
        }

        BerylConfigConstants.ensureLoaded();

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

        application.onInit();

        systems.init();

        Log.info("Beryl Systems initialized successfully");
    }

    private void run() {

        Log.info("Starting Application...");

        application.start();

        renderSystem = systems.getRenderSystem().getAPIRenderSystem();
        audioSystem = systems.getAudioSystem();
        window = Window.get();

        setup();

        final Time time = systems.getTimeSystem();

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

        if(WINDOW_VISIBLE) {
            Window.get().show();
        }

        update(IDEAL_FRAME_DELAY);

        render();
    }

    private void update(float deltaTime) {

        final EventManager eventManager = systems.getEventManager();
        final Input input = systems.getInputSystem();
        final SceneManager sceneManager = systems.getSceneManager();
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

            systems.getSceneManager().render();

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

            builder.append("\n\t");
        }

        if(EVENTS_DEBUG_REPORT) {
            builder.append("[EVENT-MANAGER]: ").append(systems.getEventManager().debugReport());
            builder.append("\n\t");
        }

        if(SCENES_DEBUG_REPORT) {
            builder.append("[SCENE-MANAGER]: ").append(systems.getEventManager().debugReport());
        }

        return builder.toString();
    }
}
