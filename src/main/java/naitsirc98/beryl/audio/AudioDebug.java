package naitsirc98.beryl.audio;

import naitsirc98.beryl.logging.Log;

import static org.lwjgl.openal.AL10.*;

final class AudioDebug {

    static void checkAudioErrors() {

        int error;

        while((error = alGetError()) != AL_NO_ERROR) {
            Log.error("[OPENAL]: " + getAudioErrorName(error));
        }
    }

    static String getAudioErrorName(int errorCode) {

        switch(errorCode) {

            case AL_NO_ERROR:
                return "AL_NO_ERROR";
            case AL_INVALID_NAME:
                return "AL_INVALID_NAME";
            case AL_INVALID_ENUM:
                return "AL_INVALID_ENUM";
            case AL_INVALID_VALUE:
                return "AL_INVALID_VALUE";
            case AL_INVALID_OPERATION:
                return "AL_INVALID_OPERATION";
            case AL_OUT_OF_MEMORY:
                return "AL_OUT_OF_MEMORY";
        }

        return "Unknown OPENAL error";
    }

}
