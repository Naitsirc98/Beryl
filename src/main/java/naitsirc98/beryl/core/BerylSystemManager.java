package naitsirc98.beryl.core;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.tasks.TaskManager;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
                taskManager = createSystem(TaskManager.class),
                sceneManager = createSystem(SceneManager.class)
        };
    }

    public void init() {
        // Initialize systems in order
        stream(systems).forEach(BerylSystem::init);
    }

    public void terminate() {
        // Terminate systems in reverse order
        for(int i = systems.length - 1;i >= 0;i--) {
            terminate(systems[i]);
        }
    }

    private void terminate(BerylSystem system) {
        try {
            system.terminate();
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
