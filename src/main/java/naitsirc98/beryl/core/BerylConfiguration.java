package naitsirc98.beryl.core;

import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.window.CursorType;
import naitsirc98.beryl.graphics.window.DisplayMode;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.logging.LogChannel;
import naitsirc98.beryl.util.ANSIColor;
import naitsirc98.beryl.util.Version;
import naitsirc98.beryl.util.geometry.Sizec;
import org.joml.Vector2ic;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static naitsirc98.beryl.core.DefaultBerylConfigurations.*;

/**
 * A class to set different configuration variables. The value of each configuration container will be read once and
 * its contents will be either stored in a {@code static final} variable or used at the moment.
 *
 * A value of null means default value. If another behaviour is wanted, it must be set before {@link Beryl} class loads.
 *
 * This class is heavily based on {@link org.lwjgl.system.Configuration}
 *
 * */
public final class BerylConfiguration<T> {

    static {
        ensureDefaultConfigurationsClassIsLoaded();
    }

    public static final BerylConfiguration<Boolean> DEBUG = new BerylConfiguration<>(DEFAULT_DEBUG);

    public static final BerylConfiguration<Boolean> INTERNAL_DEBUG = new BerylConfiguration<>(DEFAULT_INTERNAL_DEBUG);

    public static final BerylConfiguration<Boolean> SHOW_DEBUG_INFO = new BerylConfiguration<>(DEFAULT_SHOW_DEBUG_INFO);

    public static final BerylConfiguration<Boolean> FAST_MATH = new BerylConfiguration<>(DEFAULT_FAST_MATH);

    public static final BerylConfiguration<Boolean> MULTISAMPLE_ENABLE = new BerylConfiguration<>(DEFAULT_MULTISAMPLE_ENABLE);

    public static final BerylConfiguration<Integer> MSAA_SAMPLES = new BerylConfiguration<>(DEFAULT_MSAA_SAMPLES);

    public static final BerylConfiguration<Boolean> MEMORY_USAGE_REPORT = new BerylConfiguration<>(DEFAULT_MEMORY_USAGE_REPORT);

    public static final BerylConfiguration<Boolean> EVENTS_DEBUG_REPORT = new BerylConfiguration<>(DEFAULT_EVENTS_DEBUG_REPORT);

    public static final BerylConfiguration<Boolean> SCENES_DEBUG_REPORT = new BerylConfiguration<>(DEFAULT_SCENES_DEBUG_REPORT);

    public static final BerylConfiguration<String> APPLICATION_NAME = new BerylConfiguration<>(DEFAULT_APPLICATION_NAME);

    public static final BerylConfiguration<Version> APPLICATION_VERSION = new BerylConfiguration<>(DEFAULT_APPLICATION_VERSION);

    public static final BerylConfiguration<Double> INITIAL_TIME_VALUE = new BerylConfiguration<>(DEFAULT_INITIAL_TIME_VALUE);

    public static final BerylConfiguration<Set<Log.Level>> LOG_LEVELS = new BerylConfiguration<>(DEFAULT_LOG_LEVELS);

    public static final BerylConfiguration<Map<Log.Level, ANSIColor>> LOG_LEVEL_COLORS = new BerylConfiguration<>(DEFAULT_LOG_LEVEL_COLORS);

    public static final BerylConfiguration<Collection<LogChannel>> LOG_CHANNELS = new BerylConfiguration<>(DEFAULT_LOG_CHANNELS);

    public static final BerylConfiguration<DateTimeFormatter> LOG_DATETIME_FORMATTER = new BerylConfiguration<>(DEFAULT_LOG_DATETIME_FORMATTER);

    public static final BerylConfiguration<Boolean> ENABLE_ASSERTS = new BerylConfiguration<>(DEFAULT_ENABLE_ASSERTS);

    public static final BerylConfiguration<GraphicsAPI> GRAPHICS_API = new BerylConfiguration<>(DEFAULT_GRAPHICS_API);

    public static final BerylConfiguration<ShadingModel> SCENE_SHADING_MODEL = new BerylConfiguration<>(DEFAULT_SCENE_SHADING_MODEL);

    public static final BerylConfiguration<Boolean> SHADOWS_ENABLED_ON_START = new BerylConfiguration<>(DEFAULT_SHADOWS_ENABLED_ON_START);

    public static final BerylConfiguration<Boolean> SHOW_DEBUG_INFO_ON_WINDOW_TITLE = new BerylConfiguration<>(DEFAULT_SHOW_DEBUG_INFO_ON_WINDOW_TITLE);

    public static final BerylConfiguration<Vector2ic> WINDOW_POSITION = new BerylConfiguration<>(DEFAULT_WINDOW_POSITION);

    public static final BerylConfiguration<Sizec> WINDOW_SIZE = new BerylConfiguration<>(DEFAULT_WINDOW_SIZE);

    public static final BerylConfiguration<DisplayMode> WINDOW_DISPLAY_MODE = new BerylConfiguration<>(DEFAULT_WINDOW_DISPLAY_MODE);

    public static final BerylConfiguration<CursorType> WINDOW_CURSOR_TYPE = new BerylConfiguration<>(DEFAULT_WINDOW_CURSOR_TYPE);

    public static final BerylConfiguration<Boolean> WINDOW_VISIBLE = new BerylConfiguration<>(DEFAULT_WINDOW_VISIBLE);

    public static final BerylConfiguration<Boolean> WINDOW_RESIZABLE = new BerylConfiguration<>(DEFAULT_WINDOW_RESIZABLE);

    public static final BerylConfiguration<Boolean> WINDOW_FOCUS_ON_SHOW = new BerylConfiguration<>(DEFAULT_WINDOW_FOCUS_ON_SHOW);

    public static final BerylConfiguration<Boolean> VSYNC = new BerylConfiguration<>(DEFAULT_VSYNC);

    public static final BerylConfiguration<Boolean> OPENGL_ENABLE_DEBUG_MESSAGES = new BerylConfiguration<>(DEFAULT_OPENGL_ENABLE_DEBUG_MESSAGES);

    public static final BerylConfiguration<Boolean> OPENGL_ENABLE_WARNINGS_UNIFORMS = new BerylConfiguration<>(DEFAULT_OPENGL_ENABLE_WARNINGS_UNIFORMS);

    public static final BerylConfiguration<Boolean> PRINT_SHADERS_SOURCE = new BerylConfiguration<>(DEFAULT_PRINT_SHADERS_SOURCE);

    public static final BerylConfiguration<Boolean> GRAPHICS_MULTITHREADING_ENABLED = new BerylConfiguration<>(DEFAULT_GRAPHICS_MULTITHREADING_ENABLED);

    public static final BerylConfiguration<String> FIRST_SCENE_NAME = new BerylConfiguration<>(DEFAULT_FIRST_SCENE_NAME);

    static void ensureLoaded() {
    }


    private T value;

    private BerylConfiguration(T defaultValue) {
        value = defaultValue;
    }


    public void set(T value) {
        if(Beryl.LAUNCHED.get()) {
            Log.warning("Setting configuration values after Beryl has been launched has no effect");
        } else {
            this.value = value;
        }
    }

    public T get() {
        return (T) value;
    }
}
