package naitsirc98.beryl.audio;

import naitsirc98.beryl.logging.Log;

public enum AudioFormat {
    MONO8,
    MONO16,
    STEREO8,
    STEREO16;

    public static AudioFormat fromChannels(int channels, int bytes) {
        if(channels == 1 && bytes == 1) {
            return MONO8;
        }
        if(channels == 1 && bytes == 2) {
            return MONO16;
        }

        if(channels == 2 && bytes == 1) {
            return STEREO8;
        }
        if(channels == 2 && bytes == 2) {
            return STEREO16;
        }

        Log.error("Unknown channels-bytes combination audio format");
        return null;
    }
}
