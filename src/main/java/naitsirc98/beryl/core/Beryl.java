package naitsirc98.beryl.core;

import naitsirc98.beryl.graphics.window.Window;
import org.lwjgl.system.Configuration;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.TypeUtils.initSingleton;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;

public final class Beryl {

    public static final boolean INTERNAL_DEBUG = BerylConfiguration.INTERNAL_DEBUG.get(false);
    public static final boolean DEBUG = BerylConfiguration.DEBUG.get(INTERNAL_DEBUG);

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
            beryl.error(error);
        } finally {
            beryl.terminate();
        }
    }

    private final BerylApplication application;
    private final BerylSystemManager systemManager;

    private float updateDelay;
    private int updatesPerSecond;

    private Beryl(BerylApplication application) {
        this.application = application;
        initSingleton(BerylApplication.class, application);
        systemManager = new BerylSystemManager();
    }

    private void init() {

        setLWJGLConfiguration();

        application.onInit();

        systemManager.init();

        Log.info("Beryl Systems initialized successfully");
    }

    private void run() {

        Log.info("Starting Application...");

        application.start();

        // Get frequently used systems
        final Time time = systemManager.get(Time.class);

        float lastFrame = 0.0f;
        float showFPSTimer = 0.0f;

        int framesPerSecond = 0;

        while(application.running()) {

            final float now = Time.time();
            time.deltaTime = now - lastFrame;
            lastFrame = now;

            update(time.deltaTime);

            render();
            ++framesPerSecond;

            if(DEBUG && Time.time() - showFPSTimer >= 1.0f) {
                Log.debug(format("FPS: %d | UPS: %d | DeltaTime: %.3f", framesPerSecond, updatesPerSecond, Time.deltaTime()));
                time.ups = updatesPerSecond;
                time.fps = framesPerSecond;
                updatesPerSecond = 0;
                framesPerSecond = 0;
                showFPSTimer = Time.time();
            }

        }
    }

    private void update(float deltaTime) {

        updateDelay += deltaTime;

        while(updateDelay >= IDEAL_FRAME_DELAY) {

            glfwPollEvents();

            application.onUpdate();

            updateDelay -= IDEAL_FRAME_DELAY;
            ++updatesPerSecond;
        }
    }

    private void render() {
        // TODO
        // For now just simulate some rendering delay
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
