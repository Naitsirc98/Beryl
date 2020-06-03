package naitsirc98.beryl.audio.decoders;

import naitsirc98.beryl.audio.AudioBuffer;
import naitsirc98.beryl.audio.AudioDataFormat;
import naitsirc98.beryl.logging.Log;

import java.nio.file.Path;

public interface AudioDecoder {

    static AudioBuffer decode(Path audioFile, AudioDataFormat dataFormat) {

        // Only supporting .ogg for now
        if (dataFormat == AudioDataFormat.OGG) {
            return new VorbisAudioDecoder().decode(audioFile);
        }

        Log.error("Unsupported audio data format: " + dataFormat);

        return null;
    }

    AudioBuffer decode(Path audioFile);

}
