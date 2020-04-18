package naitsirc98.beryl.audio.decoders;

import naitsirc98.beryl.audio.AudioBuffer;
import naitsirc98.beryl.audio.AudioDataFormat;
import naitsirc98.beryl.logging.Log;

public interface AudioDecoder {

    static AudioBuffer decode(String audioFile, AudioDataFormat dataFormat) {

        switch(dataFormat) {
            case WAV:
                break;
            case OGG:
                return new VorbisAudioDecoder().decode(audioFile);
            case MP3:
                return new MP3AudioDecoder().decode(audioFile);
        }

        Log.error("Unsupported audio data format: " + dataFormat);
        return null;
    }

    AudioBuffer decode(String audioFile);

}
