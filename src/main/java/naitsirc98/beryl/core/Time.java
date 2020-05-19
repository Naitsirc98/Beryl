package naitsirc98.beryl.core;

import naitsirc98.beryl.util.types.Singleton;

import static org.lwjgl.glfw.GLFW.*;

public final class Time extends BerylSystem {

    public static final float IDEAL_DELTA_TIME = 1.0f / 60.0f;

    @Singleton
    private static Time instance;

    /**
     * Returns the nano time passed since the application started
     *
     * @return nanoseconds  since the application started
     */
    public static float nano() {
        return (float) (seconds() * 1e9);
    }

    /**
     * Returns the milliseconds passed since the application started
     *
     * @return milliseconds passed since the application started
     */
    public static float millis() {
        return seconds() * 1000.0f;
    }

    /**
     * Returns the seconds passed since the application started. This is equivalent to {@link #time()}
     *
     * @return seconds passed since the application started
     */
    public static float seconds() {
        return time();
    }

    /**
     * Returns the minutes passed since the application started.
     *
     * @return minutes passed since the application started
     */
    public static float minutes() {
        return time() / 60.0f;
    }

    /**
     * Returns the hours passed since the application started.
     *
     * @return hours passed since the application started
     */
    public static float hours() {
        return time() / 3600.0f;
    }

    /**
     * Returns the time in seconds since the application started.
     *
     * @return seconds passed since the application started
     */
    public static float time() {
        return (float)glfwGetTime();
    }

    /**
     * Returns a {@link String} in the format hh:mm:ss.
     *
     * @return {@link String} format of this instant
     */
    public static String format() {
        return String.format("%02d:%02d:%02d", (int) hours(), (int) minutes() % 60, (int) seconds() % 60);
    }

    /**
     * Delta time of this frame
     *
     * @return the delta time of this frame
     */
    public static float deltaTime() {
        return instance.deltaTime;
    }

    /**
     * Returns the current (rendered) frames per second value
     *
     * @return current frames per second
     */
    public static float fps() {
        return instance.fps;
    }

    /**
     * Returns the current logic updates per second value
     *
     * @return current updates per second
     */
    public static float ups() {
        return instance.ups;
    }

    /**
     * Returns the number of frames run since the application started
     *
     * @return the number of frames
     * */
    public static long frames() {
        return instance.frames;
    }

    /**
     * Returns the frequency of the underlying timer
     *
     * @return the timer frequency
     */
    public static float frequency() {
        return (float)glfwGetTimerFrequency();
    }

    float deltaTime;
    float fps;
    float ups;
    long frames;

    private Time() {

    }

    @Override
    protected void init() {
        glfwSetTime(BerylConfiguration.INITIAL_TIME_VALUE.getOrDefault(0.0));
    }

    @Override
    protected void terminate() {

    }

}
