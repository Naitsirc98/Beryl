package naitsirc98.beryl.util.types;

import naitsirc98.beryl.logging.Log;
import sun.misc.Unsafe;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

public final class TypeUtils {

    private static final Unsafe UNSAFE;

    public static <T> T newInstanceUnsafe(Class<T> type) {
        try {
            return type.cast(UNSAFE.allocateInstance(type));
        } catch (InstantiationException e) {
            Log.error("Cannot instantiate unsafe " + type, e);
        }
        return null;
    }

    public static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            Log.error("Cannot invoke constructor for class " + type, e);
        }
        return null;
    }

    public static <T> T newInstance(Class<T> type, Object... args) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor(classesOf(args));
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            Log.error("Cannot invoke constructor for class " + type + " with arguments: " + Arrays.toString(args), e);
        }
        return null;
    }

    public static Class[] classesOf(Object[] objects) {
        Class[] classes = new Class[objects.length];

        for(int i = 0;i < objects.length;i++) {
            classes[i] = objects[i].getClass();
        }

        return classes;
    }

    public static void initSingleton(Class<?> clazz) {
        initSingleton(clazz, newInstance(clazz));
    }

    public static void initSingleton(Class<?> clazz, Object value) {
        setAnnotatedField(clazz, Singleton.class, value);
    }

    public static void setAnnotatedField(Class<?> clazz, Class<? extends Annotation> annotation, Object value) {
        Stream.of(clazz.getDeclaredFields())
                .parallel()
                .filter(f -> nonNull(f.getAnnotation(annotation)))
                .findAny()
                .ifPresent(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(null, value);
                    } catch (IllegalAccessException e) {
                        Log.error("Cannot access field " + field.getName(), e);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAnnotatedField(Class<?> clazz, Class<? extends Annotation> annotation) {
        return (T) Stream.of(clazz.getDeclaredFields())
                .parallel()
                .filter(f -> nonNull(f.getAnnotation(annotation)))
                .findAny()
                .orElse(null);
    }

    public static <T> T singleton(Class<?> clazz) {
        return getAnnotatedField(clazz, Singleton.class);
    }

    public static void deleteSingleton(Class<?> clazz) {
        delete(singleton(clazz));
    }

    public static void delete(Object object) {

        Destructor destructor = object.getClass().getDeclaredAnnotation(Destructor.class);

        if(destructor == null) {
            Log.error("Trying to destroy " + object + ", but it has no destructor");
            return;
        }

        try {

            Method destructorMethod = object.getClass().getDeclaredMethod(destructor.method());
            destructorMethod.setAccessible(true);
            destructorMethod.invoke(object);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.error("Cannot call destructor method of " + object, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation, Object object) {
        Optional<Method> result = Stream.of(clazz.getDeclaredMethods())
                .parallel()
                .filter(method -> nonNull(method.getDeclaredAnnotation(annotation)))
                .findAny();

        if(result.isPresent()) {
            try {
                Method method = result.get();
                method.setAccessible(true);
                return (T) method.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.error("Cannot invoke method " + result.get().getName(), e);
            }
        }

        return null;
    }

    public static <T> T getOrElse(T actual, T orElse) {
        return actual == null ? orElse : actual;
    }

    static {

        Unsafe unsafe = null;

        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.fatal("Cannot instantiate Unsafe instance", e);
        }

        UNSAFE = unsafe;
    }

    private TypeUtils() {
    }
}