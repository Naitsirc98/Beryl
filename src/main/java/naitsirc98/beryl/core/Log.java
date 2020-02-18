package naitsirc98.beryl.core;

import naitsirc98.beryl.util.Singleton;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Utility Logger class for both internal and client side
 * */
public final class Log extends BerylSystem {

    private static final int MSG_QUEUE_TERMINATION_WAIT_TIME = Integer.MAX_VALUE;
    private static final int MSG_QUEUE_POLL_WAIT_TIME = 1000 / 60;

    private static final String PATTERN = "%s%s[%s]: %s\n%s";

    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");

    @Singleton
    private static Log instance;

    /**
     * Logs the given message with the {@link Level} 'TRACE'
     *
     * @param msg the message
     */
    public static void trace(Object msg) {
        instance.logMessage(Level.TRACE, msg);
    }

    /**
     * Logs the given message with the {@link Level} 'INFO'
     *
     * @param msg the message
     */
    public static void info(Object msg) {
        instance.logMessage(Level.INFO, msg);
    }

    /**
     * Logs the given message with the {@link Level} 'DEBUG'
     *
     * @param msg the message
     */
    public static void debug(Object msg) {
        instance.logMessage(Level.DEBUG, msg);
    }

    /**
     * Logs the given message with the {@link Level} 'WARNING'
     *
     * @param msg the message
     */
    public static void warning(Object msg) {
        instance.logMessage(Level.WARNING, msg);
    }

    /**
     * Logs the given message with the {@link Level} 'ERROR'
     *
     * @param msg the message
     */
    public static void error(Object msg) {
        instance.logMessage(Level.ERROR, withStackTrace(msg, getStackTrace()));
    }

    /**
     * Logs the given message with the {@link Level} 'ERROR'. This will print the stack trace of 'cause'
     *
     * @param msg the message
     * @param cause the {@link Throwable} cause
     */
    public static void error(Object msg, Throwable cause) {
        instance.logMessage(Level.ERROR, withStackTrace(msg, getStackTrace(cause.getStackTrace(), 0)));
    }

    /**
     * Logs the given message with the {@link Level} 'FATAL'.
     *
     * A FATAL error indicates that the application should stop execution. It will log the message and then will
     * throw an exception
     *
     * @param msg the msg
     */
    public static void fatal(Object msg) {
        String msgString = String.valueOf(msg);

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        FatalException exception = new FatalException(msgString);
        exception.setStackTrace(Arrays.copyOfRange(stackTrace, 2, stackTrace.length));

        instance.logMessage(Level.FATAL, withStackTrace(msgString, getStackTrace(stackTrace, 2)));
        instance.terminate();

        throw exception;
    }

    /**
     * Logs the given message with the {@link Level} 'FATAL'.
     *
     * A FATAL error indicates that the application should stop execution. It will log the message and then will
     * throw an exception
     *
     * @param msg   the msg
     * @param cause the {@link Throwable} cause
     */
    public static void fatal(Object msg, Throwable cause) {
        instance.logMessage(Level.FATAL, msg);
        instance.terminate();

        FatalException exception = new FatalException(String.valueOf(msg), cause);
        exception.setStackTrace(cause.getStackTrace());

        throw exception;
    }


    private static String withStackTrace(Object msg, String stackTrace) {
        return String.valueOf(msg) + '\n' + stackTrace;
    }

    private static String getStackTrace() {
        return getStackTrace(Thread.currentThread().getStackTrace(), 3);
    }

    private static String getStackTrace(StackTraceElement[] stackTrace, int startIndex) {
        StringBuilder builder = new StringBuilder("\tAt ");

        for(int i = startIndex;i < stackTrace.length;i++) {
            builder.append(stackTrace[i]).append('\n').append('\t');
        }

        return builder.toString();
    }

    private final EnumSet<Level> levelMask;
    private final EnumMap<Level, ANSIColor> levelColors;
    private final List<Channel> channels;
    private final BlockingQueue<Message> messageQueue;
    private final AtomicBoolean running;
    private final ExecutorService executor;
    private DateTimeFormatter dateTimeFormatter;

    private Log() {
        levelMask = Beryl.DEBUG ? EnumSet.allOf(Level.class) : EnumSet.of(Level.WARNING, Level.ERROR, Level.FATAL);
        levelColors = new EnumMap<>(Level.class);
        channels = new ArrayList<>(1);
        messageQueue = new LinkedBlockingDeque<>();
        running = new AtomicBoolean(false);
        executor = newSingleThreadExecutor();
    }

    @Override
    protected void init() {
        setLevelMask();
        setLevelColors();
        setChannels();
        setDateTimeFormatter();
        running.set(true);
        executor.execute(this::run);
    }

    @Override
    protected void terminate() {
        running.set(false);
        executor.shutdown();
        try {
            executor.awaitTermination(MSG_QUEUE_TERMINATION_WAIT_TIME, SECONDS);
        } catch (InterruptedException e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while terminating logging system", e);
        } finally {
            channels.forEach(this::closeChannel);
        }
    }

    private void closeChannel(Channel channel) {
        try {
            channel.close();
        } catch (Exception e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while closing channel", e);
        }
    }

