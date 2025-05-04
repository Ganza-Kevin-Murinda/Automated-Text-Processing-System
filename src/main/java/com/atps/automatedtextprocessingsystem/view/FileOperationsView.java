package com.atps.automatedtextprocessingsystem.view;

import com.atps.automatedtextprocessingsystem.controller.FileController;
import com.atps.automatedtextprocessingsystem.model.FileModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * FileOperationsView - Provides UI elements for file operations
 * Displays controls for opening, saving, and processing files
 */
public class FileOperationsView {

    private final FileController fileController;
    private final VBox root;
    private final ListView<String> recentFilesList;
    private final ObservableList<String> recentFilesData;

    /**
     * Constructor - initializes the file operations view
     * @param fileController The file controller reference
     */
    public FileOperationsView(FileController fileController) {
        this.fileController = fileController;
        this.root = new VBox(10);
        this.root.setPadding(new Insets(10));
        this.recentFilesData = FXCollections.observableArrayList();
        this.recentFilesList = new ListView<>(recentFilesData);

        initializeComponents();
        refreshRecentFiles();
    }

    /**
     * Get the root pane of the file operations view
     * @return The VBox containing the UI components
     */
    public Pane getRoot() {
        return root;
    }

    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Section title
        Label titleLabel = new Label("File Operations");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        root.getChildren().add(titleLabel);

        // File operation buttons
        createFileOperationButtons();

        // Recent files section
        createRecentFilesSection();

