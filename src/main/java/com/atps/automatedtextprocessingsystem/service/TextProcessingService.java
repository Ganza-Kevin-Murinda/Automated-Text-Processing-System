package com.atps.automatedtextprocessingsystem.service;

import com.atps.automatedtextprocessingsystem.model.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;

/**
 * Service layer that integrates all model components.
 * Acts as a facade and business logic handler for controllers.
 */
public class TextProcessingService {

    private final TextProcessingModel textModel;
    private final RegexModel regexModel;
    private final FileModel fileModel;
    private final DataModel dataModel;

    public TextProcessingService() {
        this.textModel = new TextProcessingModel();
        this.regexModel = new RegexModel();
        this.fileModel = new FileModel();
        this.dataModel = new DataModel();
    }

    //---------------- Text Processing Methods ----------------//

    /**
     * Gets the current text content
     * @return Current text being processed
     */
    public String getCurrentText() {
        return textModel.getText();
    }

    /**
     * Sets the current text content
     * @param text New text content
     */
    public void setCurrentText(String text) {
        textModel.setText(text);
    }

    /**
     * Clears the current text
     */
    public void clearText() {
        textModel.clearText();
    }

    /**
     * Undoes the last text change
     * @return true if undo successful, false otherwise
     */
    public boolean undoTextChange() {
        return textModel.undo();
    }

    /**
     * Redoes a previously undone text change
     * @return true if redo successful, false otherwise
     */
    public boolean redoTextChange() {
        return textModel.redo();
    }

    //---------------- Regex Methods ----------------//

    /**
     * Finds all matches of a regex pattern in the current text
     * @param pattern Regex pattern to match
     * @return List of matched strings
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public List<String> findMatches(String pattern) throws PatternSyntaxException {
        return textModel.findMatches(pattern);
    }

    /**
     * Finds all matches with detailed position information
     * @param pattern Regex pattern to match
     * @return List of Match objects with position info
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public List<RegexModel.Match> findMatchesWithPositions(String pattern)
            throws PatternSyntaxException {
        return regexModel.findMatches(textModel.getText(), pattern);
    }

    /**
     * Replaces text matching a pattern with replacement text
     * @param pattern The pattern to match
     * @param replacement The replacement string
     * @return The number of replacements made
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public int replaceText(String pattern, String replacement) throws PatternSyntaxException {
        return textModel.replaceText(pattern, replacement);
    }

    /**
     * Replaces all text matching a pattern with a replacement string
     * @param text The original text
     * @param pattern The regex pattern to match
     * @param replacement The replacement string
     * @return The text after replacements
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public String replaceAllText(String text, String pattern, String replacement){
        return regexModel.replaceText(text, pattern, replacement);
    }

    /**
     * Validates if a regex pattern is syntactically correct
     * @param pattern The pattern to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidRegexPattern(String pattern) {
        return regexModel.isValidPattern(pattern);
    }

    /**
     * Saves a named regex pattern
     * @param name Name for the pattern
     * @param pattern The regex pattern
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public void saveRegexPattern(String name, String pattern) throws PatternSyntaxException {
        regexModel.savePattern(name, pattern);
    }

    /**
     * Removes a saved pattern
     * @param name The name of the pattern to remove
     * @return true if a pattern was removed, false if not found
     */
    public boolean removeSavedPattern(String name) {
        return regexModel.removeSavedPattern(name);
    }

    /**
     * Gets a saved pattern by name
     * @param name Name of the pattern
     * @return The pattern string or null if not found
     */
    public String getSavedPattern(String name) {
        return regexModel.getSavedPattern(name);
    }

    /**
     * @return List of saved pattern names
     */
    public List<String> getSavedPatternNames() {
        return regexModel.getSavedPatternNames();
    }

    /**
     * @return List of recently used patterns
     */
    public List<String> getRecentPatterns() {
        return regexModel.getRecentPatterns();
    }

    //---------------- File Methods ----------------//

