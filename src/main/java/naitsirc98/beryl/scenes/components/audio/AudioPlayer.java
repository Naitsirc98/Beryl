package naitsirc98.beryl.scenes.components.audio;

import naitsirc98.beryl.audio.AudioClip;
import naitsirc98.beryl.audio.AudioSource;
import naitsirc98.beryl.scenes.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public class AudioPlayer extends Component<AudioPlayer> implements Iterable<AudioClip> {

    private AudioSource audioSource;
    private List<AudioClip> clips;
    private boolean wasPlaying;

    private AudioPlayer() {

    }

    @Override
    protected void init() {
        super.init();
        audioSource = newInstance(AudioSource.class);
        clips = new ArrayList<>(1);
        wasPlaying = false;
    }

    public AudioClip clip() {
        return clips.isEmpty() ? null : clips.get(0);
    }

    public AudioClip clip(int index) {
        return clips.get(index);
    }

    public AudioPlayer clip(AudioClip clip) {
        clear();
        audioSource.buffer(clip.buffer());
        clips.add(clip);
        return this;
    }

    public AudioPlayer play() {
        audioSource.play();
        return this;
    }

    public AudioPlayer play(AudioClip clip) {
        clip(clip);
        play();
        return this;
    }

    public AudioPlayer play(AudioClip first, AudioClip... others) {
        clear();
        audioSource.queue().enqueue(first.buffer());
        clips.add(first);
        for(AudioClip clip : others) {
            audioSource.queue().enqueue(clip.buffer());
            clips.add(clip);
        }
        audioSource.play();
        return this;
    }

    public AudioPlayer pause() {
        audioSource.pause();
        return this;
    }

    public AudioPlayer stop() {
        audioSource.stop();
        return this;
    }

    public AudioPlayer clear() {
        stop();
        audioSource.queue().clear();
        clips.clear();
        return this;
    }

    public AudioSource source() {
        return audioSource;
    }

    @Override
    public Class<? extends Component> type() {
        return AudioPlayer.class;
    }

    @Override
    protected void onEnable() {
        wasPlaying = audioSource.playing();
        audioSource.pause();
    }

    @Override
    protected void onDisable() {
        if(wasPlaying) {
            audioSource.play();
            wasPlaying = false;
        }
    }

    @Override
    protected AudioPlayer self() {
        return this;
    }

    @Override
    protected void onDestroy() {
        audioSource.release();
        audioSource = null;
    }

    @Override
    public Iterator<AudioClip> iterator() {
        return clips.iterator();
    }
}
