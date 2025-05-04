package com.atps.automatedtextprocessingsystem.controller;

import com.atps.automatedtextprocessingsystem.model.RegexModel;
import com.atps.automatedtextprocessingsystem.service.TextProcessingService;
import com.atps.automatedtextprocessingsystem.util.TextProcessingUtils;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.PatternSyntaxException;

/**
 * RegexController - Manages regex operations
 * Handles pattern validation, match/replace operations, and pattern history
 */
public class RegexController {

    private final TextProcessingService service;
    private final MainController mainController;
    private final ExecutorService executorService;

    /**
     * Constructor
     * @param service The text processing service
     * @param mainController Reference to the main controller
     */
    public RegexController(TextProcessingService service, MainController mainController, ExecutorService executorService) {
        this.service = service;
        this.mainController = mainController;
        this.executorService = executorService;

        TextProcessingUtils.logInfo("RegexController initialized");
    }

    /**
     * Validate if a regex pattern is syntactically correct
     * @param pattern The pattern to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPattern(String pattern) {
        return service.isValidRegexPattern(pattern);
    }

    /**
     * Find all matches of a regex pattern in the current text
     * @param pattern Regex pattern to match
     * @param resultHandler Consumer to handle the matches
     */
    public void findMatches(String pattern, Consumer<List<String>> resultHandler) {
        if (!isValidPattern(pattern)) {
            resultHandler.accept(new ArrayList<>());
            mainController.showErrorDialog(
                    "Invalid Pattern",
                    "The regular expression pattern is invalid",
                    "Please check your syntax and try again."
            );
            return;
        }

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                try {
                    return service.findMatches(pattern);
                } catch (PatternSyntaxException e) {
                    TextProcessingUtils.logError("Invalid regex pattern: " + e.getMessage(), e);
                    throw e;
                }
            }
        };

        task.setOnSucceeded(event -> {
            List<String> matches = task.getValue();
            TextProcessingUtils.logInfo("Found " + matches.size() + " matches for pattern: " + pattern);
            resultHandler.accept(matches);
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            TextProcessingUtils.logError("Error finding matches: " + exception.getMessage(), exception);
            mainController.showErrorDialog(
                    "Regex Error",
                    "Error finding matches",
                    exception.getMessage()
            );
            resultHandler.accept(new ArrayList<>());
        });

        executorService.submit(task);
    }

    /**
     * Find all matches with position information
     * @param pattern Regex pattern to match
     * @param resultHandler Consumer to handle the matches with positions
     */
    public void findMatchesWithPositions(String pattern, Consumer<List<RegexModel.Match>> resultHandler) {
        if (!isValidPattern(pattern)) {
            resultHandler.accept(new ArrayList<>());
            mainController.showErrorDialog(
                    "Invalid Pattern",
                    "The regular expression pattern is invalid",
                    "Please check your syntax and try again."
            );
            return;
        }

        Task<List<RegexModel.Match>> task = new Task<>() {
            @Override
            protected List<RegexModel.Match> call() {
                try {
                    return service.findMatchesWithPositions(pattern);
                } catch (PatternSyntaxException e) {
                    TextProcessingUtils.logError("Invalid regex pattern: " + e.getMessage(), e);
                    throw e;
                }
            }
        };

        task.setOnSucceeded(event -> {
            List<RegexModel.Match> matches = task.getValue();
            TextProcessingUtils.logInfo("Found " + matches.size() + " matches with positions for pattern: " + pattern);
            resultHandler.accept(matches);
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            TextProcessingUtils.logError("Error finding matches with positions: " + exception.getMessage(), exception);
            mainController.showErrorDialog(
                    "Regex Error",
                    "Error finding matches with positions",
                    exception.getMessage()
            );
            resultHandler.accept(new ArrayList<>());
        });

        executorService.submit(task);
    }

    /**
     * Replace text matching a pattern with replacement text
     * @param pattern The pattern to match
     * @param replacement The replacement string
     * @param resultHandler Consumer to handle the replacement count
     */
    public void replaceText(String pattern, String replacement, Consumer<Integer> resultHandler) {
        if (!isValidPattern(pattern)) {
            resultHandler.accept(0);
            mainController.showErrorDialog(
                    "Invalid Pattern",
                    "The regular expression pattern is invalid",
                    "Please check your syntax and try again."
            );
            return;
        }

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                try {
                    return service.replaceText(pattern, replacement);
                } catch (PatternSyntaxException e) {
                    TextProcessingUtils.logError("Invalid regex pattern: " + e.getMessage(), e);
                    throw e;
                }
            }
        };

        task.setOnSucceeded(event -> {
            int count = task.getValue();
            TextProcessingUtils.logInfo("Replaced " + count + " occurrences of pattern: " + pattern);

            // Update the text display after replacement
            TextArea textArea = mainController.getTextProcessingController().getTextArea();
            if (textArea != null) {
                textArea.setText(service.getCurrentText());
            }

            // Mark that there are unsaved changes
            mainController.setHasUnsavedChanges(true);

            resultHandler.accept(count);
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            TextProcessingUtils.logError("Error replacing text: " + exception.getMessage(), exception);
            mainController.showErrorDialog(
                    "Regex Error",
                    "Error replacing text",
                    exception.getMessage()
            );
            resultHandler.accept(0);
        });

        executorService.submit(task);
    }

    /**
     * Replace all occurrences in the provided text
     * @param text Original text
     * @param pattern Pattern to match
     * @param replacement Replacement string
     * @return The text after replacements
     */
    public String replaceAllText(String text, String pattern, String replacement) {
        try {
            return service.replaceAllText(text, pattern, replacement);
        } catch (PatternSyntaxException e) {
            TextProcessingUtils.logError("Invalid regex pattern: " + e.getMessage(), e);
            mainController.showErrorDialog(
                    "Invalid Pattern",
                    "The regular expression pattern is invalid",
                    e.getMessage()
            );
            return text; // Return original text if there's an error
        }
    }

    /**
     * Save a named regex pattern
     * @param name Name for the pattern
     * @param pattern The regex pattern
     * @return true if saved successfully, false otherwise
     */
    public boolean saveRegexPattern(String name, String pattern) {
        if (!isValidPattern(pattern)) {
            mainController.showErrorDialog(
                    "Invalid Pattern",
                    "Cannot save invalid regex pattern",
                    "Please correct the pattern before saving."
            );
            return false;
        }

        try {
            service.saveRegexPattern(name, pattern);
            TextProcessingUtils.logInfo("Saved regex pattern: " + name);
            return true;
        } catch (PatternSyntaxException e) {
            TextProcessingUtils.logError("Error saving regex pattern: " + e.getMessage(), e);
            mainController.showErrorDialog(
                    "Regex Error",
                    "Error saving regex pattern",
                    e.getMessage()
            );
            return false;
        }
    }

    /**
     * Remove a saved pattern
     * @param name Name of the pattern to remove
     * @return true if removed, false if not found
     */
    public boolean removeSavedPattern(String name) {
        boolean removed = service.removeSavedPattern(name);
        if (removed) {
            TextProcessingUtils.logInfo("Removed saved regex pattern: " + name);
        } else {
            TextProcessingUtils.logWarning("Failed to remove regex pattern (not found): " + name);
        }
        return removed;
    }

    /**
     * Get a saved pattern by name
     * @param name Name of the pattern
     * @return The pattern string or null if not found
     */
    public String getSavedPattern(String name) {
        return service.getSavedPattern(name);
    }

    /**
     * Get all saved pattern names
     * @return List of saved pattern names
     */
    public List<String> getSavedPatternNames() {
        return service.getSavedPatternNames();
    }

    /**
     * Get recently used patterns
     * @return List of recently used patterns
     */
    public List<String> getRecentPatterns() {
        return service.getRecentPatterns();
    }

}