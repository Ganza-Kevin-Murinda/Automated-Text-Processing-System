package com.atps.automatedtextprocessingsystem.view;

import com.atps.automatedtextprocessingsystem.controller.TextProcessingController;
import com.atps.automatedtextprocessingsystem.model.DataModel;
import com.atps.automatedtextprocessingsystem.service.TextProcessingService;
import com.atps.automatedtextprocessingsystem.util.TextProcessingUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TextRecordView - Manages display and operations for text records
 * Provides functionality to view, create, edit, analyze and search text records
 */
public class TextRecordView {

    private final TextProcessingController textProcessingController;
    private final TextProcessingService service;
    private final VBox root;
    private TableView<DataModel.TextRecord> recordTable;
    private ObservableList<DataModel.TextRecord> recordsList;

    // UI Components
    private TextArea previewArea;
    private Label statusLabel;


    /**
     * Constructor
     * @param textProcessingController Controller for text processing operations
     */
    public TextRecordView(TextProcessingController textProcessingController) {
        this.textProcessingController = textProcessingController;
        this.service = textProcessingController.getService();
        this.root = new VBox(10);
        this.root.setPadding(new Insets(10));

        initializeComponents();
        refreshRecordsList();
    }


    /**
     * Get the root pane
     * @return VBox containing the entire view
     */
    public VBox getRoot() {
        return root;
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Create the toolbar with record operations
        HBox toolbar = createToolbar();

        // Create the record table
        recordTable = createRecordTable();

        // Create the preview area with a split pane
        SplitPane splitPane = createPreviewArea();

        // Create status bar
        HBox statusBar = createStatusBar();

        // Add all components to the root pane
        root.getChildren().addAll(toolbar, new Separator(), recordTable, splitPane, statusBar);
        VBox.setVgrow(recordTable, Priority.ALWAYS);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
    }

