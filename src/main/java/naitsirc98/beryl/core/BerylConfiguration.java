package naitsirc98.beryl.core;

import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.window.CursorType;
import naitsirc98.beryl.graphics.window.DisplayMode;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.logging.LogChannel;
import naitsirc98.beryl.util.ANSIColor;
import naitsirc98.beryl.util.geometry.Sizec;
import naitsirc98.beryl.util.Version;
import org.joml.Vector2ic;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A class to set different configuration variables. The value of each configuration container will be read once and
 * its contents will be either stored in a {@code static final} variable or used at the moment.
 *
 * A value of null means default value. If another behaviour is wanted, it must be set before {@link Beryl} class loads.
 * There is a special method for it, which is {@link BerylApplication#setConfiguration}.
 *
 * This class is heavily based on {@link org.lwjgl.system.Configuration}
 *
 * */
public final class BerylConfiguration<T> {

    public static final BerylConfiguration<Boolean> DEBUG = new BerylConfiguration<>();
    public static final BerylConfiguration<Boolean> INTERNAL_DEBUG = new BerylConfiguration<>();

    public static final BerylConfiguration<Boolean> SHOW_DEBUG_INFO = new BerylConfiguration<>();

    public static final BerylConfiguration<Boolean> FAST_MATH = new BerylConfiguration<>();

    public static final BerylConfiguration<Boolean> MEMORY_USAGE_REPORT = new BerylConfiguration<>();
    public static final BerylConfiguration<Boolean> EVENTS_DEBUG_REPORT = new BerylConfiguration<>();
    public static final BerylConfiguration<Boolean> SCENES_DEBUG_REPORT = new BerylConfiguration<>();

    public static final BerylConfiguration<String> APPLICATION_NAME = new BerylConfiguration<>();
    public static final BerylConfiguration<Version> APPLICATION_VERSION = new BerylConfiguration<>();

    public static final BerylConfiguration<Double> INITIAL_TIME_VALUE = new BerylConfiguration<>();

    public static final BerylConfiguration<Set<Log.Level>> LOG_LEVELS = new BerylConfiguration<>();
    public static final BerylConfiguration<Map<Log.Level, ANSIColor>> LOG_LEVEL_COLORS = new BerylConfiguration<>();
    public static final BerylConfiguration<Collection<LogChannel>> LOG_CHANNELS = new BerylConfiguration<>();
    public static final BerylConfiguration<DateTimeFormatter> LOG_DATETIME_FORMATTER = new BerylConfiguration<>();

    public static final BerylConfiguration<Boolean> ENABLE_ASSERTS = new BerylConfiguration<>();

    public static final BerylConfiguration<GraphicsAPI> GRAPHICS_API = new BerylConfiguration<>();

    public static final BerylConfiguration<Boolean> SHOW_DEBUG_INFO_ON_WINDOW_TITLE = new BerylConfiguration<>();
    public static final BerylConfiguration<Vector2ic> WINDOW_POSITION = new BerylConfiguration<>();
    public static final BerylConfiguration<Sizec> WINDOW_SIZE = new BerylConfiguration<>();
    public static final BerylConfiguration<DisplayMode> WINDOW_DISPLAY_MODE = new BerylConfiguration<>();
    public static final BerylConfiguration<CursorType> WINDOW_CURSOR_TYPE = new BerylConfiguration<>();
    public static final BerylConfiguration<Boolean> WINDOW_VISIBLE = new BerylConfiguration<>();
    public static final BerylConfiguration<Boolean> WINDOW_RESIZABLE = new BerylConfiguration<>();
    public static final BerylConfiguration<Boolean> WINDOW_FOCUS_ON_SHOW = new BerylConfiguration<>();

    public static final BerylConfiguration<Integer> EVENT_QUEUE_INITIAL_CAPACITY = new BerylConfiguration<>();

    public static final BerylConfiguration<Boolean> VULKAN_ENABLE_DEBUG_MESSAGES = new BerylConfiguration<>();
    public static final BerylConfiguration<Set<String>> VULKAN_VALIDATION_LAYERS = new BerylConfiguration<>();
    public static final BerylConfiguration<Set<String>> VULKAN_DEVICE_EXTENSIONS = new BerylConfiguration<>();

    public static final BerylConfiguration<Boolean> OPENGL_ENABLE_DEBUG_MESSAGES = new BerylConfiguration<>();

    private T value;

    public BerylConfiguration() {
    }

    public boolean empty() {
        return value == null;
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public T get(T defaultValue) {
        if(empty()) {
            value = defaultValue;
        }
        return value;
    }

    public T get(Supplier<T> defaultValueSupplier) {
        if(empty()) {
            value = defaultValueSupplier.get();
        }
        return value;
    }

}
