package com.atps.automatedtextprocessingsystem.view;

import com.atps.automatedtextprocessingsystem.controller.TextProcessingController;
import com.atps.automatedtextprocessingsystem.model.RegexModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * ResultsView - Displays text processing results
 * Shows match results, provides navigation and export options
 */
public class ResultsView {

    private final TextProcessingController textProcessingController;
    private final BorderPane root;

    // UI Components
    private TextArea resultsTextArea;
    private ListView<String> matchesList;
    private Label matchCountLabel;
    private Label currentMatchLabel;
    private ComboBox<String> exportFormatComboBox;
    private Button exportButton;
    private Button copyButton;
    private Button clearButton;
    private Button nextMatchButton;
    private Button prevMatchButton;
    private TextFlow statusFlow;

    // Data
    private ObservableList<String> matches;
    private int currentMatchIndex = -1;
    private List<RegexModel.Match> matchesWithPositions;

    /**
     * Constructor - initializes the results view
     * @param textProcessingController The text processing controller reference
     */
    public ResultsView(TextProcessingController textProcessingController) {
        this.textProcessingController = textProcessingController;
        this.root = new BorderPane();
        this.matches = FXCollections.observableArrayList();

        initializeComponents();
        setupEventHandlers();
    }

    /**
     * Get the root pane of the results view
     * @return The BorderPane containing all results view components
     */
    public Pane getRoot() {
        return root;
    }

    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Configure root
        root.setPadding(new Insets(10));

        // Create top section with title and counters
        HBox topSection = createTopSection();
        root.setTop(topSection);

        // Create center section with results text area and matches list
        SplitPane centerSection = createCenterSection();
        root.setCenter(centerSection);

