package io.p13i.glassnotes.utilities;

public class Assert {
    public static void that(boolean condition) {
        that(condition, "Assertion failed");
    }

    public static void that(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }
}
