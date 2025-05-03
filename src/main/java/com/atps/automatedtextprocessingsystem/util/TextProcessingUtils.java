package com.atps.automatedtextprocessingsystem.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Utility class providing helper methods for text processing,
 * error handling, and logging throughout the application.
 */
public class TextProcessingUtils {

    // Logger for application-wide logging
    private static final Logger LOGGER = Logger.getLogger("TextProcessingApp");

    // List to store recent log entries
    private static final List<LogEntry> LOG_HISTORY = new ArrayList<>();

    // Maximum number of log entries to retain in memory
    private static final int MAX_LOG_ENTRIES = 100;

    // Static initialization block to configure the logger
    static {
        try {
            // Configure the logger with custom formatter
            LOGGER.setUseParentHandlers(false);
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new LogFormatter());
            LOGGER.addHandler(handler);

            // Set the default log level
            LOGGER.setLevel(Level.INFO);
        } catch (Exception e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private TextProcessingUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Gets the application logger
     * @return The logger instance
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Logs an info message
     * @param message The message to log
     */
    public static void logInfo(String message) {
        LOGGER.info(message);
        addLogEntry(Level.INFO, message);
    }

    /**
     * Logs a warning message
     * @param message The message to log
     */
    public static void logWarning(String message) {
        LOGGER.warning(message);
        addLogEntry(Level.WARNING, message);
    }

    /**
     * Logs an error message
     * @param message The message to log
     */
    public static void logError(String message) {
        LOGGER.severe(message);
        addLogEntry(Level.SEVERE, message);
    }

    /**
     * Logs an error message with exception details
     * @param message The message to log
     * @param exception The exception that occurred
     */
    public static void logError(String message, Throwable exception) {
        LOGGER.log(Level.SEVERE, message, exception);
        addLogEntry(Level.SEVERE, message + ": " + getStackTrace(exception));
    }

    /**
     * Adds an entry to the log history
     * @param level Log level
     * @param message Log message
     */
    private static void addLogEntry(Level level, String message) {
        synchronized (LOG_HISTORY) {
            LOG_HISTORY.add(new LogEntry(LocalDateTime.now(), level, message));

            // Keep log history within size limit
            if (LOG_HISTORY.size() > MAX_LOG_ENTRIES) {
                LOG_HISTORY.removeFirst();
            }
        }
    }

    /**
     * @return List of recent log entries
     */
    public static List<LogEntry> getLogHistory() {
        synchronized (LOG_HISTORY) {
            return new ArrayList<>(LOG_HISTORY);
        }
    }

    /**
     * Converts an exception's stack trace to a string
     * @param throwable The exception
     * @return String representation of the stack trace
     */
    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Validates if a string is null or empty
     * @param str The string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Truncates a string to a maximum length
     * @param text The string to truncate
     * @param maxLength Maximum length
     * @return Truncated string with ellipsis if needed
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Gets statistics about text content
     * @param text The text to analyze
     * @return Map with statistics (word count, character count, etc.)
     */
    public static TextStatistics getTextStatistics(String text) {
        if (text == null) {
            text = "";
        }

        TextStatistics stats = new TextStatistics();
        stats.characterCount = text.length();
        stats.characterCountNoSpaces = text.replaceAll("\\s", "").length();

        // Split by whitespace to count words
        String[] words = text.split("\\s+");
        stats.wordCount = words.length;
        if (stats.wordCount == 1 && words[0].isEmpty()) {
            stats.wordCount = 0;
        }

        // Split by sentence-ending punctuation to count sentences
        String[] sentences = text.split("[.!?]+");
        stats.sentenceCount = sentences.length;
        if (stats.sentenceCount == 1 && sentences[0].isEmpty()) {
            stats.sentenceCount = 0;
        }

        // Split by paragraph separators to count paragraphs
        String[] paragraphs = text.split("\\n\\s*\\n");
        stats.paragraphCount = paragraphs.length;
        if (stats.paragraphCount == 1 && paragraphs[0].isEmpty()) {
            stats.paragraphCount = 0;
        }

        return stats;
    }

    /**
     * Custom formatter for log entries
     */
    private static class LogFormatter extends Formatter {
        private static final DateTimeFormatter DATE_FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            sb.append(LocalDateTime.now().format(DATE_FORMATTER));
            sb.append(" [").append(record.getLevel()).append("] ");
            sb.append(record.getMessage());

            if (record.getThrown() != null) {
                sb.append(System.lineSeparator());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                sb.append(sw);
            }

            sb.append(System.lineSeparator());
            return sb.toString();
        }
    }

    /**
     * Data class for log entries
     */
    public static class LogEntry {
        private final LocalDateTime timestamp;
        private final Level level;
        private final String message;

        public LogEntry(LocalDateTime timestamp, Level level, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Level getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return formatter.format(timestamp) + " [" + level + "] " + message;
        }
    }

    /**
     * Data class for text statistics
     */
    public static class TextStatistics {
        private int characterCount;
        private int characterCountNoSpaces;
        private int wordCount;
        private int sentenceCount;
        private int paragraphCount;

        public int getCharacterCount() {
            return characterCount;
        }

        public int getCharacterCountNoSpaces() {
            return characterCountNoSpaces;
        }

        public int getWordCount() {
            return wordCount;
        }

        public int getSentenceCount() {
            return sentenceCount;
        }

        public int getParagraphCount() {
            return paragraphCount;
        }

        @Override
        public String toString() {
            return "Characters: " + characterCount +
                    ", Without spaces: " + characterCountNoSpaces +
                    ", Words: " + wordCount +
                    ", Sentences: " + sentenceCount +
                    ", Paragraphs: " + paragraphCount;
        }
    }
}
