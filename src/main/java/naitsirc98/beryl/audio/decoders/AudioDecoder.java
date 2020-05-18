package naitsirc98.beryl.audio.decoders;

import naitsirc98.beryl.audio.AudioBuffer;
import naitsirc98.beryl.audio.AudioDataFormat;
import naitsirc98.beryl.logging.Log;

public interface AudioDecoder {

    static AudioBuffer decode(String audioFile, AudioDataFormat dataFormat) {

        // Only supporting .ogg for now
        if (dataFormat == AudioDataFormat.OGG) {
            return new VorbisAudioDecoder().decode(audioFile);
        }

        Log.error("Unsupported audio data format: " + dataFormat);

        return null;
    }

    AudioBuffer decode(String audioFile);

}