    /**
     * Create toolbar with record operations
     * @return HBox containing toolbar buttons
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));

        Button newButton = new Button("New Record");
        newButton.setOnAction(e -> showNewRecordDialog());

        Button editButton = new Button("Edit Record");
        editButton.setOnAction(e -> editSelectedRecord());

        Button deleteButton = new Button("Delete Record");
        deleteButton.setOnAction(e -> deleteSelectedRecord());

        Button loadButton = new Button("Load to Editor");
        loadButton.setOnAction(e -> loadSelectedRecordToEditor());

        Button analyzeButton = new Button("Analyze");
        analyzeButton.setOnAction(e -> analyzeSelectedRecord());

        TextField searchField = new TextField();
        searchField.setPromptText("Search patterns...");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchRecords(searchField.getText()));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshRecordsList());

        toolbar.getChildren().addAll(newButton, editButton, deleteButton, loadButton,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                analyzeButton, new Separator(javafx.geometry.Orientation.VERTICAL),
                searchField, searchButton, refreshButton);

        return toolbar;
    }

    /**
     * Create the record table
     * @return TableView for text records
     */
    private TableView<DataModel.TextRecord> createRecordTable() {
        recordsList = FXCollections.observableArrayList();
        TableView<DataModel.TextRecord> table = new TableView<>(recordsList);

        // ID Column
        TableColumn<DataModel.TextRecord, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()));
        idColumn.setPrefWidth(50);

        // Name Column
        TableColumn<DataModel.TextRecord, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(200);

        // Source Column
        TableColumn<DataModel.TextRecord, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSource()));
        sourceColumn.setPrefWidth(150);

        // Content Column
        TableColumn<DataModel.TextRecord, String> contentColumn = new TableColumn<>("Content");
        sourceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getContent()));
        sourceColumn.setPrefWidth(150);

        // Created Date Column
        TableColumn<DataModel.TextRecord, String> dateColumn = new TableColumn<>("Created Date");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreationDate().format(formatter)));
        dateColumn.setPrefWidth(150);


        // Size Column (character count)
        TableColumn<DataModel.TextRecord, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(cellData -> {
            int charCount = cellData.getValue().getContent().length();
            return new SimpleStringProperty(formatSize(charCount));
        });
        sizeColumn.setPrefWidth(100);

        table.getColumns().addAll(idColumn, nameColumn, sourceColumn, contentColumn, dateColumn, sizeColumn);

        // Add selection listener to update the preview when a record is selected
        table.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updatePreview(newValue));

        // Add context menu
        table.setContextMenu(createContextMenu());

        return table;
    }

    /**
     * Create the preview area
     * @return SplitPane containing preview components
     */
    private SplitPane createPreviewArea() {
        SplitPane splitPane = new SplitPane();

        // Preview text area
        previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setWrapText(true);
        previewArea.setPromptText("Select a record to preview its content");

        VBox previewBox = new VBox(5);
        previewBox.setPadding(new Insets(5));
        Label previewLabel = new Label("Preview:");
        previewBox.getChildren().addAll(previewLabel, previewArea);
        VBox.setVgrow(previewArea, Priority.ALWAYS);

        // Add to split pane
        splitPane.getItems().add(previewBox);

        return splitPane;
    }

    /**
     * Create the status bar
     * @return HBox containing status elements
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        statusLabel = new Label("Ready");
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label countLabel = new Label("Records: 0");

        // Update count label when records change
        recordsList.addListener((javafx.collections.ListChangeListener.Change<? extends DataModel.TextRecord> c) -> {
            countLabel.setText("Records: " + recordsList.size());
        });

        statusBar.getChildren().addAll(statusLabel, spacer, countLabel);

        return statusBar;
    }

    /**
     * Create context menu for the table
     * @return ContextMenu for record operations
     */
    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewItem = new MenuItem("View Full Content");
        viewItem.setOnAction(e -> viewFullContent());

        MenuItem loadItem = new MenuItem("Load to Editor");
        loadItem.setOnAction(e -> loadSelectedRecordToEditor());

        MenuItem editItem = new MenuItem("Edit Record");
        editItem.setOnAction(e -> editSelectedRecord());

        MenuItem deleteItem = new MenuItem("Delete Record");
        deleteItem.setOnAction(e -> deleteSelectedRecord());

        MenuItem analyzeItem = new MenuItem("Analyze Content");
        analyzeItem.setOnAction(e -> analyzeSelectedRecord());

        contextMenu.getItems().addAll(viewItem, loadItem, editItem, deleteItem,
                new SeparatorMenuItem(), analyzeItem);

        return contextMenu;
    }

    /**
     * Refresh the records list from the service
     */
    public void refreshRecordsList() {
        List<DataModel.TextRecord> records = textProcessingController.getAllTextRecords();
        recordsList.setAll(records);
        updateStatus("Records refreshed. Total: " + records.size());
    }

    /**
     * Update the preview area with selected record content
     * @param record The selected record
     */
    private void updatePreview(DataModel.TextRecord record) {
        if (record != null) {
            // Show a preview (first 1000 chars)
            String content = record.getContent();
            String preview = content.length() > 1000 ?
                    content.substring(0, 1000) + "... (content truncated, view full content for more)" :
                    content;
            previewArea.setText(preview);
        } else {
            previewArea.setText("");
        }
    }

    /**
     * View full content of the selected record
     */
    private void viewFullContent() {
        DataModel.TextRecord selectedRecord = recordTable.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Record Selected", "Please select a record to view.");
            return;
        }

        // Create a dialog with scrollable text area
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Content: " + selectedRecord.getName());
        dialog.setHeaderText("ID: " + selectedRecord.getId() + ", Source: " + selectedRecord.getSource());

        TextArea contentArea = new TextArea(selectedRecord.getContent());
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefWidth(800);
        contentArea.setPrefHeight(600);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(contentArea);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    /**
     * Show dialog to create a new record
     */
    private void showNewRecordDialog() {
        // Create the custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Text Record");
        dialog.setHeaderText("Enter record details:");

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Record name");

        TextField sourceField = new TextField();
        sourceField.setPromptText("Source (optional)");

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter content here");
        contentArea.setPrefHeight(300);

        // Add fields to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Source:"), 0, 1);
        grid.add(sourceField, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);

        // Set the grid in the dialog
        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Show dialog and process result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == createButtonType) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        "Name is required", "Please enter a name for the record.");
                return;
            }

            String source = sourceField.getText().trim();
            String content = contentArea.getText();

            // Create the record
            int recordId = textProcessingController.saveAsTextRecord(name, content, source);
            updateStatus("Created new record with ID: " + recordId);
            refreshRecordsList();
        }
    }

    /**
     * Edit the selected record
     */
    private void editSelectedRecord() {
        DataModel.TextRecord selectedRecord = recordTable.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Record Selected", "Please select a record to edit.");
            return;
        }

        // Create the edit dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Text Record");
        dialog.setHeaderText("Edit record ID: " + selectedRecord.getId());

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedRecord.getName());
        TextArea contentArea = new TextArea(selectedRecord.getContent());
        contentArea.setPrefHeight(300);

        // Add fields to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentArea, 1, 1);

        // Set the grid in the dialog
        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Show dialog and process result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        "Name is required", "Please enter a name for the record.");
                return;
            }

            String content = contentArea.getText();

            // Update the record
            boolean success = service.updateTextRecord(
                    selectedRecord.getId(), name, content);

            if (success) {
                updateStatus("Updated record ID: " + selectedRecord.getId());
                refreshRecordsList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed",
                        "Failed to update record", "An error occurred while updating the record.");
            }
        }
    }

    /**
     * Delete the selected record
     */
    private void deleteSelectedRecord() {
        DataModel.TextRecord selectedRecord = recordTable.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Record Selected", "Please select a record to delete.");
            return;
        }

        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Record ID: " + selectedRecord.getId());
        confirmDialog.setContentText("Are you sure you want to delete this record? This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = service.removeTextRecord(selectedRecord.getId());

            if (success) {
                updateStatus("Deleted record ID: " + selectedRecord.getId());
                refreshRecordsList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Delete Failed",
                        "Failed to delete record", "An error occurred while deleting the record.");
            }
        }
    }

    /**
     * Load the selected record to the text editor
     */
    private void loadSelectedRecordToEditor() {
        DataModel.TextRecord selectedRecord = recordTable.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Record Selected", "Please select a record to load.");
            return;
        }

        boolean success = textProcessingController.loadTextRecord(selectedRecord.getId());

        if (success) {
            updateStatus("Loaded record ID: " + selectedRecord.getId() + " to editor");
        } else {
            showAlert(Alert.AlertType.ERROR, "Load Failed",
                    "Failed to load record", "An error occurred while loading the record.");
        }
    }

    /**
     * Analyze the selected record
     */
    private void analyzeSelectedRecord() {
        DataModel.TextRecord selectedRecord = recordTable.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Record Selected", "Please select a record to analyze.");
            return;
        }

        // Get word frequency data
        Map<String, Integer> wordFrequency = textProcessingController.analyzeWordFrequency(selectedRecord.getId());
        List<Map.Entry<String, Integer>> topWords = textProcessingController.getMostFrequentWords(selectedRecord.getId(), 20);

        // Calculate text statistics using TextProcessingUtils
        TextProcessingUtils.TextStatistics stats =
                TextProcessingUtils.getTextStatistics(selectedRecord.getContent());

        // Create the analysis dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Text Analysis");
        dialog.setHeaderText("Analysis of record: " + selectedRecord.getName());

        TabPane tabPane = new TabPane();

        // Statistics tab
        Tab statsTab = new Tab("Statistics");
        statsTab.setClosable(false);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(5);
        statsGrid.setPadding(new Insets(10));

        int row = 0;
        statsGrid.add(new Label("Word Count:"), 0, row);
        statsGrid.add(new Label(String.valueOf(stats.getWordCount())), 1, row++);

        statsGrid.add(new Label("Character Count:"), 0, row);
        statsGrid.add(new Label(String.valueOf(stats.getCharacterCount())), 1, row++);

        statsGrid.add(new Label("Line Count:"), 0, row);
        statsGrid.add(new Label(String.valueOf(stats.getSentenceCount())), 1, row++);

        statsGrid.add(new Label("Paragraph Count:"), 0, row);
        statsGrid.add(new Label(String.valueOf(stats.getParagraphCount())), 1, row++);

        statsTab.setContent(statsGrid);

        // Word frequency tab
        Tab freqTab = new Tab("Word Frequency");
        freqTab.setClosable(false);

        VBox freqBox = new VBox(10);
        freqBox.setPadding(new Insets(10));

        Label topWordsLabel = new Label("Top 20 Most Frequent Words:");

        // Create table for top words
        TableView<Map.Entry<String, Integer>> topWordsTable = new TableView<>();

        TableColumn<Map.Entry<String, Integer>, String> wordColumn = new TableColumn<>("Word");
        wordColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Integer>, Number> freqColumn = new TableColumn<>("Frequency");
        freqColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getValue()));

        topWordsTable.getColumns().addAll(wordColumn, freqColumn);
        topWordsTable.setItems(FXCollections.observableArrayList(topWords));

        freqBox.getChildren().addAll(topWordsLabel, topWordsTable);
        VBox.setVgrow(topWordsTable, Priority.ALWAYS);

        freqTab.setContent(freqBox);

        // Add tabs to tab pane
        tabPane.getTabs().addAll(statsTab, freqTab);

        // Set tab pane in dialog
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefSize(500, 400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    /**
     * Search across all records using a regex pattern
     * @param pattern Regex pattern to search for
     */
    private void searchRecords(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Pattern",
                    "Empty Search Pattern", "Please enter a search pattern.");
            return;
        }

        try {
            Map<Integer, List<String>> results = textProcessingController.searchAcrossRecords(pattern);

            if (results.isEmpty()) {
                updateStatus("No matches found for pattern: " + pattern);
                return;
            }

            // Create search results dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Search Results");
            dialog.setHeaderText("Results for pattern: " + pattern);

            VBox resultsBox = new VBox(10);
            resultsBox.setPadding(new Insets(10));

            for (Map.Entry<Integer, List<String>> entry : results.entrySet()) {
                int recordId = entry.getKey();
                List<String> matches = entry.getValue();

                // Find record name
                String recordName = "Unknown";
                for (DataModel.TextRecord record : recordsList) {
                    if (record.getId() == recordId) {
                        recordName = record.getName();
                        break;
                    }
                }

                TitledPane recordPane = new TitledPane(
                        "Record ID: " + recordId + " - " + recordName + " (" + matches.size() + " matches)",
                        createMatchesList(matches, recordId));
                resultsBox.getChildren().add(recordPane);
            }

            ScrollPane scrollPane = new ScrollPane(resultsBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(500);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().setPrefWidth(700);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            updateStatus("Found matches in " + results.size() + " records for pattern: " + pattern);
            dialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search Error",
                    "Error searching records", "Error: " + e.getMessage());
        }
    }

    /**
     * Create a list view of matches for search results
     * @param matches List of matched strings
     * @param recordId ID of the record
     * @return ListView of matches
     */
    private ListView<String> createMatchesList(List<String> matches, int recordId) {
        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(matches));
        listView.setPrefHeight(Math.min(matches.size() * 24 + 2, 200));

        // Add context menu to load record
        ContextMenu menu = new ContextMenu();
        MenuItem loadItem = new MenuItem("Load This Record");
        loadItem.setOnAction(e -> {
            textProcessingController.loadTextRecord(recordId);
            updateStatus("Loaded record ID: " + recordId + " to editor");
        });
        menu.getItems().add(loadItem);

        listView.setContextMenu(menu);

        return listView;
    }

    /**
     * Format file size for display
     * @param size Size in characters
     * @return Formatted size string
     */
    private String formatSize(int size) {
        if (size < 1000) {
            return size + " chars";
        } else if (size < 1000000) {
            return String.format("%.1f KB", size / 1000.0);
        } else {
            return String.format("%.1f MB", size / 1000000.0);
        }
    }

    /**
     * Update status message
     * @param message Status message to display
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
        TextProcessingUtils.logInfo(message);
    }

    /**
     * Show alert dialog
     * @param type Alert type
     * @param title Dialog title
     * @param header Header text
     * @param content Content text
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}