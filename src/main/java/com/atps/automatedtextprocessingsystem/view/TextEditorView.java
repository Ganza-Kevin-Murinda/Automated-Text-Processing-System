package com.atps.automatedtextprocessingsystem.view;

import com.atps.automatedtextprocessingsystem.controller.TextProcessingController;
import com.atps.automatedtextprocessingsystem.util.TextProcessingUtils.TextStatistics;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

/**
 * TextEditorView - Provides the text editing interface
 * Manages the text area and related controls for text input and editing
 */
public class TextEditorView {

    private final TextProcessingController controller;
    private final VBox root;
    private final TextArea textArea;
    private final Label statusLabel;

    /**
     * Constructor - initializes the text editor view
     * @param controller The text processing controller reference
     */
    public TextEditorView(TextProcessingController controller) {
        this.controller = controller;
        this.root = new VBox(10);
        this.textArea = new TextArea();
        this.statusLabel = new Label("Ready");

        initializeComponents();
    }

    /**
     * Get the root pane of this view
     *
     * @return The VBox containing this view
     */
    public VBox getRoot() {
        return root;
    }

    /**
     * Initialize all components for this view
     */
    private void initializeComponents() {
        // Configure the root container
        root.setPadding(new Insets(10));

        // Create and configure the toolbar
        ToolBar toolBar = createToolBar();

        // Configure the text area
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        textArea.setFont(Font.font("Monospaced", 12));

        // Set the text area in the controller
        controller.setTextArea(textArea);

        // Create status bar
        HBox statusBar = createStatusBar();

        // Add components to the root
        root.getChildren().addAll(toolBar, textArea, statusBar);
        VBox.setVgrow(textArea, Priority.ALWAYS);
    }

    /**
     * Create the toolbar with text editing buttons
     *
     * @return The configured toolbar
     */
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();

        // Create buttons
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clear());

        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e -> undo());

        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e -> redo());

        Button statsButton = new Button("Statistics");
        statsButton.setOnAction(e -> showTextStatistics());

        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Add buttons to toolbar
        toolBar.getItems().addAll(
                clearButton,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                undoButton,
                redoButton,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                statsButton
        );

        return toolBar;
    }

    /**
     * Create the status bar showing text information
     *
     * @return The status bar as an HBox
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Initialize with empty statistics (will be updated when text changes)
        Label lineCountLabel = new Label("Lines: 0");
        Label wordCountLabel = new Label("Words: 0");
        Label charCountLabel = new Label("Chars: 0");

        // Add listener to update statistics when text changes
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            TextStatistics stats = controller.getTextStatistics();
            lineCountLabel.setText("Lines: " + stats.getSentenceCount());
            wordCountLabel.setText("Words: " + stats.getWordCount());
            charCountLabel.setText("Chars: " + stats.getCharacterCount());
        });

        statusBar.getChildren().addAll(statusLabel, spacer, lineCountLabel, wordCountLabel, charCountLabel);

        return statusBar;
    }

    /**
     * Set content of the text area
     *
     * @param content Text content to set
     */
    public void setContent(String content) {
        textArea.setText(content);
        // This will indirectly update the service via the listener in controller
    }

    /**
     * Get content of the text area
     *
     * @return Current text content
     */
    public String getContent() {
        return textArea.getText();
    }

    /**
     * Clear the text editor
     */
    public void clear() {
        controller.clearText();
        updateStatusLabel("Text cleared");
    }

    /**
     * Perform undo operation
     */
    public void undo() {
        controller.undoTextChange();
        updateStatusLabel("Undo performed");
    }

    /**
     * Perform redo operation
     */
    public void redo() {
        controller.redoTextChange();
        updateStatusLabel("Redo performed");
    }

    /**
     * Cut selected text to clipboard
     */
    public void cut() {
        textArea.cut();
        updateStatusLabel("Text cut to clipboard");
    }

    /**
     * Copy selected text to clipboard
     */
    public void copy() {
        textArea.copy();
        updateStatusLabel("Text copied to clipboard");
    }

    /**
     * Paste text from clipboard
     */
    public void paste() {
        textArea.paste();
        updateStatusLabel("Text pasted from clipboard");
    }

    /**
     * Select all text
     */
    public void selectAll() {
        textArea.selectAll();
        updateStatusLabel("All text selected");
    }

    /**
     * Show text statistics dialog
     */
    private void showTextStatistics() {
        TextStatistics stats = controller.getTextStatistics();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Text Statistics");
        alert.setHeaderText("Current Text Statistics");

        // Create content for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Character count:"), 0, 0);
        grid.add(new Label(String.valueOf(stats.getCharacterCount())), 1, 0);

        grid.add(new Label("Word count:"), 0, 1);
        grid.add(new Label(String.valueOf(stats.getWordCount())), 1, 1);

        grid.add(new Label("Line count:"), 0, 2);
        grid.add(new Label(String.valueOf(stats.getSentenceCount())), 1, 2);

        grid.add(new Label("Sentences:"), 0, 3);
        grid.add(new Label(String.valueOf(stats.getSentenceCount())), 1, 3);

        grid.add(new Label("Paragraphs:"), 0, 4);
        grid.add(new Label(String.valueOf(stats.getParagraphCount())), 1, 4);

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();

        updateStatusLabel("Statistics displayed");
    }

    /**
     * Update the status label text
     *
     * @param message Message to display
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
    }
}