package naitsirc98.beryl.core;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.scenes.SceneManager;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static naitsirc98.beryl.util.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.TypeUtils.newInstance;

public class BerylSystemManager {

    private final BerylSystem[] systems;

    public BerylSystemManager() {
        systems = new BerylSystem[] {
                createSystem(GLFWLibrary.class),
                createSystem(Log.class),
                createSystem(Time.class),
                createSystem(EventManager.class),
                createSystem(Input.class),
                createSystem(Graphics.class),
                createSystem(SceneManager.class)
        };
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        return (T) Arrays.stream(systems)
                .filter(system -> system.getClass().equals(clazz))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(clazz.getName() + " is not a valid Beryl System"));
    }

    public void init() {
        // Initialize systems in order
        stream(systems).forEach(BerylSystem::init);
    }

    public void terminate() {
        // Terminate systems in reverse order
        reverseOrder(systems).forEach(BerylSystem::terminate);
    }

    private Stream<BerylSystem> reverseOrder(BerylSystem[] systems) {
        return IntStream.range(0, systems.length)
                .boxed()
                .map(index -> systems[systems.length - index - 1])
                .filter(Objects::nonNull);
    }

    private <T extends BerylSystem> T createSystem(Class<T> clazz) {
        final T system = newInstance(clazz);
        initSingleton(clazz, system);
        return system;
    }
}
