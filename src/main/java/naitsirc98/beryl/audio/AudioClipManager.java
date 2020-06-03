package naitsirc98.beryl.audio;

import naitsirc98.beryl.assets.AssetManager;
import naitsirc98.beryl.audio.decoders.AudioDecoder;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.types.Singleton;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class AudioClipManager implements AssetManager<AudioClip> {

    @Singleton
    private static AudioClipManager instance;

    public static AudioClipManager get() {
        return instance;
    }

    private final Map<String, AudioClip> audioClips;
    private final AtomicInteger handleProvider;

    private AudioClipManager() {
        this.audioClips = new ConcurrentHashMap<>();
        this.handleProvider = new AtomicInteger();
    }

    @Override
    public void init() {

    }

    public synchronized AudioClip createAudioClip(String name, Path audioFile, AudioDataFormat dataFormat) {

        if(invalidAudioClipParameters(name, audioFile, dataFormat)) {
            return null;
        }

        AudioClip audioClip = new AudioClip(handleProvider.getAndIncrement(),
                name, decodeAudioData(audioFile, dataFormat));

        audioClips.put(audioClip.name(), audioClip);

        return audioClip;
    }

    @Override
    public int count() {
        return audioClips.size();
    }

    @Override
    public boolean exists(String assetName) {
        return audioClips.containsKey(assetName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K extends AudioClip> K get(String assetName) {
        return (K) audioClips.get(assetName);
    }

    @Override
    public void destroy(AudioClip asset) {
        if(audioClips.containsValue(asset)) {
            asset.release();
            audioClips.remove(asset.name());
        }
    }

    @Override
    public void destroyAll() {
        audioClips.values().forEach(AudioClip::release);
        audioClips.clear();
    }

    @Override
    public void terminate() {
        destroyAll();
    }

    private boolean invalidAudioClipParameters(String name, Path audioFile, AudioDataFormat dataFormat) {

        if(name == null) {
            Log.error("Name cannot be null");
            return true;
        }

        if(exists(name)) {
            Log.error("There is already an Audio Clip called " + name);
            return true;
        }

        if(audioFile == null) {
            Log.error("Audio file cannot be null");
            return true;
        }

        if(Files.notExists(audioFile)) {
            Log.error("File " + audioFile + "does not exists");
            return true;
        }

        if(dataFormat == null) {
            Log.error("Audio data format cannot be null");
            return true;
        }

        return false;
    }

    private String getFileName(String audioFile) {
        return audioFile.substring(audioFile.lastIndexOf(File.pathSeparatorChar) + 1);
    }

    private AudioBuffer decodeAudioData(Path audioFile, AudioDataFormat dataFormat) {
        return AudioDecoder.decode(audioFile, dataFormat);
    }

}
