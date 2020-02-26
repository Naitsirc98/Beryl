package naitsirc98.beryl.util;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;

import java.util.Objects;

/**
 * Utility class for internal assertions
 * */
public final class Asserts {

    public static final boolean ASSERTS_ENABLED = BerylConfiguration.ENABLE_ASSERTS.get(Beryl.INTERNAL_DEBUG);

    public static void assertTrue(boolean condition) {
        if(ASSERTS_ENABLED) {
            if(!condition) {
                throw new AssertionException("Assert condition was false");
            }
        }
    }

    public static void assertFalse(boolean condition) {
        if(ASSERTS_ENABLED) {
            assertTrue(!condition);
        }
    }

    public static <T> T assertEquals(T actual, T expected) {
        if(ASSERTS_ENABLED) {
            if(Objects.equals(actual, expected)) {
                return actual;
            }
            throw new AssertionException(actual, expected, true);
        }
        return actual;
    }

    public static <T> T assertNotEquals(T actual, T other) {
        if(ASSERTS_ENABLED) {
            if(Objects.equals(actual, other)) {
                throw new AssertionException(actual, other, false);
            }
            return actual;
        }
        return actual;
    }

    public static <T> T assertNull(T object) {
        if(ASSERTS_ENABLED) {
            return assertEquals(object, null);
        }
        return object;
    }

    public static <T> T assertNonNull(T object) {
        if(ASSERTS_ENABLED) {
            return assertNotEquals(object, null);
        }
        return object;
    }

    public static <T> T assertOfType(Object object, Class<T> clazz) {
        if(ASSERTS_ENABLED) {
            return clazz.cast(object);
        }
        return clazz.cast(object);
    }

    public static <T> T assertThat(T object, boolean condition) {
        if(ASSERTS_ENABLED) {
            if(!condition) {
                throw new AssertionException();
            }
            return object;
        }
        return object;
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
