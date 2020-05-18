package naitsirc98.beryl.audio.decoders;

import naitsirc98.beryl.audio.AudioBuffer;
import naitsirc98.beryl.audio.AudioFormat;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static naitsirc98.beryl.util.types.DataType.INT16_SIZEOF;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.libc.LibCStdlib.free;

/**
 * A simple Vorbis (.ogg) audio decoder. Audio files should be 96 Kbps.
 *
 * */
public final class VorbisAudioDecoder implements AudioDecoder {

    public VorbisAudioDecoder() {
    }

    @Override
    public AudioBuffer decode(String audioFile) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer error = stack.ints(0);
            IntBuffer channels = stack.ints(0);
            IntBuffer frequency = stack.ints(0);

            final long decoder = stb_vorbis_open_filename(audioFile, error, null);

            if(decoder == NULL) {
                Log.error("Failed to open .ogg audio file: " + audioFile + ". Error " + error.get(0));
                return null;
            }

            ShortBuffer data = stb_vorbis_decode_filename(audioFile, channels, frequency);

            if(data == null) {
                Log.error("Could not decode vorbis audio file: " + audioFile + ": " + stb_vorbis_get_error(decoder));
                return null;
            }

            AudioFormat format = AudioFormat.fromChannels(channels.get(0), INT16_SIZEOF);

            AudioBuffer buffer = new AudioBuffer();

            buffer.data(memAddress(data), data.capacity() * INT16_SIZEOF, format, frequency.get(0));

            free(data);

            return buffer;
        }
    }
}