        // Create bottom section with navigation and export options
        VBox bottomSection = createBottomSection();
        root.setBottom(bottomSection);
    }

    /**
     * Create the top section with title and counters
     * @return HBox containing the top section components
     */
    private HBox createTopSection() {
        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(0, 0, 10, 0));

        // Title label
        Label titleLabel = new Label("Results");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Match counters
        matchCountLabel = new Label("No matches");
        currentMatchLabel = new Label("");

        topSection.getChildren().addAll(titleLabel, spacer, currentMatchLabel, matchCountLabel);
        return topSection;
    }

    /**
     * Create the center section with results display
     * @return SplitPane containing the results display components
     */
    private SplitPane createCenterSection() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        // Results text area for displaying processed text
        resultsTextArea = new TextArea();
        resultsTextArea.setEditable(false);
        resultsTextArea.setWrapText(true);
        resultsTextArea.setPromptText("Processing results will appear here");

        // Matches list for displaying individual matches
        VBox matchesBox = new VBox(5);
        Label matchesLabel = new Label("Matches");
        matchesLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        matchesList = new ListView<>(matches);
        matchesList.setPrefHeight(200);

        matchesBox.getChildren().addAll(matchesLabel, matchesList);
        matchesBox.setPadding(new Insets(0, 0, 0, 10));

        // Add components to split pane
        splitPane.getItems().addAll(resultsTextArea, matchesBox);
        splitPane.setDividerPositions(0.7);

        return splitPane;
    }

    /**
     * Create the bottom section with navigation and export options
     * @return VBox containing the bottom section components
     */
    private VBox createBottomSection() {
        VBox bottomSection = new VBox(10);
        bottomSection.setPadding(new Insets(10, 0, 0, 0));

        // Navigation buttons
        HBox navigationBox = new HBox(10);

        prevMatchButton = new Button("Previous Match");
        prevMatchButton.setDisable(true);

        nextMatchButton = new Button("Next Match");
        nextMatchButton.setDisable(true);

        clearButton = new Button("Clear Results");

        navigationBox.getChildren().addAll(prevMatchButton, nextMatchButton, clearButton);

        // Export options
        HBox exportBox = new HBox(10);

        Label exportLabel = new Label("Export as:");
        exportFormatComboBox = new ComboBox<>();
        exportFormatComboBox.getItems().addAll("Text (.txt)", "CSV (.csv)", "HTML (.html)", "JSON (.json)");
        exportFormatComboBox.setValue("Text (.txt)");

        exportButton = new Button("Export");
        copyButton = new Button("Copy to Clipboard");

        exportBox.getChildren().addAll(exportLabel, exportFormatComboBox, exportButton, copyButton);

        // Status flow for messages
        statusFlow = new TextFlow();
        statusFlow.setPrefHeight(20);

        // Add all sections to bottom container
        bottomSection.getChildren().addAll(navigationBox, exportBox, statusFlow);

        return bottomSection;
    }

    /**
     * Set up event handlers for UI components
     */
    private void setupEventHandlers() {
        // Match list selection
        matchesList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        int index = matchesList.getSelectionModel().getSelectedIndex();
                        navigateToMatch(index);
                    }
                }
        );

        // Navigation buttons
        prevMatchButton.setOnAction(e -> navigateToPreviousMatch());
        nextMatchButton.setOnAction(e -> navigateToNextMatch());
        clearButton.setOnAction(e -> clearResults());

        // Export options
        exportButton.setOnAction(e -> exportResults());
        copyButton.setOnAction(e -> copyResultsToClipboard());
    }

    /**
     * Display text processing results
     * @param processedText The processed text to display
     */
    public void displayResults(String processedText) {
        resultsTextArea.setText(processedText);
        updateStatus("Results updated");
    }

    /**
     * Display matches found in text
     * @param matches List of matched strings
     */
    public void displayMatches(List<String> matches) {
        this.matches.clear();
        this.matches.addAll(matches);

        matchCountLabel.setText(matches.size() + " matches found");
        currentMatchIndex = -1;

        updateMatchNavigation();
        updateStatus(matches.size() + " matches found");
    }

    /**
     * Display matches with position information
     * @param matchesWithPositions List of Match objects with position info
     */
    public void displayMatchesWithPositions(List<RegexModel.Match> matchesWithPositions) {
        this.matchesWithPositions = matchesWithPositions;

        // Extract match text for the list
        this.matches.clear();
        for (RegexModel.Match match : matchesWithPositions) {
            this.matches.add(match.getText());
        }

        matchCountLabel.setText(matches.size() + " matches found");
        currentMatchIndex = -1;

        updateMatchNavigation();
        updateStatus(matches.size() + " matches found");
    }

    /**
     * Navigate to a specific match by index
     * @param index Index of the match to navigate to
     */
    private void navigateToMatch(int index) {
        if (matchesWithPositions != null && index >= 0 && index < matchesWithPositions.size()) {
            RegexModel.Match match = matchesWithPositions.get(index);
            currentMatchIndex = index;

            // Select the text in the results area
            resultsTextArea.selectRange(match.getStartPosition(), match.getEndPosition());
            resultsTextArea.requestFocus();

            // Update current match label
            currentMatchLabel.setText("Match " + (currentMatchIndex + 1) + " of " + matches.size());

            // Update navigation buttons
            updateMatchNavigation();
        }
    }

    /**
     * Navigate to the previous match
     */
    private void navigateToPreviousMatch() {
        if (currentMatchIndex > 0) {
            navigateToMatch(currentMatchIndex - 1);
            matchesList.getSelectionModel().select(currentMatchIndex);
            matchesList.scrollTo(currentMatchIndex);
        }
    }

    /**
     * Navigate to the next match
     */
    private void navigateToNextMatch() {
        if (currentMatchIndex < matches.size() - 1) {
            navigateToMatch(currentMatchIndex + 1);
            matchesList.getSelectionModel().select(currentMatchIndex);
            matchesList.scrollTo(currentMatchIndex);
        }
    }

    /**
     * Update match navigation buttons state
     */
    private void updateMatchNavigation() {
        if (matches.isEmpty()) {
            prevMatchButton.setDisable(true);
            nextMatchButton.setDisable(true);
            currentMatchLabel.setText("");
        } else {
            prevMatchButton.setDisable(currentMatchIndex <= 0);
            nextMatchButton.setDisable(currentMatchIndex >= matches.size() - 1);
        }
    }

    /**
     * Clear all results
     */
    private void clearResults() {
        resultsTextArea.clear();
        matches.clear();
        matchesWithPositions = null;
        currentMatchIndex = -1;
        matchCountLabel.setText("No matches");
        currentMatchLabel.setText("");
        updateMatchNavigation();
        updateStatus("Results cleared");
    }

    /**
     * Export results to a file
     */
    private void exportResults() {
        String content = resultsTextArea.getText();
        if (content == null || content.isEmpty()) {
            updateStatus("No content to export", true);
            return;
        }

        String selectedFormat = exportFormatComboBox.getValue();
        String fileExtension;
        String formattedContent = content;

        // Format the content based on the selected export format
        switch (selectedFormat) {
            case "CSV (.csv)":
                fileExtension = "csv";
                formattedContent = formatAsCSV(content);
                break;
            case "HTML (.html)":
                fileExtension = "html";
                formattedContent = formatAsHTML(content);
                break;
            case "JSON (.json)":
                fileExtension = "json";
                formattedContent = formatAsJSON(content);
                break;
            default:
                fileExtension = "txt";
                break;
        }

        // Create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Results");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        selectedFormat, "*." + fileExtension
                )
        );
        fileChooser.setInitialFileName("results." + fileExtension);

        // Show save dialog
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(formattedContent);
                updateStatus("Results exported to " + file.getName());
            } catch (IOException e) {
                updateStatus("Failed to export results: " + e.getMessage(), true);
            }
        }
    }

    /**
     * Copy results to clipboard
     */
    private void copyResultsToClipboard() {
        String content = resultsTextArea.getText();
        if (content == null || content.isEmpty()) {
            updateStatus("No content to copy", true);
            return;
        }

        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
        clipboardContent.putString(content);
        clipboard.setContent(clipboardContent);

        updateStatus("Results copied to clipboard");
    }

    /**
     * Format content as CSV
     * @param content Content to format
     * @return CSV formatted content
     */
    private String formatAsCSV(String content) {
        // Simple CSV formatting - can be enhanced
        StringBuilder csv = new StringBuilder("Index,Match\n");

        for (int i = 0; i < matches.size(); i++) {
            String match = matches.get(i);
            // Escape quotes and add quotes around fields with commas
            if (match.contains(",") || match.contains("\"")) {
                match = "\"" + match.replace("\"", "\"\"") + "\"";
            }
            csv.append(i + 1).append(",").append(match).append("\n");
        }

        return csv.toString();
    }

    /**
     * Format content as HTML
     * @param content Content to format
     * @return HTML formatted content
     */
    private String formatAsHTML(String content) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("  <title>Text Processing Results</title>\n")
                .append("  <style>\n")
                .append("    body { font-family: Arial, sans-serif; margin: 20px; }\n")
                .append("    table { border-collapse: collapse; width: 100%; }\n")
                .append("    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
                .append("    th { background-color: #f2f2f2; }\n")
                .append("    tr:nth-child(even) { background-color: #f9f9f9; }\n")
                .append("    .results { white-space: pre-wrap; margin: 20px 0; padding: 10px; border: 1px solid #ddd; }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("  <h1>Text Processing Results</h1>\n");

        // Add processed text section
        html.append("  <h2>Processed Text</h2>\n")
                .append("  <div class=\"results\">")
                .append(escapeHTML(content))
                .append("</div>\n");

        // Add matches section if available
        if (!matches.isEmpty()) {
            html.append("  <h2>Matches Found (").append(matches.size()).append(")</h2>\n")
                    .append("  <table>\n")
                    .append("    <tr><th>#</th><th>Match</th></tr>\n");

            for (int i = 0; i < matches.size(); i++) {
                html.append("    <tr>")
                        .append("<td>").append(i + 1).append("</td>")
                        .append("<td>").append(escapeHTML(matches.get(i))).append("</td>")
                        .append("</tr>\n");
            }

            html.append("  </table>\n");
        }

        html.append("</body>\n")
                .append("</html>");

        return html.toString();
    }

    /**
     * Format content as JSON
     * @param content Content to format
     * @return JSON formatted content
     */
    private String formatAsJSON(String content) {
        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("  \"processedText\": \"").append(escapeJSON(content)).append("\",\n")
                .append("  \"matchCount\": ").append(matches.size()).append(",\n")
                .append("  \"matches\": [\n");

        for (int i = 0; i < matches.size(); i++) {
            json.append("    {\n")
                    .append("      \"index\": ").append(i + 1).append(",\n")
                    .append("      \"text\": \"").append(escapeJSON(matches.get(i))).append("\"");

            // Add position info if available
            if (matchesWithPositions != null && i < matchesWithPositions.size()) {
                RegexModel.Match match = matchesWithPositions.get(i);
                json.append(",\n")
                        .append("      \"startPosition\": ").append(match.getStartPosition()).append(",\n")
                        .append("      \"endPosition\": ").append(match.getEndPosition());
            }

            json.append("\n    }");

            if (i < matches.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n")
                .append("}");

        return json.toString();
    }

    /**
     * Escape HTML special characters
     * @param text Text to escape
     * @return HTML escaped text
     */
    private String escapeHTML(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Escape JSON special characters
     * @param text Text to escape
     * @return JSON escaped text
     */
    private String escapeJSON(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Update the status message
     * @param message Message to display
     */
    private void updateStatus(String message) {
        updateStatus(message, false);
    }

    /**
     * Update the status message with optional error indication
     * @param message Message to display
     * @param isError Whether this is an error message
     */
    private void updateStatus(String message, boolean isError) {
        statusFlow.getChildren().clear();
        Text statusText = new Text(message);
        statusText.setFill(isError ? Color.RED : Color.BLACK);
        statusFlow.getChildren().add(statusText);
    }

    /**
     * Set the current text for the results area
     * @param text Text to set
     */
    public void setText(String text) {
        resultsTextArea.setText(text);
    }

    /**
     * Get the current text from the results area
     * @return Current text
     */
    public String getText() {
        return resultsTextArea.getText();
    }

    /**
     * Display replacement results
     * @param originalText Original text before replacement
     * @param processedText Processed text after replacement
     * @param replacementCount Number of replacements made
     */
    public void displayReplacementResults(String originalText, String processedText, int replacementCount) {
        resultsTextArea.setText(processedText);
        matchCountLabel.setText(replacementCount + " replacements made");
        currentMatchLabel.setText("");

        // Clear match lists since these are replacements, not matches
        matches.clear();
        matchesWithPositions = null;
        currentMatchIndex = -1;

        updateMatchNavigation();
        updateStatus(replacementCount + " replacements completed");
    }
}
