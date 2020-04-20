package naitsirc98.beryl.animations;

public class Animation {

    private final String name;
    private final KeyFrame[] frames;
    private final float duration;

    public Animation(String name, KeyFrame[] frames, float duration) {
        this.name = name;
        this.frames = frames;
        this.duration = duration;
    }

    public String name() {
        return name;
    }

    public float duration() {
        return duration;
    }

    public int frameCount() {
        return frames.length;
    }

    public KeyFrame frame(int index) {
        return frames[index];
    }

    public KeyFrame[] frames() {
        return frames;
    }
}
