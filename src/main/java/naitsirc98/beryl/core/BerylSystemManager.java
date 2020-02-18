package naitsirc98.beryl.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

class BerylSystemManager extends BerylSystem {

    // ===> Beryl Systems

    private final Log log;
    private final Time time;


    // <===

    public BerylSystemManager() {
        log = allocate(Log.class);
        time = allocate(Time.class);
    }

    @Override
    protected void init() {
        // Initialize systems in order
        log.init();
        time.init();
    }

    @Override
    protected void terminate() {
        // Terminate systems in reverse order
        time.terminate();
        log.terminate();
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
