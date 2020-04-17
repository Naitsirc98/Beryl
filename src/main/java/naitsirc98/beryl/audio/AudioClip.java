package naitsirc98.beryl.audio;

import naitsirc98.beryl.assets.Asset;
import naitsirc98.beryl.resources.ManagedResource;

public class AudioClip extends ManagedResource implements Asset {

    private final int handle;
    private final String name;
    private final AudioFormat format;
    private final AudioBuffer buffer;

    AudioClip(int handle, String name, AudioFormat format, AudioBuffer buffer) {
        this.handle = handle;
        this.name = name;
        this.format = format;
        this.buffer = buffer;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int handle() {
        return handle;
    }

    public AudioFormat format() {
        return format;
    }

    public AudioBuffer buffer() {
        return buffer;
    }

    @Override
    protected void free() {
        buffer.release();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioClip audioClip = (AudioClip) o;
        return handle == audioClip.handle;
    }

    @Override
    public int hashCode() {
        return handle;
    }
}
