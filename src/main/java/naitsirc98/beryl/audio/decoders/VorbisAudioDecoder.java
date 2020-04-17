package naitsirc98.beryl.audio.decoders;

import naitsirc98.beryl.audio.AudioBuffer;
import naitsirc98.beryl.audio.AudioFormat;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static naitsirc98.beryl.util.types.DataType.INT16_SIZEOF;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.libc.LibCStdlib.free;

public final class VorbisAudioDecoder implements AudioDecoder {

    @Override
    public AudioBuffer decode(String audioFile, AudioFormat format) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer channels = stack.mallocInt(1);
            IntBuffer frequency = stack.mallocInt(1);

            ShortBuffer data = stb_vorbis_decode_filename(audioFile, channels, frequency);

            if(data == null) {
                Log.error("Could not decode vorbis audio file: " + audioFile);
                return null;
            }

            AudioBuffer buffer = new AudioBuffer();
            buffer.data(memAddress(data), data.capacity() * INT16_SIZEOF, format, frequency.get(0));

            free(data);

            return buffer;
        }
    }
}
