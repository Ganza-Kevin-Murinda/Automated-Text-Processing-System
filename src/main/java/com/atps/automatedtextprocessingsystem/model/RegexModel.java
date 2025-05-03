package com.atps.automatedtextprocessingsystem.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Model class that specializes in regex operations.
 * Handles pattern compilation, validation, and provides detailed regex functionality.
 */
public class RegexModel {

    // Store recently used patterns for quick access
    private final List<String> recentPatterns;

    // Store named patterns for user convenience
    private final Map<String, String> savedPatterns;

    // Maximum number of recent patterns to store
    private static final int MAX_RECENT_PATTERNS = 10;

    /**
     * Constructor initializes collections for pattern storage
     */
    public RegexModel() {
        this.recentPatterns = new ArrayList<>();
        this.savedPatterns = new HashMap<>();

        // Add some commonly used regex patterns as examples
        savedPatterns.put("Email", "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
        savedPatterns.put("URL", "https?://\\S+");
        savedPatterns.put("Phone (RW)", "^(?:\\+250|0)?7[9832]\\d{7}$");
        savedPatterns.put("Date (MM/DD/YYYY)", "\\b(0?[1-9]|1[0-2])/(0?[1-9]|[12]\\d|3[01])/\\d{4}\\b");
    }

    /**
     * Validates if a regex pattern is syntactically correct
     * @param pattern The pattern to validate
     * @return true if a pattern is valid, false otherwise
     */
    public boolean isValidPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * Find all matches in text based on a regex pattern
     * @param text The text to search in
     * @param pattern The regex pattern to match
     * @return List of Match objects containing details about each match
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public List<Match> findMatches(String text, String pattern) throws PatternSyntaxException {
        List<Match> matches = new ArrayList<>();

        if (text == null || text.isEmpty() || pattern == null || pattern.isEmpty()) {
            return matches;
        }

        // Add to recent patterns if not already there
        addToRecentPatterns(pattern);

        // Compile pattern and create matcher
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(text);

        // Find and collect all matches with their positions
        while (matcher.find()) {
            String matchedText = matcher.group();
            int startPos = matcher.start();
            int endPos = matcher.end();

            matches.add(new Match(matchedText, startPos, endPos));
        }

        return matches;
    }

    /**
     * Replaces text matching a pattern with a replacement string
     * @param text The original text
     * @param pattern The regex pattern to match
     * @param replacement The replacement string
     * @return The text after replacements
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public String replaceText(String text, String pattern, String replacement)
            throws PatternSyntaxException {

        if (text == null || text.isEmpty() || pattern == null || pattern.isEmpty()) {
            return text != null ? text : "";
        }

        if (replacement == null) {
            replacement = "";
        }

        // Add to recent patterns
        addToRecentPatterns(pattern);

        // Compile pattern and replace all occurrences
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(text);

        return matcher.replaceAll(replacement);
    }

    /**
     * Adds a pattern to the recent patterns list
     * @param pattern The pattern to add
     */
    private void addToRecentPatterns(String pattern) {
        // Remove if already exists (to move it to front)
        recentPatterns.remove(pattern);

        // Add to the front of a list
        recentPatterns.addFirst(pattern);

        // Keep a list at max size
        if (recentPatterns.size() > MAX_RECENT_PATTERNS) {
            recentPatterns.removeLast();
        }
    }

    /**
     * Saves a named pattern
     * @param name The name to assign to the pattern
     * @param pattern The regex pattern
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public void savePattern(String name, String pattern) throws PatternSyntaxException {
        if (name == null || name.isEmpty() || pattern == null) {
            throw new IllegalArgumentException("Name and pattern must not be empty");
        }

        // Validate pattern before saving
        Pattern.compile(pattern); // Will throw PatternSyntaxException if invalid

        savedPatterns.put(name, pattern);
    }

    /**
     * Retrieves a saved pattern by name
     * @param name The name of the saved pattern
     * @return The pattern string or null if not found
     */
    public String getSavedPattern(String name) {
        return savedPatterns.get(name);
    }

    /**
     * @return List of all saved pattern names
     */
    public List<String> getSavedPatternNames() {
        return new ArrayList<>(savedPatterns.keySet());
    }

    /**
     * @return List of recently used patterns
     */
    public List<String> getRecentPatterns() {
        return new ArrayList<>(recentPatterns);
    }

    /**
     * Removes a saved pattern
     * @param name The name of the pattern to remove
     * @return true if a pattern was removed, false if not found
     */
    public boolean removeSavedPattern(String name) {
        if (savedPatterns.containsKey(name)) {
            savedPatterns.remove(name);
            return true;
        }
        return false;
    }

    /**
     * Inner class to represent a regex match with position information
     */
    public static class Match {
        private String text;
        private int startPosition;
        private int endPosition;

        public Match(String text, int startPosition, int endPosition) {
            this.text = text;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

        public String getText() {
            return text;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public int getEndPosition() {
            return endPosition;
        }

        @Override
        public String toString() {
            return "Match['" + text + "', start=" + startPosition + ", end=" + endPosition + "]";
        }
    }
}
