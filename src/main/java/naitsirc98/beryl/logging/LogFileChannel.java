package naitsirc98.beryl.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Objects.requireNonNull;

public class LogFileChannel implements LogChannel {

    public static final OpenOption[] DEFAULT_OPEN_OPTIONS = {
            CREATE,
            APPEND
    };

    private final BufferedWriter bufferedWriter;
    private final EnumSet<Log.Level> acceptedLevels;


    public LogFileChannel(Path path, OpenOption... openOptions) {
        this(path, EnumSet.allOf(Log.Level.class), openOptions);
    }

    public LogFileChannel(Path path, Log.Level minLevel, OpenOption... openOptions) {
        this(path, EnumSet.range(requireNonNull(minLevel), Log.Level.ERROR), openOptions);
    }

    public LogFileChannel(Path path, EnumSet<Log.Level> acceptedLevels, OpenOption... openOptions) {
        this.acceptedLevels = requireNonNull(acceptedLevels);
        bufferedWriter = createBufferedWriter(path, openOptions);
    }

    private BufferedWriter createBufferedWriter(Path path, OpenOption... openOptions) {
        try {
            return Files.newBufferedWriter(path, openOptions);
        } catch (IOException e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while creating buffered writer", e);
        }
        return null;
    }

    @Override
    public void write(String message) throws Exception {
        bufferedWriter.write(message);
    }

    @Override
    public void close() throws Exception {
        bufferedWriter.close();
    }

    @Override
    public boolean accept(Log.Level level) {
        return acceptedLevels.contains(level);
    }
}
