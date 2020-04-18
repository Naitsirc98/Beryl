package naitsirc98.beryl.audio.decoders;

import naitsirc98.beryl.audio.AudioBuffer;
import naitsirc98.beryl.audio.AudioFormat;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class MP3AudioDecoder implements AudioDecoder {

    @Override
    public AudioBuffer decode(String audioFile) {

        try {
            File file = new File(audioFile);

            AudioInputStream in= javax.sound.sampled.AudioSystem.getAudioInputStream(file);

            javax.sound.sampled.AudioFormat baseFormat = in.getFormat();

            javax.sound.sampled.AudioFormat decodedFormat = new javax.sound.sampled.AudioFormat(
                    PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);

            AudioInputStream din = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, in);

            byte[] bytes = din.readAllBytes();

            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length);
            data.put(bytes).rewind();

            AudioFormat format = AudioFormat.fromChannels(decodedFormat.getChannels(), 2);

            AudioBuffer buffer = new AudioBuffer();

            buffer.data(memAddress(data), data.capacity(), format, (int) decodedFormat.getSampleRate());

        } catch (UnsupportedAudioFileException | IOException e) {
            Log.error("Failed to decode mp3 file: " + audioFile, e);
        }

        return null;
    }
}