    /**
     * Reads text from a file
     * @param filePath Path to the file
     * @return The text content
     * @throws IOException If an I/O error occurs
     */
    public String readFromFile(String filePath) throws IOException {
        String content = fileModel.readTextFile(filePath);
        textModel.setText(content);
        return content;
    }

    /**
     * Saves current text to a file
     * @param filePath Path to the file
     * @throws IOException If an I/O error occurs
     */
    public void saveToFile(String filePath) throws IOException {
        fileModel.writeTextFile(filePath, textModel.getText());
    }

    /**
     * Processes a file line by line using a stream
     * @param filePath Path to the file
     * @param lineProcessor Function to process each line
     * @return List of processed results
     * @throws IOException If an I/O error occurs
     */
    public <T> List<T> processFileWithStream(String filePath, Function<String, T> lineProcessor)
            throws IOException {
        return fileModel.processFileWithStream(filePath, lineProcessor);
    }

    /**
     * Process multiple files in batch mode
     * @param filePaths List of file paths to process
     * @param processor Function to process each file's content
     * @return Map of file paths to processing results
     */
    public Map<String, String> batchProcessFiles(List<String> filePaths,
                                                 Function<String, String> processor) {
        return fileModel.batchProcessFiles(filePaths, processor);
    }

    /**
     * @return List of recently accessed files
     */
    public List<FileModel.FileMetadata> getRecentFiles() {
        return fileModel.getRecentFiles();
    }

    //---------------- Data Analysis Methods ----------------//

    /**
     * Adds the current text as a new record
     * @param name Name for the record
     * @param source Source of the text
     * @return ID of the created record
     */
    public int saveCurrentTextAsRecord(String name, String source) {
        return dataModel.addTextRecord(name, textModel.getText(), source);
    }

    /**
     * Adds text as a new record
     * @param name Name for the record
     * @param content Text content
     * @param source Source of the text
     * @return ID of the created record
     */
    public int addTextRecord(String name, String content, String source) {
        return dataModel.addTextRecord(name, content, source);
    }

    /**
     * Updates an existing text record
     * @param id ID of the record to update
     * @param name New name (or null to keep existing)
     * @param content New content (or null to keep existing)
     * @return true if update successful, false if record not found
     */
    public boolean updateTextRecord(int id, String name, String content){
        return dataModel.updateTextRecord(id, name, content);
    }

    /**
     * Removes a text record
     * @param id ID of the record to remove
     * @return true if removal successful, false if record not found
     */
    public boolean removeTextRecord(int id) {
        return dataModel.removeTextRecord(id);
    }

    /**
     * @return List of all stored text records
     */
    public List<DataModel.TextRecord> getAllTextRecords() {
        return dataModel.getAllTextRecords();
    }

    /**
     * Gets a record by ID
     * @param id ID of the record
     * @return The record or null if not found
     */
    public DataModel.TextRecord getTextRecord(int id) {
        return dataModel.getTextRecord(id);
    }

    /**
     * Loads a text record into the current text
     * @param id ID of the record to load
     * @return true if successful, false if record not found
     */
    public boolean loadTextRecordToCurrent(int id) {
        DataModel.TextRecord record = dataModel.getTextRecord(id);
        if (record != null) {
            textModel.setText(record.getContent());
            return true;
        }
        return false;
    }

    /**
     * Analyzes word frequency in a text record
     * @param id ID of the record to analyze
     * @return Map of words to frequencies, or empty map if record not found
     */
    public Map<String, Integer> analyzeWordFrequency(int id) {
        return dataModel.analyzeWordFrequency(id);
    }

    /**
     * Finds most frequent words in a text record
     * @param id ID of the record
     * @param topN Number of top words to return
     * @return List of word-frequency pairs sorted by frequency
     */
    public List<Map.Entry<String, Integer>> getMostFrequentWords(int id, int topN) {
        return dataModel.getMostFrequentWords(id, topN);
    }

    /**
     * Searches for a pattern across all text records
     * @param pattern Regex pattern to search for
     * @return Map of record IDs to matches in that record
     */
    public Map<Integer, List<String>> searchAcrossRecords(String pattern) {
        return dataModel.searchAcrossRecords(pattern);
    }
}