    private void run() {
        while(running.get()) {
            while(!messageQueue.isEmpty()) {
                popMessage().ifPresent(this::log);
            }
        }
    }

    private Optional<Message> popMessage() {
        try {
            return Optional.ofNullable(messageQueue.poll(MSG_QUEUE_POLL_WAIT_TIME, MILLISECONDS));
        } catch (InterruptedException e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while popping message", e);
        }
        return Optional.empty();
    }

    private void logMessage(Level level, Object msg) {
        if(levelMask.contains(level)) {
            messageQueue.add(new Message(level, msg));
        }
    }

    private void log(Message message) {
        final String bakedMessage = bakeMessage(message.level, message.contents);
        channels.parallelStream()
                .filter(channel -> channel.accept(message.level))
                .forEach(channel -> writeMessage(channel, bakedMessage));
    }

    private void writeMessage(Channel channel, String bakedMessage) {
        try {
            channel.write(bakedMessage);
        } catch (Exception e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while writing to channel", e);
        }
    }

    private String bakeMessage(Level level, Object msg) {
        return String.format(PATTERN, colorOf(level), timestamp(), level.name(), msg, ANSIColor.RESET.code);
    }

    private String colorOf(Level level) {
        final ANSIColor ansiColor = levelColors.get(level);
        return ansiColor == null ? ANSIColor.NONE.code : ansiColor.code;
    }

    private String timestamp() {
        final String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        return timestamp.isEmpty() ? "" : '[' + timestamp + ']';
    }

    private void setDateTimeFormatter() {
        dateTimeFormatter = BerylConfiguration.LOG_DATETIME_FORMATTER.get(DEFAULT_DATETIME_FORMATTER);
    }

    private void setChannels() {
        channels.addAll(BerylConfiguration.LOG_CHANNELS.get(Collections.singleton(Channel.stdout())));
    }

    private void setLevelColors() {
        if(BerylConfiguration.LOG_LEVEL_COLORS.empty()) {
            setDefaultLevelColors();
        } else {
            levelColors.putAll(BerylConfiguration.LOG_LEVEL_COLORS.get());
        }
    }

    private void setDefaultLevelColors() {
        levelColors.put(Level.TRACE, ANSIColor.NONE);
        levelColors.put(Level.INFO, ANSIColor.BLUE);
        levelColors.put(Level.DEBUG, ANSIColor.GREEN);
        levelColors.put(Level.WARNING, ANSIColor.YELLOW);
        levelColors.put(Level.ERROR, ANSIColor.RED);
        levelColors.put(Level.FATAL, ANSIColor.RED_BOLD);
    }

    private void setLevelMask() {
        if(!BerylConfiguration.LOG_LEVELS.empty()) {
            levelMask.clear();
            levelMask.addAll(BerylConfiguration.LOG_LEVELS.get());
        }
    }

    public enum Level {

        TRACE,
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        FATAL;

        public static int compare(Level level1, Level level2) {
            return requireNonNull(level1).compareTo(requireNonNull(level2));
        }

    }

    public enum ANSIColor {

        NONE(""),
        RESET("\u001b[0m"),
        CYAN("\u001b[36m"),
        BLUE("\u001b[34m"),
        GREEN("\u001b[32m"),
        YELLOW("\u001b[33m"),
        MAGENTA("\u001b[35m"),
        RED("\u001b[31m"),
        RESET_BOLD("\u001b[0;1m"),
        CYAN_BOLD("\u001b[36;1m"),
        BLUE_BOLD("\u001b[34;1m"),
        GREEN_BOLD("\u001b[32;1m"),
        YELLOW_BOLD("\u001b[33;1m"),
        MAGENTA_BOLD("\u001b[35;1m"),
        RED_BOLD("\u001b[31;1m");

        public final String code;

        ANSIColor(String code) {
            this.code = code;
        }
    }

    @FunctionalInterface
    public interface Channel {

        static Channel stdout() {
            return of(System.out);
        }

        static Channel of(OutputStream outputStream) {
            return msg -> outputStream.write(msg.getBytes(UTF_8));
        }

        static Channel of(PrintStream printStream) {
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
        default boolean accept(Level level) {
            return true;
        }
    }

    public static class FileChannel implements Channel {

        public static final OpenOption[] DEFAULT_OPEN_OPTIONS = {
                CREATE,
                APPEND
        };

        private final BufferedWriter bufferedWriter;
        private final EnumSet<Level> acceptedLevels;


        public FileChannel(Path path, OpenOption... openOptions) {
            this(path, EnumSet.allOf(Level.class), openOptions);
        }

        public FileChannel(Path path, Level minLevel, OpenOption... openOptions) {
            this(path, EnumSet.range(requireNonNull(minLevel), Level.ERROR), openOptions);
        }

        public FileChannel(Path path, EnumSet<Level> acceptedLevels, OpenOption... openOptions) {
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
        public boolean accept(Level level) {
            return acceptedLevels.contains(level);
        }
    }

    private static final class Message {

        private final Level level;
        private final Object contents;

        public Message(Level level, Object contents) {
            this.level = level;
            this.contents = contents;
        }
    }

    private static class FatalException extends RuntimeException {

        public FatalException(String msg) {
            super(msg);
        }

        public FatalException(Throwable cause) {
            super(cause);
        }

        public FatalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
