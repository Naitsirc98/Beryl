package naitsirc98.beryl.audio.decoders;

import naitsirc98.beryl.audio.AudioBuffer;
import naitsirc98.beryl.audio.AudioDataFormat;
import naitsirc98.beryl.audio.AudioFormat;
import naitsirc98.beryl.logging.Log;

public interface AudioDecoder {

    static AudioBuffer decode(String audioFile, AudioFormat format, AudioDataFormat dataFormat) {

        switch(dataFormat) {
            case WAV:
                break;
            case OGG:
                return new VorbisAudioDecoder().decode(audioFile, format);
        }

        Log.error("Unsupported audio data format: " + dataFormat);
        return null;
    }

    AudioBuffer decode(String audioFile, AudioFormat format);

}
