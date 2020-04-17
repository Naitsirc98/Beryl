package naitsirc98.beryl.audio;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.handles.LongHandle;

import static naitsirc98.beryl.audio.AudioDebug.checkAudioErrors;
import static org.lwjgl.openal.ALC10.*;

public final class AudioDevice implements LongHandle, Resource {

    static AudioDevice instance;

    public static AudioDevice get() {
        return instance;
    }


    private final String name;
    private long handle;

    AudioDevice(String name) {

        this.name = name;

        handle = alcOpenDevice(name);

        if(handle == NULL) {
            checkAudioErrors();
            Log.fatal("Failed to open Audio Device " + name);
        }
    }

    public String name() {
        return name;
    }

    @Override
    public long handle() {
        return handle;
    }

    @Override
    public void release() {
        alcCloseDevice(handle);
        handle = NULL;
    }
}
