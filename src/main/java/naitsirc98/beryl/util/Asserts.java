package naitsirc98.beryl.util;

import java.util.Objects;

import static naitsirc98.beryl.core.BerylConfigConstants.ENABLE_ASSERTS;

/**
 * Utility class for internal assertions
 * */
public final class Asserts {

    public static void assertTrue(boolean condition) {
        if(ENABLE_ASSERTS) {
            if(!condition) {
                throw new AssertionException("Assert condition was false");
            }
        }
    }

    public static void assertFalse(boolean condition) {
        if(ENABLE_ASSERTS) {
            assertTrue(!condition);
        }
    }

    public static <T> T assertEquals(T actual, T expected) {
        if(ENABLE_ASSERTS) {
            if(Objects.equals(actual, expected)) {
                return actual;
            }
            throw new AssertionException(actual, expected, true);
        }
        return actual;
    }

    public static <T> T assertNotEquals(T actual, T other) {
        if(ENABLE_ASSERTS) {
            if(Objects.equals(actual, other)) {
                throw new AssertionException(actual, other, false);
            }
            return actual;
        }
        return actual;
    }

    public static <T> T assertNull(T object) {
        if(ENABLE_ASSERTS) {
            return assertEquals(object, null);
        }
        return object;
    }

    public static <T> T assertNonNull(T object) {
        if(ENABLE_ASSERTS) {
            return assertNotEquals(object, null);
        }
        return object;
    }

    public static <T> T assertOfType(Object object, Class<T> clazz) {
        if(ENABLE_ASSERTS) {
            return clazz.cast(object);
        }
        return clazz.cast(object);
    }

    public static <T> T assertThat(T object, boolean condition) {
        if(ENABLE_ASSERTS) {
            if(!condition) {
                throw new AssertionException("The given object " + object + " does not pass the condition");
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
