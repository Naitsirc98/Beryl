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
import org.lwjgl.system.Configuration;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class BerylConfigConstants {

    static {
        BerylConfiguration.ensureLoaded();
    }

    public static final boolean DEBUG = BerylConfiguration.DEBUG.get();

    public static final boolean INTERNAL_DEBUG = BerylConfiguration.INTERNAL_DEBUG.get();

    public static final boolean SHOW_DEBUG_INFO = BerylConfiguration.SHOW_DEBUG_INFO.get();

    public static final boolean FAST_MATH = BerylConfiguration.FAST_MATH.get();

    public static final boolean MULTISAMPLE_ENABLE = BerylConfiguration.MULTISAMPLE_ENABLE.get();

    public static final int MSAA_SAMPLES = BerylConfiguration.MSAA_SAMPLES.get();

    public static final boolean MEMORY_USAGE_REPORT = BerylConfiguration.MEMORY_USAGE_REPORT.get();

    public static final boolean EVENTS_DEBUG_REPORT = BerylConfiguration.EVENTS_DEBUG_REPORT.get();

    public static final boolean SCENES_DEBUG_REPORT = BerylConfiguration.SCENES_DEBUG_REPORT.get();

    public static final String APPLICATION_NAME = BerylConfiguration.APPLICATION_NAME.get();

    public static final Version APPLICATION_VERSION = BerylConfiguration.APPLICATION_VERSION.get();

    public static final double INITIAL_TIME_VALUE = BerylConfiguration.INITIAL_TIME_VALUE.get();

    public static final Set<Log.Level> LOG_LEVELS = BerylConfiguration.LOG_LEVELS.get();

    public static final Map<Log.Level, ANSIColor> LOG_LEVEL_COLORS = BerylConfiguration.LOG_LEVEL_COLORS.get();

    public static final Collection<LogChannel> LOG_CHANNELS = BerylConfiguration.LOG_CHANNELS.get();

    public static final DateTimeFormatter LOG_DATETIME_FORMATTER = BerylConfiguration.LOG_DATETIME_FORMATTER.get();

    public static final boolean ENABLE_ASSERTS = BerylConfiguration.ENABLE_ASSERTS.get();

    public static final GraphicsAPI GRAPHICS_API = BerylConfiguration.GRAPHICS_API.get();

    public static final ShadingModel SCENE_SHADING_MODEL = BerylConfiguration.SCENE_SHADING_MODEL.get();

    public static final boolean SHADOWS_ENABLED_ON_START = BerylConfiguration.SHADOWS_ENABLED_ON_START.get();

    public static final boolean SHOW_DEBUG_INFO_ON_WINDOW_TITLE = BerylConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.get();

    public static final Vector2ic WINDOW_POSITION = BerylConfiguration.WINDOW_POSITION.get();

    public static final Sizec WINDOW_SIZE = BerylConfiguration.WINDOW_SIZE.get();

    public static final DisplayMode WINDOW_DISPLAY_MODE = BerylConfiguration.WINDOW_DISPLAY_MODE.get();

    public static final CursorType WINDOW_CURSOR_TYPE = BerylConfiguration.WINDOW_CURSOR_TYPE.get();

    public static final boolean WINDOW_VISIBLE = BerylConfiguration.WINDOW_VISIBLE.get();

    public static final boolean WINDOW_RESIZABLE = BerylConfiguration.WINDOW_RESIZABLE.get();

    public static final boolean WINDOW_FOCUS_ON_SHOW = BerylConfiguration.WINDOW_FOCUS_ON_SHOW.get();

    public static final boolean VSYNC = BerylConfiguration.VSYNC.get();

    public static final boolean OPENGL_ENABLE_DEBUG_MESSAGES = BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.get();

    public static final boolean OPENGL_ENABLE_WARNINGS_UNIFORMS = BerylConfiguration.OPENGL_ENABLE_WARNINGS_UNIFORMS.get();

    public static final boolean PRINT_SHADERS_SOURCE = BerylConfiguration.PRINT_SHADERS_SOURCE.get();

    static void ensureLoaded() {

    }

    private static void setLWJGLConfiguration() {
        Configuration.DEBUG_STREAM.set(new LWJGLDebugStream());
        Configuration.DEBUG.set(INTERNAL_DEBUG);
        // Configuration.DEBUG_STACK.set(INTERNAL_DEBUG);
        Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(INTERNAL_DEBUG);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(INTERNAL_DEBUG);
        Configuration.DEBUG_LOADER.set(INTERNAL_DEBUG);
        Configuration.DEBUG_FUNCTIONS.set(INTERNAL_DEBUG);
        Configuration.GLFW_CHECK_THREAD0.set(INTERNAL_DEBUG);
        Configuration.DISABLE_CHECKS.set(!INTERNAL_DEBUG);
        Configuration.DISABLE_FUNCTION_CHECKS.set(!INTERNAL_DEBUG);
    }

    private static void setJOMLConfiguration() {
        System.setProperty("joml.debug", String.valueOf(INTERNAL_DEBUG));
        System.setProperty("joml.fastmath", String.valueOf(FAST_MATH));
        System.setProperty("joml.sinLookup", String.valueOf(FAST_MATH));
        System.setProperty("joml.format", String.valueOf(false));
    }

    private BerylConfigConstants() {}
}
