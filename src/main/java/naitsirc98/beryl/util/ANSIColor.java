package naitsirc98.beryl.util;

public enum ANSIColor {

    NONE(""),
    RESET("\u001b[0m"),
    CYAN("\u001b[36m"),
    BLUE("\u001b[34m"),
    GREEN("\u001b[32m"),
    YELLOW("\u001b[33m"),
    MAGENTA("\u001b[35m"),
    RED("\u001b[31m"),
    RESET_BOLD("\u001b[0;1m"),
    CYAN_BOLD("\u001b[36;1m"),
    BLUE_BOLD("\u001b[34;1m"),
    GREEN_BOLD("\u001b[32;1m"),
    YELLOW_BOLD("\u001b[33;1m"),
    MAGENTA_BOLD("\u001b[35;1m"),
    RED_BOLD("\u001b[31;1m");

    public final String code;

    ANSIColor(String code) {
        this.code = code;
    }
}
