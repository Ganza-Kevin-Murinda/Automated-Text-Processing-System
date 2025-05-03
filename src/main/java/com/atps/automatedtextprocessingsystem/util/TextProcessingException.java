package com.atps.automatedtextprocessingsystem.util;

/**
 * Custom exception class for handling application-specific errors.
 * This centralizes error handling and provides consistent error reporting throughout the application.
 */
public class TextProcessingException extends Exception {

    // Error type enumeration
    public enum ErrorType {
        FILE_ERROR,      // File operations errors
        REGEX_ERROR,     // Regular expression errors
        DATA_ERROR,      // Data management errors
        INPUT_ERROR,     // User input validation errors
        PROCESSING_ERROR // General processing errors
    }

    // The type of error that occurred
    private final ErrorType errorType;

    /**
     * Constructor with an error message
     * @param message Error message
     * @param errorType Type of error
     */
    public TextProcessingException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * Constructor with error message and cause
     * @param message Error message
     * @param cause Original exception that caused this error
     * @param errorType Type of error
     */
    public TextProcessingException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }

    /**
     * @return The error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Creates a file error exception
     * @param message Error message
     * @return New exception instance
     */
    public static TextProcessingException createFileError(String message) {
        return new TextProcessingException(message, ErrorType.FILE_ERROR);
    }

    /**
     * Creates a file error exception with cause
     * @param message Error message
     * @param cause Original exception
     * @return New exception instance
     */
    public static TextProcessingException createFileError(String message, Throwable cause) {
        return new TextProcessingException(message, cause, ErrorType.FILE_ERROR);
    }

    /**
     * Creates a regex error exception
     * @param message Error message
     * @return New exception instance
     */
    public static TextProcessingException createRegexError(String message) {
        return new TextProcessingException(message, ErrorType.REGEX_ERROR);
    }

    /**
     * Creates a regex error exception with cause
     * @param message Error message
     * @param cause Original exception
     * @return New exception instance
     */
    public static TextProcessingException createRegexError(String message, Throwable cause) {
        return new TextProcessingException(message, cause, ErrorType.REGEX_ERROR);
    }

    /**
     * Creates a data error exception
     * @param message Error message
     * @return New exception instance
     */
    public static TextProcessingException createDataError(String message) {
        return new TextProcessingException(message, ErrorType.DATA_ERROR);
    }

    /**
     * Creates a data error exception with cause
     * @param message Error message
     * @param cause Original exception
     * @return New exception instance
     */
    public static TextProcessingException createDataError(String message, Throwable cause) {
        return new TextProcessingException(message, cause, ErrorType.DATA_ERROR);
    }

    /**
     * Creates an input error exception
     * @param message Error message
     * @return New exception instance
     */
    public static TextProcessingException createInputError(String message) {
        return new TextProcessingException(message, ErrorType.INPUT_ERROR);
    }

    /**
     * Creates a processing error exception
     * @param message Error message
     * @return New exception instance
     */
    public static TextProcessingException createProcessingError(String message) {
        return new TextProcessingException(message, ErrorType.PROCESSING_ERROR);
    }

    /**
     * Creates a processing error exception with cause
     * @param message Error message
     * @param cause Original exception
     * @return New exception instance
     */
    public static TextProcessingException createProcessingError(String message, Throwable cause) {
        return new TextProcessingException(message, cause, ErrorType.PROCESSING_ERROR);
    }
}
