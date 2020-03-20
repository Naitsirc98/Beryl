package naitsirc98.beryl.core;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.rendering.RenderSystem;
import naitsirc98.beryl.graphics.rendering.RenderingPaths;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.ResourceManager;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.tasks.TaskManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import static naitsirc98.beryl.util.types.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public class BerylSystemManager {

    final Log log;
    final GLFWLibrary glfwLibrary;
    final Time time;
    final EventManager eventManager;
    final Input input;
    final Graphics graphics;
    final ResourceManager resourceManager;
    final RenderingPaths renderingPaths;
    final RenderSystem renderSystem;
    final TaskManager taskManager;
    final SceneManager sceneManager;
    private final BerylSystem[] systems;

    public BerylSystemManager() {
        systems = new BerylSystem[] {
                log = createSystem(Log.class),
                glfwLibrary = createSystem(GLFWLibrary.class),
                time = createSystem(Time.class),
                eventManager = createSystem(EventManager.class),
                input = createSystem(Input.class),
                graphics = createSystem(Graphics.class),
                resourceManager = createSystem(ResourceManager.class),
                renderingPaths = createSystem(RenderingPaths.class),
                renderSystem = createSystem(RenderSystem.class),
                taskManager = createSystem(TaskManager.class),
                sceneManager = createSystem(SceneManager.class)
        };
    }

    public void init() throws Throwable {
        // Initialize systems in order
        Throwable error = null;
        for(BerylSystem system : systems) {
            error = initialize(system);
        }
        if(error != null) {
            throw error;
        }
    }

    private Throwable initialize(BerylSystem system) {
        try {

            double time = System.nanoTime();

            if(log.initialized()) {
                Log.info("Initializing " + system.getClass().getSimpleName() + "...");
            } else {
                Logger.getLogger(getClass().getSimpleName()).info("Initializing " + system.getClass().getSimpleName() + "...");
            }

            system.init();
            system.initialized(true);

            time = (System.nanoTime() - time) / 1e6;

            if(log.initialized()) {
                Log.info(system.getClass().getSimpleName() + " initialized in " + time + " ms");
            } else {
                Logger.getLogger(getClass().getSimpleName()).info(system.getClass().getSimpleName() + " initialized in " + time + " ms");
            }

        } catch(Throwable e) {
            Logger.getLogger(BerylSystemManager.class.getSimpleName())
                    .log(Level.SEVERE, "Failed to initialize system " + system.getClass().getSimpleName(), e);
            return e;
        }
        return null;
    }

    public void terminate() {
        // Terminate systems in reverse order
        for(int i = systems.length - 1;i >= 0;i--) {
            terminate(systems[i]);
        }
    }

    private void terminate(BerylSystem system) {
        try {
            if(system != null && system.initialized()) {
                system.terminate();
            }
        } catch(Throwable e) {
            Logger.getLogger(BerylSystemManager.class.getSimpleName())
                    .log(Level.SEVERE, "Failed to terminate system " + system, e);
        }
    }

    private <T extends BerylSystem> T createSystem(Class<T> clazz) {
        final T system = newInstance(clazz);
        initSingleton(clazz, system);
        return system;
    }
}
