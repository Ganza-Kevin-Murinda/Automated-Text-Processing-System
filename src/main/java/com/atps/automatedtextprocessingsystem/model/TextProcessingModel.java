package com.atps.automatedtextprocessingsystem.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Core model class that manages text content and processing operations.
 * This class handles the main text data and provides methods for text manipulation.
 */
public class TextProcessingModel {

    // The current text content being processed
    private String currentText;

    // Store operation history for undo/redo functionality
    private List<String> textHistory;

    // Current position in the history list for undo/redo operations
    private int historyPosition;

    // Maximum number of history entries to maintain
    private static final int MAX_HISTORY_SIZE = 20;

    /**
     * Constructor initializes an empty text processor
     */
    public TextProcessingModel() {
        this.currentText = "";
        this.textHistory = new ArrayList<>();
        this.textHistory.add(""); // Add initial empty state
        this.historyPosition = 0;
    }

    /**
     * Constructor with initial text content
     * @param initialText The initial text to process
     */
    public TextProcessingModel(String initialText) {
        this.currentText = initialText != null ? initialText : "";
        this.textHistory = new ArrayList<>();
        this.textHistory.add(this.currentText); // Add initial state
        this.historyPosition = 0;
    }

    /**
     * Updates the current text and saves it to history
     * @param newText The new text content
     */
    public void setText(String newText) {
        if (newText == null) {
            newText = "";
        }

        // Add to history only if a text has changed
        if (!newText.equals(currentText)) {
            // if we are not already at the most recent change (after undo operations), remove all future history entries
            if (historyPosition < textHistory.size() - 1) {
                textHistory = new ArrayList<>(textHistory.subList(0, historyPosition + 1));
            }

            // Add new text to history
            textHistory.add(newText);

            // Remove the oldest history if exceeding max size
            if (textHistory.size() > MAX_HISTORY_SIZE) {
                textHistory.removeFirst();
            }

            historyPosition = textHistory.size() - 1;
        }

        currentText = newText;
    }

    /**
     * @return The current text content
     */
    public String getText() {
        return currentText;
    }

    /**
     * Finds all matches of a regular expression pattern in the current text
     * @param regexPattern The regex pattern to match
     * @return List of matched strings
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public List<String> findMatches(String regexPattern) throws PatternSyntaxException {
        List<String> matches = new ArrayList<>();

        if (currentText.isEmpty() || regexPattern == null || regexPattern.isEmpty()) {
            return matches;
        }

        // Compile the pattern and create matcher
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(currentText);

        // Find all matches
        while (matcher.find()) {
            matches.add(matcher.group());
        }

        return matches;
    }

    /**
     * Replaces text matching a pattern with replacement text
     * @param regexPattern The pattern to match
     * @param replacement The replacement string
     * @return The number of replacements made
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public int replaceText(String regexPattern, String replacement) throws PatternSyntaxException {
        if (currentText.isEmpty() || regexPattern == null || regexPattern.isEmpty()) {
            return 0;
        }

        if (replacement == null) {
            replacement = "";
        }

        // Compile the pattern
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(currentText);

        // Count matches before replacing (to return count)
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        // If matches found, perform replacement and update text
        if (count > 0) {
            String newText = matcher.replaceAll(replacement);
            setText(newText);
        }

        return count;
    }

    /**
     * Undo the last text change operation
     * @return true if undo was successful, false if no history available
     */
    public boolean undo() {
        if (historyPosition > 0) {
            historyPosition--;
            currentText = textHistory.get(historyPosition);
            return true;
        }
        return false;
    }

    /**
     * Redo a previously undone text change operation
     * @return true if redo was successful, false if no future history available
     */
    public boolean redo() {
        if (historyPosition < textHistory.size() - 1) {
            historyPosition++;
            currentText = textHistory.get(historyPosition);
            return true;
        }
        return false;
    }

    /**
     * Clears the current text
     */
    public void clearText() {
        setText("");
    }
}
