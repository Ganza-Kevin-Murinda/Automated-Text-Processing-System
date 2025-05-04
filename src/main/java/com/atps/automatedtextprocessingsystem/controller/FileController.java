package com.atps.automatedtextprocessingsystem.controller;

import com.atps.automatedtextprocessingsystem.model.FileModel;
import com.atps.automatedtextprocessingsystem.service.TextProcessingService;
import com.atps.automatedtextprocessingsystem.util.TextProcessingException;
import com.atps.automatedtextprocessingsystem.util.TextProcessingUtils;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FileController - Manages file interactions
 * Handles file selection, reading/writing operations, and batch processing
 */
public class FileController {

    private final TextProcessingService service;
    private final MainController mainController;
    private final ExecutorService executorService;
    private String currentFilePath = null;

    /**
     * Constructor
     * @param service The text processing service
     * @param mainController Reference to the main controller
     */
    public FileController(TextProcessingService service, MainController mainController, ExecutorService executorService) {
        this.service = service;
        this.mainController = mainController;
        this.executorService = executorService;
    }

    /**
     * Get the current file path
     * @return Current file path or null if not saved
     */
    public String getCurrentFilePath() {
        return currentFilePath;
    }

    /**
     * Open a file selection dialog and load the selected file
     */
    public void openFile() {
        // Check for unsaved changes first
        if (mainController.hasUnsavedChanges()) {
            boolean shouldProceed = mainController.showUnsavedChangesDialog();
            if (!shouldProceed) {
                return;
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(mainController.getPrimaryStage());
        if (file != null) {
            loadFile(file.getAbsolutePath());
        }
    }

    /**
     * Load text from a file path
     * @param filePath Path to the file
     */
    public void loadFile(String filePath) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                try {
                    return service.readFromFile(filePath);
                } catch (IOException e) {
                    TextProcessingUtils.logError("Error reading file: " + filePath, e);
                    throw e;
                }
            }
        };

        task.setOnSucceeded(event -> {
            String content = task.getValue();
            mainController.getTextProcessingController().updateTextDisplay(content);
            currentFilePath = filePath;
            mainController.setHasUnsavedChanges(false);
            TextProcessingUtils.logInfo("File loaded: " + filePath);
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            mainController.showErrorDialog(
                    "File Error",
                    "Error reading file: " + filePath,
                    exception.getMessage()
            );
        });