        // Batch processing section
        createBatchProcessingSection();
    }

    /**
     * Create buttons for basic file operations
     */
    private void createFileOperationButtons() {
        // Create a button group for file operations
        TilePane buttonPane = new TilePane();
        buttonPane.setPrefColumns(2);
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);


        Button openButton = new Button("Open");
        openButton.setPrefWidth(100);
        openButton.setOnAction(e -> fileController.openFile());

        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(100);
        saveButton.setOnAction(e -> fileController.saveCurrentText());

        Button saveAsButton = new Button("Save As");
        saveAsButton.setPrefWidth(100);
        saveAsButton.setOnAction(e -> fileController.saveCurrentTextAs());

        buttonPane.getChildren().addAll(openButton, saveButton, saveAsButton);
        root.getChildren().add(buttonPane);
    }

    /**
     * Handle new file creation (this is not directly in FileController but needed for UI)
     */
    private void handleNewFile() {
        // We need to create a workaround since setNewText() isn't in the controller
        // This would clear the text editor and reset the current file path
        // For now, we'll just open a blank file
        // In a real implementation, you would add this method to FileController

        // We can simulate this by checking for unsaved changes and updating relevant fields
        if (fileController.getCurrentFilePath() != null) {
            // This will trigger the unsaved changes dialog if needed
            fileController.openFile();
        }
    }

    /**
     * Create the recent files section
     */
    private void createRecentFilesSection() {
        // Recent files section
        Label recentFilesLabel = new Label("Recent Files");
        recentFilesLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        recentFilesList.setPrefHeight(150);
        recentFilesList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedFile = recentFilesList.getSelectionModel().getSelectedItem();
                if (selectedFile != null && !selectedFile.isEmpty()) {
                    // Extract the file path from the display string
                    String filePath = selectedFile.split(" - ")[1];
                    fileController.loadFile(filePath);
                }
            }
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshRecentFiles());

        VBox recentFilesBox = new VBox(5, recentFilesLabel, recentFilesList, refreshButton);
        root.getChildren().add(recentFilesBox);
    }

    /**
     * Create the batch processing section
     */
    private void createBatchProcessingSection() {
        // Batch processing section
        Label batchLabel = new Label("Batch Processing");
        batchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Button processDirectoryButton = new Button("Process Directory");
        processDirectoryButton.setPrefWidth(150);
        processDirectoryButton.setOnAction(e -> showBatchProcessDialog());

        Button processFilesButton = new Button("Process Files");
        processFilesButton.setPrefWidth(150);
        processFilesButton.setOnAction(e -> showMultiFileProcessDialog());

        HBox batchButtonBox = new HBox(10, processDirectoryButton, processFilesButton);

        VBox batchBox = new VBox(5, batchLabel, batchButtonBox);
        root.getChildren().add(batchBox);
    }

    /**
     * Show dialog for batch processing a directory
     */
    private void showBatchProcessDialog() {
        // A simple dialog to select a processing function
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Batch Process");
        dialog.setHeaderText("Select Processing Option");
        dialog.setContentText("Choose a processing option for batch processing:");

        ButtonType buttonCount = new ButtonType("Count Words");
        ButtonType buttonUppercase = new ButtonType("Convert to Uppercase");
        ButtonType buttonCancel = ButtonType.CANCEL;

        dialog.getButtonTypes().setAll(buttonCount, buttonUppercase, buttonCancel);

        dialog.showAndWait().ifPresent(response -> {
            if (response == buttonCount) {
                // Word count function
                Function<String, String> processor = text -> "Word count: " + countWords(text);
                fileController.batchProcessDirectory(processor, this::handleBatchResults);
            } else if (response == buttonUppercase) {
                // Uppercase conversion function
                Function<String, String> processor = String::toUpperCase;
                fileController.batchProcessDirectory(processor, this::handleBatchResults);
            }
        });
    }

    /**
     * Show dialog for processing multiple selected files
     */
    private void showMultiFileProcessDialog() {
        // A simple dialog to select a processing function
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Process Files");
        dialog.setHeaderText("Select Processing Option");
        dialog.setContentText("Choose a processing option for the selected files:");

        ButtonType buttonCount = new ButtonType("Count Words");
        ButtonType buttonUppercase = new ButtonType("Convert to Uppercase");
        ButtonType buttonCancel = ButtonType.CANCEL;

        dialog.getButtonTypes().setAll(buttonCount, buttonUppercase, buttonCancel);

        dialog.showAndWait().ifPresent(response -> {
            if (response == buttonCount) {
                // Word count function
                Function<String, String> processor = text -> "Word count: " + countWords(text);
                fileController.processSelectedFiles(processor, this::handleBatchResults);
            } else if (response == buttonUppercase) {
                // Uppercase conversion function
                Function<String, String> processor = String::toUpperCase;
                fileController.processSelectedFiles(processor, this::handleBatchResults);
            }
        });
    }

    /**
     * Count words in a text
     * @param text The text to count words in
     * @return The number of words
     */
    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\s+").length;
    }

    /**
     * Handle batch processing results
     * @param results Map of file paths to processing results
     */
    private void handleBatchResults(Map<String, String> results) {
        // Create a dialog to display the results
        Dialog<ButtonType> resultsDialog = new Dialog<>();
        resultsDialog.setTitle("Batch Processing Results");
        resultsDialog.setHeaderText("Processing completed for " + results.size() + " files");

        // Create a text area to display the results
        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefWidth(500);
        resultsArea.setPrefHeight(300);

        StringBuilder content = new StringBuilder();
        results.forEach((path, result) -> {
            content.append("File: ").append(path).append("\n");
            content.append("Result: ").append(result).append("\n\n");
        });
        resultsArea.setText(content.toString());

        resultsDialog.getDialogPane().setContent(resultsArea);

        // Add buttons to the dialog
        ButtonType exportButtonType = new ButtonType("Export Results");
        ButtonType closeButtonType = ButtonType.CLOSE;
        resultsDialog.getDialogPane().getButtonTypes().addAll(exportButtonType, closeButtonType);

        // Handle the export button
        resultsDialog.setResultConverter(dialogButton -> {
            if (dialogButton == exportButtonType) {
                fileController.exportToFile(content.toString(), "batch_results.txt");
            }
            return dialogButton;
        });

        resultsDialog.showAndWait();
    }

    /**
     * Refresh the recent files list
     */
    public void refreshRecentFiles() {
        recentFilesData.clear();
        List<FileModel.FileMetadata> recentFiles = fileController.getRecentFiles();

        if (recentFiles != null && !recentFiles.isEmpty()) {
            for (FileModel.FileMetadata metadata : recentFiles) {
                String displayPath = metadata.getFilePath();
                recentFilesData.add(displayPath);
            }
        } else {
            recentFilesData.add("No recent files");
        }
    }
}