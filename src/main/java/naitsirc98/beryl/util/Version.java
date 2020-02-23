package naitsirc98.beryl.util;

public final class Version {

    private final int major;
    private final int minor;
    private final int revision;

    public Version(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int revision() {
        return revision;
    }

}
