package naitsirc98.beryl.util;

import naitsirc98.beryl.core.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

public final class TypeUtils {

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

    public static Class<?>[] classesOf(Object[] objects) {
        return (Class<?>[]) Arrays.stream(objects)
                .map(object -> object == null ? null : object.getClass())
                .toArray();
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
    public static <T> T callAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        Optional<Method> result = Stream.of(clazz.getDeclaredMethods())
                .parallel()
                .filter(method -> nonNull(method.getDeclaredAnnotation(annotation)))
                .findAny();

        if(result.isPresent()) {
            try {
                return (T) result.get().invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.error("Cannot invoke method " + result.get().getName());
            }
        }

        return null;
    }


    private TypeUtils() {
    }
}
