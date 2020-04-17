package naitsirc98.beryl.audio;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.handles.LongHandle;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.audio.AudioDebug.checkAudioErrors;
import static org.lwjgl.openal.ALC10.*;

public final class AudioContext implements LongHandle, Resource {

    static AudioContext instance;

    public static AudioContext get() {
        return instance;
    }

    private long handle;
    private AudioDevice device;
    private ALCCapabilities capabilities;

    public AudioContext(AudioDevice device) {
        this.device = requireNonNull(device);
        createOpenALContext();
        makeCurrent();
        capabilities = ALC.createCapabilities(handle);
        AL.createCapabilities(capabilities);
        checkAudioErrors();
    }

    public void makeCurrent() {
        alcMakeContextCurrent(handle);
    }

    public boolean isCurrent() {
        return alcGetCurrentContext() == handle;
    }

    @Override
    public long handle() {
        return handle;
    }

    public ALCCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public void release() {
        handle = NULL;
        device = null;
    }

    private void createOpenALContext() {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer attributes = stack.ints(0);

            handle = alcCreateContext(device.handle(), attributes);

            if(handle == NULL) {
                Log.fatal("Failed to create OpenAL context");
            }
        }
    }
}
