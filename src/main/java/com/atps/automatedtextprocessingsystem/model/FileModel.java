package com.atps.automatedtextprocessingsystem.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Model class for handling file operations including reading, writing,
 * and processing files with streams.
 */
public class FileModel {

    // Store metadata about recently processed files
    private final List<FileMetadata> recentFiles;

    // Maximum number of recent files to track
    private static final int MAX_RECENT_FILES = 10;

    /**
     * Constructor initializes the recent files list
     */
    public FileModel() {
        this.recentFiles = new ArrayList<>();
    }

    /**
     * Reads text from a file
     * @param filePath Path to the file
     * @return The text content of the file
     * @throws IOException If an I/O error occurs
     */
    public String readTextFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        Path path = Paths.get(filePath);
        StringBuilder content = new StringBuilder();

        // Use try-with-resources to ensure proper resource management
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }

        // Add to the recent files list
        updateRecentFiles(new FileMetadata(filePath, LocalDateTime.now(),
                Files.size(path), "Read"));

        return content.toString();
    }

    /**
     * Writes text to a file
     * @param filePath Path to the file
     * @param content The text content to write
     * @throws IOException If an I/O error occurs
     */
    public void writeTextFile(String filePath, String content) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        if (content == null) {
            content = "";
        }

        Path path = Paths.get(filePath);

        // Use try-with-resources for proper resource management
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(content);
        }

        // Add to the recent files list
        updateRecentFiles(new FileMetadata(filePath, LocalDateTime.now(),
                Files.size(path), "Write"));
    }

    /**
     * Process a file line by line using a stream
     * @param filePath Path to the file
     * @param lineProcessor Function to process each line — it lets you pass a lambda expression that defines what to do with each line
     * @return List of processed results
     * @throws IOException If an I/O error occurs
     */
    public <T> List<T> processFileWithStream(String filePath, Function<String, T> lineProcessor)
            throws IOException {

        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        if (lineProcessor == null) {
            throw new IllegalArgumentException("Line processor cannot be null");
        }

        Path path = Paths.get(filePath);

        // Process file using streams
        List<T> results;
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            results = lines.map(lineProcessor).collect(Collectors.toList());
        }

        // Add to the recent files list
        updateRecentFiles(new FileMetadata(filePath, LocalDateTime.now(),
                Files.size(path), "Process"));

        return results;
    }

    /**
     * Process multiple files in batch mode
     * @param filePaths List of file paths to process
     * @param processor Function to process each file's content
     * @return Map of file paths to processing results
     */
    public Map<String, String> batchProcessFiles(List<String> filePaths,
                                                 Function<String, String> processor) {

        if (filePaths == null || filePaths.isEmpty()) {
            return Collections.emptyMap();
        }

        if (processor == null) {
            throw new IllegalArgumentException("Processor cannot be null");
        }

        Map<String, String> results = new HashMap<>();

        // Process each file and collect results
        for (String filePath : filePaths) {
            try {
                String content = readTextFile(filePath);
                String processedContent = processor.apply(content);
                results.put(filePath, processedContent);
            } catch (IOException e) {
                // Store the error message as the result for this file
                results.put(filePath, "Error: " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * Updates the list of recently accessed files
     * @param metadata Metadata about the file access
     */
    private void updateRecentFiles(FileMetadata metadata) {
        // Remove if the same file is already in the list
        recentFiles.removeIf(f -> f.getFilePath().equals(metadata.getFilePath()));

        // Add to the front of the list
        recentFiles.addFirst(metadata);

        // Keep the list at max size
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.removeLast();
        }
    }

    /**
     * @return List of recent file metadata
     */
    public List<FileMetadata> getRecentFiles() {
        return new ArrayList<>(recentFiles);
    }

    /**
     * Inner class to store metadata about processed files
     */
    public static class FileMetadata {
        private String filePath;
        private LocalDateTime accessTime;
        private long fileSize;
        private String operationType;

        public FileMetadata(String filePath, LocalDateTime accessTime,
                            long fileSize, String operationType) {
            this.filePath = filePath;
            this.accessTime = accessTime;
            this.fileSize = fileSize;
            this.operationType = operationType;
        }

        public String getFilePath() {
            return filePath;
        }

        public LocalDateTime getAccessTime() {
            return accessTime;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getOperationType() {
            return operationType;
        }

        @Override
        public String toString() {
            return "FileMetadata[path='" + filePath + "', accessed=" + accessTime +
                    ", size=" + fileSize + " bytes, operation=" + operationType + "]";
        }
    }
}