        executorService.submit(task);
    }

    /**
     * Save current text to the current file path or show save dialog if none
     * @return true if save was successful, false otherwise
     */
    public boolean saveCurrentText() {
        if (currentFilePath == null) {
            return saveCurrentTextAs();
        } else {
            return saveCurrentTextToFile(currentFilePath);
        }
    }

    /**
     * Show save as dialog and save current text
     * @return true if save was successful, false otherwise
     */
    public boolean saveCurrentTextAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Text File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("All Files", "*.*")
        );

        // Set initial directory if we have a current file
        if (currentFilePath != null) {
            File currentFile = new File(currentFilePath);
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        }

        File file = fileChooser.showSaveDialog(mainController.getPrimaryStage());
        if (file != null) {
            return saveCurrentTextToFile(file.getAbsolutePath());
        }
        return false;
    }

    /**
     * Save current text to a specific file path
     * @param filePath Path to save the file
     * @return true if save was successful, false otherwise
     */
    private boolean saveCurrentTextToFile(String filePath) {
        try {
            service.saveToFile(filePath);
            currentFilePath = filePath;
            mainController.setHasUnsavedChanges(false);
            TextProcessingUtils.logInfo("File saved: " + filePath);
            return true;
        } catch (IOException e) {
            TextProcessingUtils.logError("Error saving file: " + filePath, e);
            mainController.showErrorDialog(
                    "File Error",
                    "Error saving file: " + filePath,
                    e.getMessage()
            );
            return false;
        }
    }

    /**
     * Export processing results to a file
     * @param content Content to export
     * @param defaultFileName Suggested file name
     */
    public void exportToFile(String content, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Results");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("All Files", "*.*")
        );

        if (defaultFileName != null && !defaultFileName.isEmpty()) {
            fileChooser.setInitialFileName(defaultFileName);
        }

        File file = fileChooser.showSaveDialog(mainController.getPrimaryStage());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), content);
                TextProcessingUtils.logInfo("Results exported to: " + file.getAbsolutePath());

                mainController.showInfoDialog(
                        "Export Successful",
                        "Results exported successfully",
                        "File saved to: " + file.getAbsolutePath()
                );
            } catch (IOException e) {
                TextProcessingUtils.logError("Error exporting results: " + e.getMessage(), e);
                mainController.showErrorDialog(
                        "Export Error",
                        "Error exporting results",
                        e.getMessage()
                );
            }
        }
    }

    /**
     * Open a directory selection dialog for batch processing
     * @param processor Function to process each file
     * @param resultHandler Consumer to handle the batch processing results
     */
    public void batchProcessDirectory(Function<String, String> processor,
                                      Consumer<Map<String, String>> resultHandler) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory for Batch Processing");

        File directory = directoryChooser.showDialog(mainController.getPrimaryStage());
        if (directory != null) {
            batchProcessDirectory(directory.toPath(), processor, resultHandler);
        }
    }

    /**
     * Batch process all text files in a directory
     * @param directoryPath Path to the directory
     * @param processor Function to process each file
     * @param resultHandler Consumer to handle the batch processing results
     */
    public void batchProcessDirectory(Path directoryPath, Function<String, String> processor,
                                      Consumer<Map<String, String>> resultHandler) {
        Task<Map<String, String>> task = new Task<>() {
            @Override
            protected Map<String, String> call() throws Exception {
                try {
                    List<String> filePaths = Files.walk(directoryPath)
                            .filter(Files::isRegularFile)
                            .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                            .map(Path::toString)
                            .collect(Collectors.toList());

                    return service.batchProcessFiles(filePaths, processor);
                } catch (IOException e) {
                    TextProcessingUtils.logError("Error during batch processing: " + e.getMessage(), e);
                    throw e;
                }
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, String> results = task.getValue();
            TextProcessingUtils.logInfo("Batch processing completed. Files processed: " + results.size());
            resultHandler.accept(results);
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            TextProcessingUtils.logError("Batch processing failed: " + exception.getMessage(), exception);
            mainController.showErrorDialog(
                    "Batch Processing Error",
                    "Error during batch processing",
                    exception.getMessage()
            );
        });

        executorService.submit(task);
    }

    /**
     * Process selected files using a file chooser
     * @param processor Function to process each file's content
     * @param resultHandler Consumer to handle the batch processing results
     */
    public void processSelectedFiles(Function<String, String> processor,
                                     Consumer<Map<String, String>> resultHandler) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Process");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("All Files", "*.*")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(mainController.getPrimaryStage());
        if (files != null && !files.isEmpty()) {
            List<String> filePaths = files.stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());

            Task<Map<String, String>> task = new Task<>() {
                @Override
                protected Map<String, String> call() {
                    return service.batchProcessFiles(filePaths, processor);
                }
            };

            task.setOnSucceeded(event -> {
                Map<String, String> results = task.getValue();
                TextProcessingUtils.logInfo("File processing completed. Files processed: " + results.size());
                resultHandler.accept(results);
            });

            task.setOnFailed(event -> {
                Throwable exception = task.getException();
                TextProcessingUtils.logError("File processing failed: " + exception.getMessage(), exception);
                mainController.showErrorDialog(
                        "Processing Error",
                        "Error processing files",
                        exception.getMessage()
                );
            });

            executorService.submit(task);
        }
    }

    /**
     * Get the list of recently accessed files
     * @return List of file metadata objects
     */
    public List<FileModel.FileMetadata> getRecentFiles() {
        return service.getRecentFiles();
    }
}
