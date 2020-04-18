package naitsirc98.beryl.audio;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.handles.IntHandle;

import static naitsirc98.beryl.audio.AudioDebug.checkAudioErrors;
import static naitsirc98.beryl.core.Beryl.DEBUG;
import static org.lwjgl.openal.AL10.*;

public class AudioBuffer implements IntHandle, Resource {

    private int handle;
    private AudioFormat format;

    public AudioBuffer() {

        this.handle = alGenBuffers();

        if(handle == NULL) {
            checkAudioErrors();
            Log.fatal("Could not create OPENAL buffer");
        }
    }

    @Override
    public int handle() {
        return handle;
    }

    public int frequency() {
        return alGetBufferi(handle, AL_FREQUENCY);
    }

    public int bits() {
        return alGetBufferi(handle, AL_BITS);
    }

    public int channels() {
        return alGetBufferi(handle, AL_CHANNELS);
    }

    public int size() {
        return alGetBufferi(handle, AL_SIZE);
    }

    public AudioFormat format() {
        return format;
    }

    public void data(long audioDataPtr, int size, AudioFormat format, int frequency) {
        nalBufferData(handle, asOpenALFormat(format), audioDataPtr, size, frequency);
        if(DEBUG) {
            checkAudioErrors();
        }
        this.format = format;
    }

    @Override
    public void release() {
        alDeleteBuffers(handle);
        handle = NULL;
        format = null;
    }

    private int asOpenALFormat(AudioFormat format) {

        switch(format) {

            case MONO8:
                return AL_FORMAT_MONO8;
            case MONO16:
                return AL_FORMAT_MONO16;
            case STEREO8:
                return AL_FORMAT_STEREO8;
            case STEREO16:
                return AL_FORMAT_STEREO16;
        }

        Log.fatal("Unknown audio format " + format);

        return -1;
    }
}
