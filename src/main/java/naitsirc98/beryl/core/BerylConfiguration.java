package naitsirc98.beryl.core;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class BerylConfiguration<T> {

    public static final BerylConfiguration<Boolean> DEBUG = new BerylConfiguration<>();
    public static final BerylConfiguration<Boolean> INTERNAL_DEBUG = new BerylConfiguration<>();

    public static final BerylConfiguration<Double> INITIAL_TIME_VALUE = new BerylConfiguration<>();

    public static final BerylConfiguration<Set<Log.Level>> LOG_LEVELS = new BerylConfiguration<>();
    public static final BerylConfiguration<Map<Log.Level, Log.ANSIColor>> LOG_LEVEL_COLORS = new BerylConfiguration<>();
    public static final BerylConfiguration<Collection<Log.Channel>> LOG_CHANNELS = new BerylConfiguration<>();
    public static final BerylConfiguration<DateTimeFormatter> LOG_DATETIME_FORMATTER = new BerylConfiguration<>();

    public static final BerylConfiguration<Boolean> ENABLE_ASSERTS = new BerylConfiguration<>();

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

}
