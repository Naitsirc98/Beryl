package naitsirc98.beryl.audio;

import naitsirc98.beryl.core.BerylConfiguration;
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
    private final Vector3f forward;
    private final Vector3f up;

    AudioListener() {
        position = new Vector3f();
        velocity = new Vector3f();
        forward = new Vector3f();
        up = new Vector3f();
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

    public Vector3fc forward() {
        return forward;
    }

    public Vector3fc up() {
        return up;
    }

    public AudioListener orientation(Vector3fc forward, Vector3fc up) {
        return orientation(forward.x(), forward.y(), forward.z(), up.x(), up.y(), up.z());
    }

    private AudioListener orientation(float forwardX, float forwardY, float forwardZ, float upX, float upY, float upZ) {
        try(MemoryStack stack = stackPush()) {
            alListenerfv(AL_ORIENTATION, stack.floats(forwardX, forwardY, forwardZ, upX, upY, upZ));
        }
        this.forward.set(forwardX, forwardY, forwardZ);
        this.up.set(upX, upY, upZ);
        return this;
    }

    void update() {
        try(MemoryStack stack = stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(6);
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
        forward.set(buffer);
        up.set(3, buffer);
    }
}
