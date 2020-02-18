package naitsirc98.beryl.util;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;

import java.util.Objects;

/**
 * Utility class for internal assertions
 * */
public final class Asserts {

    private static final boolean ENABLED = BerylConfiguration.ENABLE_ASSERTS.get(Beryl.INTERNAL_DEBUG);

    public static void assertTrue(boolean condition) {
        if(ENABLED) {
            if(!condition) {
                throw new AssertionException();
            }
        }
    }

    public static void assertFalse(boolean condition) {
        if(ENABLED) {
            assertTrue(!condition);
        }
    }

    public static <T> T assertEquals(T actual, T expected) {
        if(ENABLED) {
            if(Objects.equals(actual, expected)) {
                return actual;
            }
            throw new AssertionException(actual, expected, true);
        }
        return actual;
    }

    public static <T> T assertNotEquals(T actual, T other) {
        if(ENABLED) {
            if(Objects.equals(actual, other)) {
                throw new AssertionException(actual, other, false);
            }
            return actual;
        }
        return actual;
    }

    public static <T> T assertNull(T object) {
        if(ENABLED) {
            return assertEquals(object, null);
        }
        return object;
    }

    public static <T> T assertNonNull(T object) {
        if(ENABLED) {
            return assertNotEquals(object, null);
        }
        return object;
    }

    public static <T> T assertOfType(Object object, Class<T> clazz) {
        if(ENABLED) {
            return clazz.cast(object);
        }
        return clazz.cast(object);
    }

    private Asserts() {}

    public static class AssertionException extends IllegalStateException {

        public AssertionException() {
        }

        public AssertionException(Object actual, Object expected, boolean mustBeEquals) {
            this(mustBeEquals
                    ? String.format("Expected %s, but was %s", expected, actual)
                    : String.format("Parameter must not be equal to %s", expected));
        }

        public AssertionException(String msg) {
            super(msg);
        }
    }

}