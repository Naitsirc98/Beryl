package naitsirc98.beryl.audio;

import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.handles.IntHandle;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static naitsirc98.beryl.audio.AudioDebug.checkAudioErrors;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class AudioSource implements IntHandle, Resource {

    private int handle;
    private final AudioQueue queue;

    public AudioSource() {
        handle = alGenSources();
        checkAudioErrors();
        queue = new AudioQueue(handle);
    }

    @Override
    public int handle() {
        return handle;
    }

    public AudioQueue queue() {
        return queue;
    }

    public void play() {
        alSourcePlay(handle);
    }

    public void pause() {
        alSourcePause(handle);
    }

    public void stop() {
        alSourceStop(handle);
    }

    public void rewind() {
        alSourceRewind(handle);
    }

    public boolean initialState() {
        return alGetSourcei(handle, AL_SOURCE_STATE) == AL_INITIAL;
    }

    public boolean playing() {
        return alGetSourcei(handle, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public boolean paused() {
        return alGetSourcei(handle, AL_SOURCE_STATE) == AL_PAUSED;
    }

    public boolean stopped() {
        return alGetSourcei(handle, AL_SOURCE_STATE) == AL_STOPPED;
    }

    public boolean looping() {
        return alGetSourcei(handle, AL_LOOPING) == AL_TRUE;
    }

    public AudioSource looping(boolean looping) {
        alSourcei(handle, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
        return this;
    }

    public float seconds() {
        return alGetSourcef(handle, AL_SEC_OFFSET);
    }

    public AudioSource seconds(float seconds) {
        alSourcef(handle, AL_SEC_OFFSET, seconds);
        return this;
    }

    public float sample() {
        return alGetSourcef(handle, AL_SAMPLE_OFFSET);
    }

    public AudioSource sample(float sample) {
        alSourcef(handle, AL_SAMPLE_OFFSET, sample);
        return this;
    }

    public float offset() {
        return alGetSourcef(handle, AL_BYTE_OFFSET);
    }

    public AudioSource offset(float offset) {
        alSourcef(handle, AL_BYTE_OFFSET, offset);
        return this;
    }

    public float pitch() {
        return alGetSourcef(handle, AL_PITCH);
    }

    public AudioSource pitch(float pitch) {
        alSourcef(handle, AL_PITCH, pitch);
        return this;
    }

    public float gain() {
        return alGetSourcef(handle, AL_GAIN);
    }

    public AudioSource gain(float gain) {
        alSourcef(handle, AL_GAIN, gain);
        return this;
    }

    public float minGain() {
        return alGetSourcef(handle, AL_MIN_GAIN);
    }

    public AudioSource minGain(float minGain) {
        alSourcef(handle, AL_MIN_GAIN, minGain);
        return this;
    }

    public float maxGain() {
        return alGetSourcef(handle, AL_MAX_GAIN);
    }

    public AudioSource maxGain(float maxGain) {
        alSourcef(handle, AL_MAX_GAIN, maxGain);
        return this;
    }

    public float maxDistance() {
        return alGetSourcef(handle, AL_MAX_DISTANCE);
    }

    public AudioSource maxDistance(float maxDistance) {
        alSourcef(handle, AL_MAX_DISTANCE, maxDistance);
        return this;
    }

    public float rollOff() {
        return alGetSourcef(handle, AL_ROLLOFF_FACTOR);
    }

    public AudioSource rollOff(float rollOff) {
        alSourcef(handle, AL_ROLLOFF_FACTOR, rollOff);
        return this;
    }

    public float referenceDistance() {
        return alGetSourcef(handle, AL_REFERENCE_DISTANCE);
    }

    public AudioSource referenceDistance(float referenceDistance) {
        alSourcef(handle, AL_REFERENCE_DISTANCE, referenceDistance);
        return this;
    }

    public float coneOuterGain() {
        return alGetSourcef(handle, AL_CONE_OUTER_GAIN);
    }

    public AudioSource coneOuterGain(float coneOuterGain) {
        alSourcef(handle, AL_CONE_OUTER_GAIN, coneOuterGain);
        return this;
    }

    public float coneInnerAngle() {
        return alGetSourcef(handle, AL_CONE_INNER_ANGLE);
    }

    public AudioSource coneInnerAngle(float coneInnerAngle) {
        alSourcef(handle, AL_CONE_INNER_ANGLE, coneInnerAngle);
        return this;
    }

    public float coneOuterAngle() {
        return alGetSourcef(handle, AL_CONE_OUTER_ANGLE);
    }

    public AudioSource coneOuterAngle(float coneOuterAngle) {
        alSourcef(handle, AL_CONE_OUTER_ANGLE, coneOuterAngle);
        return this;
    }

    public Vector3fc position() {
        try(MemoryStack stack = stackPush()) {
            FloatBuffer position = stack.mallocFloat(3);
            alGetSourcefv(handle, AL_POSITION, position);
            return new Vector3f(position);
        }
    }

    public AudioSource position(Vector3fc position) {
        alSource3f(handle, AL_POSITION, position.x(), position.y(), position.z());
        return this;
    }

    public Vector3fc velocity() {
        try(MemoryStack stack = stackPush()) {
            FloatBuffer velocity = stack.mallocFloat(3);
            alGetSourcefv(handle, AL_VELOCITY, velocity);
            return new Vector3f(velocity);
        }
    }

    public AudioSource velocity(Vector3fc velocity) {
        alSource3f(handle, AL_VELOCITY, velocity.x(), velocity.y(), velocity.z());
        return this;
    }

    public Vector3fc direction() {
        try(MemoryStack stack = stackPush()) {
            FloatBuffer direction = stack.mallocFloat(3);
            alGetSourcefv(handle, AL_DIRECTION, direction);
            return new Vector3f(direction);
        }
    }

    public AudioSource direction(Vector3fc direction) {
        alSource3f(handle, AL_DIRECTION, direction.x(), direction.y(), direction.z());
        return this;
    }

    public boolean relativeToListener() {
        return alGetSourcei(handle, AL_SOURCE_RELATIVE) == AL_TRUE;
    }

    public AudioSource relativeToListener(boolean relativeToListener) {
        alSourcei(handle, AL_SOURCE_RELATIVE, relativeToListener ? AL_TRUE : AL_FALSE);
        return this;
    }

    @Override
    public void release() {
        alDeleteSources(handle);
    }
}
