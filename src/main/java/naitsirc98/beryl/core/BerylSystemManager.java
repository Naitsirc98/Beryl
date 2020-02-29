package naitsirc98.beryl.core;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.rendering.RenderingPaths;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.tasks.TaskManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.stream;
import static naitsirc98.beryl.util.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.TypeUtils.newInstance;

public class BerylSystemManager {

    final Log log;
    final GLFWLibrary glfwLibrary;
    final Time time;
    final EventManager eventManager;
    final Input input;
    final Graphics graphics;
    final RenderingPaths renderingPaths;
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
                renderingPaths = createSystem(RenderingPaths.class),
                taskManager = createSystem(TaskManager.class),
                sceneManager = createSystem(SceneManager.class)
        };
    }

    public void init() throws Throwable {
        // Initialize systems in order
        Throwable error = null;
        for(int i = 0;i < systems.length;i++) {
            error = initialize(systems[i]);
        }
        if(error != null) {
            throw error;
        }
    }

    private Throwable initialize(BerylSystem system) {
        try {
            if(system != null) {
                system.init();
                system.initialized = true;
            }
        } catch(Throwable e) {
            Logger.getLogger(BerylSystemManager.class.getSimpleName())
                    .log(Level.SEVERE, "Failed to initialize system " + system, e);
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
            if(system != null && system.initialized) {
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
