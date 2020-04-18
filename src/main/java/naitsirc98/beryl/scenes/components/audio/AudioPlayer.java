package naitsirc98.beryl.scenes.components.audio;

import naitsirc98.beryl.audio.AudioClip;
import naitsirc98.beryl.audio.AudioSource;
import naitsirc98.beryl.scenes.Component;

import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public class AudioPlayer extends Component<AudioPlayer> {

    private AudioSource audioSource;
    private boolean wasPlaying;

    private AudioPlayer() {

    }

    @Override
    protected void init() {
        super.init();
        audioSource = newInstance(AudioSource.class);
        wasPlaying = false;
    }

    public AudioPlayer play() {
        audioSource.play();
        return this;
    }

    public AudioPlayer play(AudioClip clip) {
        stop();
        audioSource.buffer(clip.buffer());
        audioSource.play();
        return this;
    }

    public AudioPlayer play(AudioClip first, AudioClip... others) {
        stop();
        audioSource.queue().clear();
        audioSource.queue().enqueue(first.buffer());
        for(AudioClip clip : others) {
            audioSource.queue().enqueue(clip.buffer());
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
}
