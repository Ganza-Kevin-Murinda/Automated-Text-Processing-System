package com.atps.automatedtextprocessingsystem.view;

import com.atps.automatedtextprocessingsystem.controller.MainController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;

/**
 * MainView - Main application window and layout structure
 * Creates and manages the primary UI components for the application
 */
public class MainView {

    private final MainController mainController;
    private final BorderPane root;

    // Main view components
    private TextEditorView textEditorView;
    private RegexView regexView;
    private TextRecordView recordView;
    private FileOperationsView fileOperationsView;

    /**
     * Constructor - initializes the main view and its components
     * @param mainController The main controller reference
     */
    public MainView(MainController mainController) {
        this.mainController = mainController;
        this.root = new BorderPane();

        // Initialize the view components
        initializeMenuBar();
        initializeMainContent();
    }

    /**
     * Get the root pane of the main view
     * @return The BorderPane containing the entire UI
     */
    public BorderPane getRoot() {
        return root;
    }

    /**
     * Initialize the application menu bar
     */
    private void initializeMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> mainController.getFileController().openFile());

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> mainController.getFileController().saveCurrentText());

        MenuItem saveAsItem = new MenuItem("Save As");
        saveAsItem.setOnAction(e -> mainController.getFileController().saveCurrentTextAs());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> mainController.exitApplication());

        fileMenu.getItems().addAll(openItem, saveItem, saveAsItem, new SeparatorMenuItem(), exitItem);

        // Edit Menu
        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setOnAction(e -> textEditorView.undo());

        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setOnAction(e -> textEditorView.redo());

        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setOnAction(e -> textEditorView.cut());

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> textEditorView.copy());

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> textEditorView.paste());

        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> textEditorView.selectAll());

        editMenu.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(),
                cutItem, copyItem, pasteItem, new SeparatorMenuItem(), selectAllItem);

        // Regex Menu
        Menu regexMenu = new Menu("Regex");
        MenuItem findItem = new MenuItem("Find");
        findItem.setOnAction(e -> regexView.showFindDialog());

        MenuItem replaceItem = new MenuItem("Replace");
        replaceItem.setOnAction(e -> regexView.showReplaceDialog());

        MenuItem savePatternItem = new MenuItem("Save Pattern");
        savePatternItem.setOnAction(e -> regexView.savePattern());

        MenuItem loadPatternItem = new MenuItem("Load Pattern");
        loadPatternItem.setOnAction(e -> regexView.loadPattern());

        regexMenu.getItems().addAll(findItem, replaceItem, savePatternItem, loadPatternItem);

        // Batch Menu
        Menu batchMenu = new Menu("Batch");
        MenuItem processDirItem = new MenuItem("Process Directory");
        processDirItem.setOnAction(e -> showBatchProcessDirectoryDialog());

        MenuItem processFilesItem = new MenuItem("Process Files");
        processFilesItem.setOnAction(e -> showBatchProcessFilesDialog());

        batchMenu.getItems().addAll(processDirItem, processFilesItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().add(aboutItem);

        // Add all menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, editMenu, regexMenu, batchMenu, helpMenu);

        // Set the menu bar at the top of the border pane
        root.setTop(menuBar);
    }

    /**
     * Handle new file creation
     */
    private void handleNewFile() {
        // Check for unsaved changes first
        if (mainController.hasUnsavedChanges()) {
            boolean shouldProceed = mainController.showUnsavedChangesDialog();
            if (!shouldProceed) {
                return;
            }
        }

        // Clear the text editor and reset the file path
        textEditorView.setContent("");
        mainController.setHasUnsavedChanges(false);
    }

    /**
     * Show batch process directory dialog
     */
    private void showBatchProcessDirectoryDialog() {
        if (fileOperationsView != null) {
            // Pass a dummy processor - the actual one will be selected in the dialog
            mainController.getFileController().batchProcessDirectory(
                    text -> text,
                    results -> handleBatchResults(results)
            );
        }
    }

    /**
     * Show batch process files dialog
     */
    private void showBatchProcessFilesDialog() {
        if (fileOperationsView != null) {
            // Pass a dummy processor - the actual one will be selected in the dialog
            mainController.getFileController().processSelectedFiles(
                    text -> text,
                    results -> handleBatchResults(results)
            );
        }
    }

    /**
     * Handle batch processing results
     */
    private void handleBatchResults(Map<String, String> results) {
        // Create a result string
        StringBuilder resultBuilder = new StringBuilder();
        results.forEach((path, result) -> {
            resultBuilder.append("File: ").append(path).append("\n");
            resultBuilder.append("Result: ").append(result).append("\n\n");
        });

        // Show the results in a dialog
        TextArea textArea = new TextArea(resultBuilder.toString());
        textArea.setEditable(false);
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(300);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Batch Processing Results");
        dialog.setHeaderText("Processing completed for " + results.size() + " files");
        dialog.getDialogPane().setContent(textArea);

        ButtonType exportButton = new ButtonType("Export Results");
        ButtonType closeButton = ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().addAll(exportButton, closeButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == exportButton) {
                mainController.getFileController().exportToFile(resultBuilder.toString(), "batch_results.txt");
            }
            return buttonType;
        });

        dialog.showAndWait();
    }

    /**
     * Initialize the main content area
     */
    private void initializeMainContent() {
        // Create a split pane for dividing the main content area
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);

        // Top section: Text editor and regex operations
        SplitPane topSplitPane = new SplitPane();
        topSplitPane.setOrientation(Orientation.HORIZONTAL);

        // Create text editor view with the actual implementation
        textEditorView = new TextEditorView(mainController.getTextProcessingController());
        VBox editorBox = new VBox();
        editorBox.getChildren().add(new Label("Text Editor"));
        editorBox.getChildren().add(textEditorView.getRoot());

        // Create regex view (placeholder until you implement the actual class)
        regexView = new RegexView(mainController.getRegexController());
        VBox regexBox = new VBox();
        regexBox.getChildren().add(new Label("Regex Operations"));
        regexBox.getChildren().add(regexView.getRoot());

        // Add the editor and regex components to the top split pane
        topSplitPane.getItems().addAll(editorBox, regexBox);
        topSplitPane.setDividerPositions(0.7);

        // Bottom section: Results view and file operations
        SplitPane bottomSplitPane = new SplitPane();
        bottomSplitPane.setOrientation(Orientation.HORIZONTAL);

        // Create results view with the actual implementation
        recordView = new TextRecordView(mainController.getTextProcessingController());
        VBox resultsBox = new VBox();
        resultsBox.getChildren().add(new Label("Results"));
        resultsBox.getChildren().add(recordView.getRoot());

        // Create file operations view (placeholder until you implement the actual class)
        fileOperationsView = new FileOperationsView(mainController.getFileController());
        VBox fileBox = new VBox();
        fileBox.getChildren().add(new Label("File Operations"));
        fileBox.getChildren().add(fileOperationsView.getRoot());

        // Add the results and file operations to the bottom split pane
        bottomSplitPane.getItems().addAll(resultsBox, fileBox);
        bottomSplitPane.setDividerPositions(0.7);

        // Add both split panes to the main split pane
        mainSplitPane.getItems().addAll(topSplitPane, bottomSplitPane);
        mainSplitPane.setDividerPositions(0.7);

        // Set the main split pane in the center of the border pane
        ScrollPane scrollPane = new ScrollPane(mainSplitPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true); // Optional: allows dragging to scroll

        root.setCenter(scrollPane);
    }



    /**
     * Show the about dialog
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Automated Text Processing System");
        alert.setContentText("A powerful text processing application with regex support.\n\n" +
                "Version: 1.0\n" +
                "© 2025 Mulk");
        alert.showAndWait();
    }

}