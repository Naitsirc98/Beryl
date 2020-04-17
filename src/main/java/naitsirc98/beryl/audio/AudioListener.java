package naitsirc98.beryl.audio;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class AudioListener {

    static AudioListener instance;

    public static AudioListener get() {
        return instance;
    }

    private final Vector3f position;
    private final Vector3f velocity;
    private final Vector3f orientation;

    AudioListener() {
        position = new Vector3f();
        velocity = new Vector3f();
        orientation = new Vector3f();
    }

    public float gain() {
        return alGetListenerf(AL_GAIN);
    }

    public AudioListener gain(float gain) {
        alListenerf(AL_GAIN, gain);
        return this;
    }

    public Vector3fc position() {
        return position;
    }

    public AudioListener position(Vector3fc position) {
        return position(position.x(), position.y(), position.z());
    }

    private AudioListener position(float x, float y, float z) {
        alListener3f(AL_POSITION, x, y, z);
        this.position.set(x, y, z);
        return this;
    }

    public Vector3fc velocity() {
        return velocity;
    }

    public AudioListener velocity(Vector3fc velocity) {
        return velocity(velocity.x(), velocity.y(), velocity.z());
    }

    private AudioListener velocity(float x, float y, float z) {
        alListener3f(AL_VELOCITY, x, y, z);
        this.velocity.set(x, y, z);
        return this;
    }

    public Vector3fc orientation() {
        return orientation;
    }

    public AudioListener orientation(Vector3fc orientation) {
        return velocity(orientation.x(), orientation.y(), orientation.z());
    }

    private AudioListener orientation(float x, float y, float z) {
        alListener3f(AL_ORIENTATION, x, y, z);
        this.orientation.set(x, y, z);
        return this;
    }

    void update() {
        try(MemoryStack stack = stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            cachePosition(buffer);
            cacheVelocity(buffer);
            cacheOrientation(buffer);
        }
    }

    private void cachePosition(FloatBuffer buffer) {
        alGetListenerfv(AL_POSITION, buffer);
        position.set(buffer);
    }

    private void cacheVelocity(FloatBuffer buffer) {
        alGetListenerfv(AL_VELOCITY, buffer);
        velocity.set(buffer);
    }

    private void cacheOrientation(FloatBuffer buffer) {
        alGetListenerfv(AL_ORIENTATION, buffer);
        orientation.set(buffer);
    }
}
