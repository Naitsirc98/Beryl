package naitsirc98.beryl.core;

public final class DefaultConfigurations {

    public static Runnable debugConfiguration() {
        return () -> {
            BerylConfiguration.INTERNAL_DEBUG.set(true);
            BerylConfiguration.DEBUG.set(true);
            BerylConfiguration.SHOW_DEBUG_INFO.set(true);
            BerylConfiguration.MEMORY_USAGE_REPORT.set(true);
            BerylConfiguration.FAST_MATH.set(false);
            BerylConfiguration.ENABLE_ASSERTS.set(true);
            BerylConfiguration.SCENES_DEBUG_REPORT.set(true);
            BerylConfiguration.EVENTS_DEBUG_REPORT.set(true);
            BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.set(true);
            BerylConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.set(true);
            BerylConfiguration.VSYNC.set(false);
        };
    }

    public static Runnable developmentConfiguration() {
        return () -> {
            BerylConfiguration.INTERNAL_DEBUG.set(true);
            BerylConfiguration.DEBUG.set(true);
            BerylConfiguration.SHOW_DEBUG_INFO.set(true);
            BerylConfiguration.MEMORY_USAGE_REPORT.set(true);
            BerylConfiguration.FAST_MATH.set(true);
            BerylConfiguration.ENABLE_ASSERTS.set(true);
            BerylConfiguration.SCENES_DEBUG_REPORT.set(false);
            BerylConfiguration.EVENTS_DEBUG_REPORT.set(false);
            BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.set(true);
            BerylConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.set(true);
            BerylConfiguration.VSYNC.set(false);
        };
    }

    public static Runnable debugReleaseConfiguration() {
        return () -> {
            BerylConfiguration.INTERNAL_DEBUG.set(false);
            BerylConfiguration.DEBUG.set(false);
            BerylConfiguration.SHOW_DEBUG_INFO.set(true);
            BerylConfiguration.MEMORY_USAGE_REPORT.set(false);
            BerylConfiguration.FAST_MATH.set(false);
            BerylConfiguration.ENABLE_ASSERTS.set(false);
            BerylConfiguration.SCENES_DEBUG_REPORT.set(false);
            BerylConfiguration.EVENTS_DEBUG_REPORT.set(false);
            BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.set(true);
            BerylConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.set(true);
            BerylConfiguration.VSYNC.set(false);
        };
    }

    public static Runnable releaseConfiguration() {
        return () -> {
            BerylConfiguration.INTERNAL_DEBUG.set(false);
            BerylConfiguration.DEBUG.set(false);
            BerylConfiguration.SHOW_DEBUG_INFO.set(false);
            BerylConfiguration.MEMORY_USAGE_REPORT.set(false);
            BerylConfiguration.FAST_MATH.set(true);
            BerylConfiguration.ENABLE_ASSERTS.set(false);
            BerylConfiguration.SCENES_DEBUG_REPORT.set(false);
            BerylConfiguration.EVENTS_DEBUG_REPORT.set(false);
            BerylConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.set(false);
            BerylConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.set(false);
            BerylConfiguration.VSYNC.set(true);
        };
    }

    private DefaultConfigurations() {}
}
