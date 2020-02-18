package naitsirc98.beryl.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

class BerylSystemManager extends BerylSystem {

    // ===> Beryl Systems
    private final Time time;
    // ...
    // <===

    public BerylSystemManager() {
        time = allocate(Time.class);
    }

    @Override
    protected void init() {
        // Initialize systems in order
        time.init();
    }

    @Override
    protected void terminate() {
        // Terminate systems in reverse order
        time.terminate();
    }

    @SuppressWarnings("unchecked")
    private <T> T allocate(Class<T> clazz) {
        try {
            return (T) Stream.of(clazz.getDeclaredMethods())
                    .filter(method -> method.getAnnotation(Allocator.class) != null)
                    .filter(method -> (method.getModifiers() & Modifier.STATIC) != 0)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Cannot find an allocator static method for " + clazz.getName()))
                    .invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
}
