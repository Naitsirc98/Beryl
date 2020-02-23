package naitsirc98.beryl.logging;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.util.ANSIColor;
import naitsirc98.beryl.util.Singleton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

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
     * Logs the given message with the given {@link Level}
     *
     * @param level the level
     * @param msg the message
     */
    public static void log( Level level, Object msg) {
        instance.logMessage(level, msg);
    }

    /**
     * Logs the given message with the given {@link Level}
     *
     * @param level the level
     * @param msg the message
     * @param throwable the throwable
     */
    public static void log(Level level, Object msg, Throwable throwable) {
        instance.logMessage(level, withThrowable(msg, throwable));
    }

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
        instance.logMessage(Level.ERROR, withStackTrace(msg));
    }

    /**
     * Logs the given message with the {@link Level} 'ERROR'. This will print the stack trace of 'cause'
     *
     * @param msg the message
     * @param cause the {@link Throwable} cause
     */
    public static void error(Object msg, Throwable cause) {
        instance.logMessage(Level.ERROR, withThrowable(msg, cause));
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

        instance.logMessage(Level.FATAL, withStackTrace(msgString, stackTrace, 2));
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

        instance.logMessage(Level.FATAL, withThrowable(msg, cause));
        instance.terminate();

        FatalException exception = new FatalException(String.valueOf(msg), cause);
        exception.setStackTrace(cause.getStackTrace());

        throw exception;
    }

    private static String withThrowable(Object msg, Throwable throwable) {
        return msg + getThrowableString(throwable);
    }

    private static String getThrowableString(Throwable throwable) {

        StringBuilder builder = new StringBuilder();

        Throwable cause = throwable;

        while(cause != null) {
            builder.append('\n').append('\t').append("Caused by ");
            builder.append(cause.toString());
            builder.append('\n').append(getStackTrace(cause.getStackTrace(), 0));

            cause = cause.getCause();
        }

        return builder.toString();
    }

    private static String withStackTrace(Object msg) {
        return String.valueOf(msg) + '\n' + getStackTrace();
    }

    private static String withStackTrace(Object msg, StackTraceElement[] stackTrace, int startIndex) {
        return String.valueOf(msg) + '\n' + getStackTrace(stackTrace, startIndex);
    }

    private static String getStackTrace() {
        return getStackTrace(Thread.currentThread().getStackTrace(), 4);
    }

    private static String getStackTrace(StackTraceElement[] stackTrace, int startIndex) {
        StringBuilder builder = new StringBuilder();

        for(int i = startIndex;i < stackTrace.length;i++) {
            builder.append('\t').append("at ").append(stackTrace[i]).append('\n');
        }

        return builder.toString();
    }

    private final EnumSet<Level> levelMask;
    private final EnumMap<Level, ANSIColor> levelColors;
    private final List<LogChannel> channels;
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

    private void closeChannel(LogChannel channel) {
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

    private void writeMessage(LogChannel channel, String bakedMessage) {
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
        channels.addAll(BerylConfiguration.LOG_CHANNELS.get(Collections.singleton(LogChannel.stdout())));
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

    private static final class Message {

        private final Level level;
        private final Object contents;

        public Message(Level level, Object contents) {
            this.level = level;
            this.contents = contents;
        }
    }

}