package naitsirc98.beryl.util;

import naitsirc98.beryl.logging.Log;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public class FileUtils {

    public static ByteBuffer readAllBytes(Path path) {

        ByteBuffer buffer = null;

        try {

            final long size = Files.size(path);

            buffer = memAlloc((int) size);

            try(FileChannel channel = FileChannel.open(path)) {
                while(channel.read(buffer) > 0);
            }

            buffer.rewind();

        } catch (Exception e) {
            Log.error("Failed to read file contents: " + path, e);
        }

        return buffer;
    }

    public static String getFileExtension(Path path) {
        String filename = path.toString();
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
