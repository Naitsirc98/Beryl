package naitsirc98.beryl.audio;

import naitsirc98.beryl.assets.Asset;
import naitsirc98.beryl.resources.ManagedResource;

import java.util.function.Consumer;

public class AudioClip extends ManagedResource implements Asset {

    public static AudioClip get(String name) {

        AudioClipManager manager = AudioClipManager.get();

        if(manager.exists(name)) {
            return manager.get(name);
        }

        return null;
    }

    public static AudioClip get(String name, Consumer<AudioClipParams> audioClipParamsConsumer) {

        AudioClipManager manager = AudioClipManager.get();

        if(manager.exists(name)) {
            return manager.get(name);
        }

        AudioClipParams params = new AudioClipParams();
        audioClipParamsConsumer.accept(params);

        return manager.createAudioClip(name, params.audioFile, params.dataFormat);
    }

    private final int handle;
    private final String name;
    private final AudioFormat format;
    private final AudioBuffer buffer;

    AudioClip(int handle, String name, AudioBuffer buffer) {
        this.handle = handle;
        this.name = name;
        this.format = buffer.format();
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

    public static final class AudioClipParams {

        private String audioFile;
        private AudioDataFormat dataFormat;

        public AudioClipParams() {
            dataFormat = AudioDataFormat.OGG;
        }

        public AudioClipParams audioFile(String audioFile) {
            this.audioFile = audioFile;
            return this;
        }

        public AudioClipParams dataFormat(AudioDataFormat dataFormat) {
            this.dataFormat = dataFormat;
            return this;
        }
    }
}
