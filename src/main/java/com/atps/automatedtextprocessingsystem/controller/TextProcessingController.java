package com.atps.automatedtextprocessingsystem.controller;

import com.atps.automatedtextprocessingsystem.model.DataModel;
import com.atps.automatedtextprocessingsystem.service.TextProcessingService;
import com.atps.automatedtextprocessingsystem.util.TextProcessingException;
import com.atps.automatedtextprocessingsystem.util.TextProcessingUtils;
import com.atps.automatedtextprocessingsystem.util.TextProcessingUtils.TextStatistics;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.PatternSyntaxException;

/**
 * TextProcessingController - Handles text processing operations
 * Manages text input, processing, and updates the view with results
 */
public class TextProcessingController {

    private final TextProcessingService service;
    private final MainController mainController;
    private final ExecutorService executorService;

    private TextArea textArea; // Reference to the main text area in the view

    /**
     * Constructor
     * @param service The text processing service
     * @param mainController Reference to the main controller
     */
    public TextProcessingController(TextProcessingService service, MainController mainController, ExecutorService executorService ) {
        this.service = service;
        this.mainController = mainController;
        this.executorService = executorService;

        TextProcessingUtils.logInfo("TextProcessingController initialized");
    }

    /**
     * Set the text area reference from the view
     * @param textArea The JavaFX text area
     */
    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;

        // Add listener to detect changes in the text area
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            // Update the service with the new text
            updateServiceText(newValue);
            // Mark that there are unsaved changes
            mainController.setHasUnsavedChanges(true);
        });
    }

    /**
     * Get the text area reference
     * @return The JavaFX text area
     */
    public TextArea getTextArea() {
        return textArea;
    }

    /**
     * Get the current text from the service
     * @return The current text
     */
    public String getCurrentText() {
        return service.getCurrentText();
    }

    /**
     * Update the text in the service
     * @param text The new text
     */
    public void updateServiceText(String text) {
        service.setCurrentText(text);
    }

    /**
     * Update the text in the UI
     * @param text The new text to display
     */
    public void updateTextDisplay(String text) {
        if (textArea != null) {
            textArea.setText(text);
        }
    }

    /**
     * Clear the current text
     */
    public void clearText() {
        service.clearText();
        updateTextDisplay("");
        mainController.setHasUnsavedChanges(false);
        TextProcessingUtils.logInfo("Text cleared");
    }

    /**
     * Undo the last text change
     */
    public void undoTextChange() {
        if (service.undoTextChange()) {
            updateTextDisplay(service.getCurrentText());
            TextProcessingUtils.logInfo("Undo operation performed");
        } else {
            TextProcessingUtils.logInfo("No changes to undo");
        }
    }

    /**
     * Redo previously undone text change
     */
    public void redoTextChange() {
        if (service.redoTextChange()) {
            updateTextDisplay(service.getCurrentText());
            TextProcessingUtils.logInfo("Redo operation performed");
        } else {
            TextProcessingUtils.logInfo("No changes to redo");
        }
    }

    /**
     * Get statistics about the current text
     * @return TextStatistics object with text metrics
     */
    public TextStatistics getTextStatistics() {
        return TextProcessingUtils.getTextStatistics(service.getCurrentText());
    }

    /**
     * Save the current text as a named record
     * @param name Name for the record
     * @param source Source of the text
     * @return ID of the created record
     */
    public int saveAsTextRecord(String name, String source) {
        int recordId = service.saveCurrentTextAsRecord(name, source);
        TextProcessingUtils.logInfo("Text saved as record: " + name);
        return recordId;
    }

    /**
     * Load a text record by ID
     * @param recordId ID of the record to load
     * @return true if successful, false if record not found
     */
    public boolean loadTextRecord(int recordId) {
        boolean success = service.loadTextRecordToCurrent(recordId);
        if (success) {
            updateTextDisplay(service.getCurrentText());
            TextProcessingUtils.logInfo("Loaded text record ID: " + recordId);
        } else {
            TextProcessingUtils.logWarning("Failed to load text record ID: " + recordId);
        }
        return success;
    }

    /**
     * Get all text records
     * @return List of text records
     */
    public List<DataModel.TextRecord> getAllTextRecords() {
        return service.getAllTextRecords();
    }

    /**
     * Process text in the background to avoid freezing the UI
     * @param processor The text processing function to run
     */
    public void processTextAsync(Runnable processor) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                processor.run();
                return null;
            }
        };

        executorService.submit(task);
    }

    /**
     * Analyze word frequency in a text record
     * @param recordId ID of the record to analyze
     * @return Map of words to frequencies
     */
    public Map<String, Integer> analyzeWordFrequency(int recordId) {
        return service.analyzeWordFrequency(recordId);
    }

    /**
     * Find most frequent words in a text record
     * @param recordId ID of the record
     * @param topN Number of top words to return
     * @return List of word-frequency pairs sorted by frequency
     */
    public List<Map.Entry<String, Integer>> getMostFrequentWords(int recordId, int topN) {
        return service.getMostFrequentWords(recordId, topN);
    }

    /**
     * Search for matches across all text records
     * @param pattern Regex pattern to search for
     * @return Map of record IDs to matches in that record
     */
    public Map<Integer, List<String>> searchAcrossRecords(String pattern) {
        try {
            return service.searchAcrossRecords(pattern);
        } catch (PatternSyntaxException e) {
            handlePatternSyntaxException(e);
            return Map.of();
        }
    }

    /**
     * Handle pattern syntax exceptions
     * @param e The exception that occurred
     */
    private void handlePatternSyntaxException(PatternSyntaxException e) {
        TextProcessingUtils.logError("Invalid regex pattern: " + e.getMessage(), e);
        mainController.showErrorDialog(
                "Invalid Regex Pattern",
                "The regular expression pattern is invalid",
                e.getMessage()
        );
    }

}