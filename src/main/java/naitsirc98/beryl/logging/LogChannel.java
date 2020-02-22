package naitsirc98.beryl.logging;

import java.io.OutputStream;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@FunctionalInterface
public interface LogChannel {

    static LogChannel stdout() {
        return of(System.out);
    }

    static LogChannel of(OutputStream outputStream) {
        return msg -> outputStream.write(msg.getBytes(UTF_8));
    }

    static LogChannel of(PrintStream printStream) {
        return printStream::print;
    }

    void write(String message) throws Exception;

    /**
     * Override this if the underlying channel must be closed
     *
     */
    default void close() throws Exception {}

    /**
     * Tells whether this channel may act on the specified level or not
     *
     */
    default boolean accept(Log.Level level) {
        return true;
    }
}
