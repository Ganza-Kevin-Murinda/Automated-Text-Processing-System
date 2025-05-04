package com.atps.automatedtextprocessingsystem.model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Model class for data collection, analysis, and storage.
 * Handles text analytics and persistence of processed data.
 */
public class DataModel {

    // Store processed text records
    private final List<TextRecord> textRecords;

    // Cache word frequency results for better performance
    private final Map<String, Map<String, Integer>> frequencyCache;

    /**
     * Constructor initializes collections
     */
    public DataModel() {
        this.textRecords = new ArrayList<>();
        this.frequencyCache = new HashMap<>();
    }

    /**
     * Adds a text record to the collection
     * @param name Identifier for the text
     * @param content The text content
     * @param source Source of the text (file, user input, etc.)
     * @return ID of the added record
     */
    public int addTextRecord(String name, String content, String source) {
        if (name == null || name.isEmpty()) {
            name = "Record_" + (textRecords.size() + 1);
        }

        if (content == null) {
            content = "";
        }

        if (source == null) {
            source = "Unknown";
        }

        // Create new record
        TextRecord record = new TextRecord(textRecords.size(), name, content, source);
        textRecords.add(record);

        // Clear cached frequency data for this record
        frequencyCache.remove(name);

        return record.getId();
    }

    /**
     * Updates an existing text record
     * @param id ID of the record to update
     * @param name New name (or null to keep existing)
     * @param content New content (or null to keep existing)
     * @return true if update successful, false if record not found
     */
    public boolean updateTextRecord(int id, String name, String content) {
        // Find the record with the given ID

        for (TextRecord record : textRecords) {
            if (record.getId() == id) {

                String oldName = record.getName();

                if (name != null && !name.isEmpty()) {
                    record.setName(name);
                }
                if (content != null) {
                    record.setContent(content);
                }

                frequencyCache.remove(oldName); // always clear cache for the original name

                return true;
            }
        }

        return false; // Record not found
    }

    /**
     * Removes a text record
     * @param id ID of the record to remove
     * @return true if removal successful, false if record not found
     */
    public boolean removeTextRecord(int id) {
        // Find the record with the given ID
        for (Iterator<TextRecord> it = textRecords.iterator(); it.hasNext();) {
            TextRecord record = it.next();
            if (record.getId() == id) {
                // Clear cached frequency data for this record
                frequencyCache.remove(record.getName());
                it.remove();
                return true;
            }
        }

        return false; // Record not found
    }

    /**
     * Retrieves a text record by ID
     * @param id ID of the record to retrieve
     * @return The record or null if not found
     */
    public TextRecord getTextRecord(int id) {
        for (TextRecord record : textRecords) {
            if (record.getId() == id) {
                return record;
            }
        }

        return null; // Record not found
    }

    /**
     * @return List of all text records
     */
    public List<TextRecord> getAllTextRecords() {
        return new ArrayList<>(textRecords);
    }

    /**
     * Analyzes word frequency in a text record
     * @param id ID of the record to analyze
     * @return Map of words to their frequencies, or empty map if record not found
     */
    public Map<String, Integer> analyzeWordFrequency(int id) {
        TextRecord record = getTextRecord(id);
        if (record == null || record.getContent().isEmpty()) {
            return Collections.emptyMap();
        }

        // Check if the result is cached
        if (frequencyCache.containsKey(record.getName())) {
            return new HashMap<>(frequencyCache.get(record.getName()));
        }

        // Split the text into words (non-word characters as delimiters)
        String[] words = record.getContent().split("\\W+");

        // Count word frequencies using streams
        Map<String, Integer> frequencies = Arrays.stream(words)
                .filter(word -> !word.isEmpty()) // Skip empty strings
                .map(String::toLowerCase) // Convert to lowercase for case-insensitive counting
                .collect(Collectors.groupingBy(
                        Function.identity(), // The word itself as the key
                        Collectors.summingInt(word -> 1) // Count occurrences
                ));

        // Cache the result
        frequencyCache.put(record.getName(), new HashMap<>(frequencies));

        return frequencies;
    }

    /**
     * Finds the most frequent words in a text record
     * @param id ID of the record to analyze
     * @param topN Number of top words to return
     * @return List of word-frequency pairs, sorted by frequency (descending)
     */
    public List<Map.Entry<String, Integer>> getMostFrequentWords(int id, int topN) {
        Map<String, Integer> frequencies = analyzeWordFrequency(id);

        if (frequencies.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort by frequency (descending) and limit to topN entries
        return frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Searches for a pattern across all text records
     * @param pattern Regex pattern to search for
     * @return Map of record IDs to list of matches in that record
     */
    public Map<Integer, List<String>> searchAcrossRecords(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, List<String>> results = new HashMap<>();
        Pattern compiledPattern = Pattern.compile(pattern);

        // Search each record
        for (TextRecord record : textRecords) {
            List<String> matches = new ArrayList<>();

            // Find all matches in this record
            Matcher matcher = compiledPattern.matcher(record.getContent());
            while (matcher.find()) {
                matches.add(matcher.group());
            }

            // Add to results if matches found
            if (!matches.isEmpty()) {
                results.put(record.getId(), matches);
            }
        }

        return results;
    }

    /**
     * Inner class to represent a processed text record
     */
    public static class TextRecord {
        private int id;
        private String name;
        private String content;
        private String source;
        private LocalDateTime creationDate;

        public TextRecord(int id, String name, String content, String source) {
            this.id = id;
            this.name = name;
            this.content = content;
            this.source = source;
            this.creationDate = LocalDateTime.now();
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSource() {
            return source;
        }

        public LocalDateTime getCreationDate() {
            return creationDate;
        }

        @Override
        public String toString() {
            return "TextRecord[id=" + id + ", name='" + name + "', content='"+ content +"', source='" + source +
                    "', created=" + creationDate + ", content length=" + content.length() + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            TextRecord other = (TextRecord) obj;
            return id == other.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
