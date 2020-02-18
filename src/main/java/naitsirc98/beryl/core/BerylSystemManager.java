package naitsirc98.beryl.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class BerylSystemManager extends BerylSystem {

    // ===> Beryl Systems

    private final BerylSystem[] systems;

    // <===

    public BerylSystemManager() {
        systems = new BerylSystem[] {
                allocate(Log.class),
                allocate(Time.class)
        };
    }

    @Override
    public void init() {
        // Initialize systems in order
        stream(systems).forEach(BerylSystem::init);
    }

    @Override
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

    private <T extends BerylSystem> T allocate(Class<T> clazz) {
        final T system = newInstance(clazz);
        initSingletonInstance(clazz, system);
        return system;
    }

    private <T extends BerylSystem> void initSingletonInstance(Class<T> clazz, T system) {
        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(Singleton.class) != null)
                .filter(field -> (field.getModifiers() & Modifier.STATIC) != 0)
                .findAny()
                .ifPresent(field -> {
                    try {
                        field.setAccessible(true);
                        field.set(null, system);
                    } catch (IllegalAccessException e) {
                        Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot initialize singleton instance of " + clazz.getName(), e);
                    }
                });
    }

    private <T extends BerylSystem> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot instantiate " + clazz.getName(), e);
        }
        return null;
    }

}
