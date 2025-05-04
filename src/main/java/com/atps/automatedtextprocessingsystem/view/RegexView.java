package com.atps.automatedtextprocessingsystem.view;

import com.atps.automatedtextprocessingsystem.controller.RegexController;
import com.atps.automatedtextprocessingsystem.model.RegexModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.Optional;

/**
 * RegexView - UI component for regex operations
 * Provides interface for pattern matching, replacement, and management
 */
public class RegexView {
    private final RegexController regexController;
    private final VBox root;

    // UI Components
    private TextField patternField;
    private TextField replacementField;
    private Button findButton;
    private Button replaceButton;
    private Button highlightButton;
    private ComboBox<String> savedPatternsComboBox;
    private ListView<String> resultsListView;
    private TextFlow matchInfoTextFlow;
    private Label statusLabel;

    /**
     * Constructor - initializes the regex view
     * @param regexController Reference to the regex controller
     */
    public RegexView(RegexController regexController) {
        this.regexController = regexController;
        this.root = new VBox(10);
        this.root.setPadding(new Insets(10));

        initializeComponents();
        setupEventHandlers();
        loadSavedPatterns();
    }

    /**
     * Get the root pane of the regex view
     * @return The VBox containing the regex components
     */
    public Pane getRoot() {
        return root;
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Pattern input section
        Label patternLabel = new Label("Regex Pattern:");
        patternField = new TextField();
        patternField.setPromptText("Enter regex pattern");

        // Pattern validation feedback
        statusLabel = new Label();
        statusLabel.setTextFill(Color.GRAY);

        // Saved patterns section
        Label savedPatternsLabel = new Label("Saved Patterns:");
        savedPatternsComboBox = new ComboBox<>();
        savedPatternsComboBox.setPromptText("Select a saved pattern");
        savedPatternsComboBox.setPrefWidth(200);

        Button savePatternButton = new Button("Save");
        Button deletePatternButton = new Button("Delete");

        HBox patternButtonsBox = new HBox(10, savePatternButton, deletePatternButton);

        // Replacement section
        Label replacementLabel = new Label("Replacement:");
        replacementField = new TextField();
        replacementField.setPromptText("Enter replacement text");

        // Action buttons
        findButton = new Button("Find");
        replaceButton = new Button("Replace");

        HBox actionButtonsBox = new HBox(10, findButton, replaceButton);

        // Results section
        Label resultsLabel = new Label("Results:");
        resultsListView = new ListView<>();
        resultsListView.setPrefHeight(100);

        matchInfoTextFlow = new TextFlow();
        matchInfoTextFlow.setPrefHeight(100);
        matchInfoTextFlow.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ddd; -fx-padding: 5px;");

        // Add components to root
        root.getChildren().addAll(
                patternLabel, patternField, statusLabel,
                new Separator(),
                savedPatternsLabel, savedPatternsComboBox, patternButtonsBox,
                new Separator(),
                replacementLabel, replacementField,
                actionButtonsBox,
                new Separator(),
                resultsLabel, resultsListView,
                matchInfoTextFlow
        );
    }

    /**
     * Set up event handlers for UI components
     */
    private void setupEventHandlers() {
        // Pattern field validation
        patternField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePattern(newValue);
        });

        // Saved patterns selection
        savedPatternsComboBox.setOnAction(e -> {
            String selectedPattern = savedPatternsComboBox.getValue();
            if (selectedPattern != null && !selectedPattern.isEmpty()) {
                String pattern = regexController.getSavedPattern(selectedPattern);
                if (pattern != null) {
                    patternField.setText(pattern);
                }
            }
        });

        // Save pattern button
        savedPatternsComboBox.getItems().addListener((ListChangeListener<String>) change -> {
            if (savedPatternsComboBox.getItems().isEmpty()) {
                savedPatternsComboBox.setPromptText("No saved patterns");
            } else {
                savedPatternsComboBox.setPromptText("Select a saved pattern");
            }
        });


        // Save pattern dialog
        Button saveButton = (Button) root.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .flatMap(hbox -> ((HBox) hbox).getChildren().stream())
                .filter(node -> node instanceof Button && ((Button) node).getText().equals("Save"))
                .findFirst()
                .orElse(null);

        if (saveButton != null) {
            saveButton.setOnAction(e -> showSavePatternDialog());
        }

        // Delete pattern button
        Button deleteButton = (Button) root.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .flatMap(hbox -> ((HBox) hbox).getChildren().stream())
                .filter(node -> node instanceof Button && ((Button) node).getText().equals("Delete"))
                .findFirst()
                .orElse(null);

        if (deleteButton != null) {
            deleteButton.setOnAction(e -> {
                String selectedPattern = savedPatternsComboBox.getValue();
                if (selectedPattern != null && !selectedPattern.isEmpty()) {
                    boolean removed = regexController.removeSavedPattern(selectedPattern);
                    if (removed) {
                        loadSavedPatterns();
                        statusLabel.setText("Pattern '" + selectedPattern + "' removed");
                        statusLabel.setTextFill(Color.GREEN);
                    }
                }
            });
        }

        // Find button
        findButton.setOnAction(e -> {
            String pattern = patternField.getText();
            if (!pattern.isEmpty() && regexController.isValidPattern(pattern)) {
                regexController.findMatches(pattern, this::displayMatches);
            }
        });

        // Replace button
        replaceButton.setOnAction(e -> {
            String pattern = patternField.getText();
            String replacement = replacementField.getText();
            if (!pattern.isEmpty() && regexController.isValidPattern(pattern)) {
                regexController.replaceText(pattern, replacement, count -> {
                    statusLabel.setText("Replaced " + count + " occurrences");
                    statusLabel.setTextFill(Color.GREEN);
                });
            }
        });

        // Result item selection
        resultsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        showMatchDetails(newValue);
                    }
                }
        );
    }

    /**
     * Display matches in the results list
     * @param matches List of matched strings
     */
    private void displayMatches(List<String> matches) {
        resultsListView.setItems(FXCollections.observableArrayList(matches));
        updateMatchInfo(matches.size() + " matches found");
    }

    /**
     * Display matches with position information
     * @param matches List of Match objects with position info
     */
    private void displayMatchesWithPositions(List<RegexModel.Match> matches) {
        // Extract just the match text for the list view
        List<String> matchTexts = matches.stream()
                .map(RegexModel.Match::getText)
                .toList();

        resultsListView.setItems(FXCollections.observableArrayList(matchTexts));

        StringBuilder details = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            RegexModel.Match match = matches.get(i);
            details.append("Match ").append(i + 1)
                    .append(": '").append(match.getText()).append("'")
                    .append(" at position ").append(match.getStartPosition())
                    .append("-").append(match.getEndPosition())
                    .append("\n");
        }

        updateMatchInfo(matches.size() + " matches found");
    }

    /**
     * Show dialog to save the current pattern
     */
    private void showSavePatternDialog() {
        String pattern = patternField.getText();
        if (pattern.isEmpty() || !regexController.isValidPattern(pattern)) {
            statusLabel.setText("Cannot save invalid pattern");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Pattern");
        dialog.setHeaderText("Save Regular Expression Pattern");
        dialog.setContentText("Enter a name for this pattern:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.isEmpty()) {
                boolean saved = regexController.saveRegexPattern(name, pattern);
                if (saved) {
                    loadSavedPatterns();
                    savedPatternsComboBox.setValue(name);
                    statusLabel.setText("Pattern '" + name + "' saved");
                    statusLabel.setTextFill(Color.GREEN);
                }
            }
        });
    }

    /**
     * Load saved patterns into the combo box
     */
    private void loadSavedPatterns() {
        List<String> patternNames = regexController.getSavedPatternNames();
        savedPatternsComboBox.setItems(FXCollections.observableArrayList(patternNames));
    }

    /**
     * Validate the current pattern
     * @param pattern Pattern to validate
     */
    private void validatePattern(String pattern) {
        if (pattern.isEmpty()) {
            statusLabel.setText("");
            return;
        }

        boolean isValid = regexController.isValidPattern(pattern);
        if (isValid) {
            statusLabel.setText("Valid pattern");
            statusLabel.setTextFill(Color.GREEN);
            findButton.setDisable(false);
            replaceButton.setDisable(false);
        } else {
            statusLabel.setText("Invalid pattern");
            statusLabel.setTextFill(Color.RED);
            findButton.setDisable(true);
            replaceButton.setDisable(true);
        }
    }

    /**
     * Update the match info section
     * @param info Information to display
     */
    private void updateMatchInfo(String info) {
        matchInfoTextFlow.getChildren().clear();
        Text text = new Text(info);
        matchInfoTextFlow.getChildren().add(text);
    }

    /**
     * Show details about a selected match
     * @param match Match text
     */
    private void showMatchDetails(String match) {
        if (match != null && !match.isEmpty()) {
            String details = "Selected match: '" + match + "'\n" +
                    "Length: " + match.length() + " characters";
            updateMatchInfo(details);
        }
    }

    /**
     * Show the find dialog
     */
    public void showFindDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Find");
        dialog.setHeaderText("Find Text using Regular Expressions");

        // Create the find dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label patternLabel = new Label("Regex Pattern:");
        TextField patternField = new TextField(this.patternField.getText());

        content.getChildren().addAll(patternLabel, patternField);
        dialog.getDialogPane().setContent(content);

        ButtonType findButtonType = new ButtonType("Find", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(findButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == findButtonType) {
                String pattern = patternField.getText();
                if (!pattern.isEmpty() && regexController.isValidPattern(pattern)) {
                    this.patternField.setText(pattern);
                    regexController.findMatches(pattern, this::displayMatches);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show the replace dialog
     */
    public void showReplaceDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Replace");
        dialog.setHeaderText("Replace Text using Regular Expressions");

        // Create the replace dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label patternLabel = new Label("Find Pattern:");
        TextField patternField = new TextField(this.patternField.getText());

        Label replacementLabel = new Label("Replace With:");
        TextField replacementField = new TextField(this.replacementField.getText());

        content.getChildren().addAll(patternLabel, patternField, replacementLabel, replacementField);
        dialog.getDialogPane().setContent(content);

        ButtonType replaceButtonType = new ButtonType("Replace", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(replaceButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == replaceButtonType) {
                String pattern = patternField.getText();
                String replacement = replacementField.getText();

                if (!pattern.isEmpty() && regexController.isValidPattern(pattern)) {
                    this.patternField.setText(pattern);
                    this.replacementField.setText(replacement);
                    regexController.replaceText(pattern, replacement, count -> {
                        statusLabel.setText("Replaced " + count + " occurrences");
                        statusLabel.setTextFill(Color.GREEN);
                    });
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show dialog to save the current pattern
     */
    public void savePattern() {
        showSavePatternDialog();
    }

    /**
     * Show dialog to load a pattern
     */
    public void loadPattern() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Load Pattern");
        dialog.setHeaderText("Load Saved Regular Expression Pattern");

        // Create the content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        ListView<String> patternsListView = new ListView<>();
        patternsListView.setItems(FXCollections.observableArrayList(regexController.getSavedPatternNames()));
        patternsListView.setPrefHeight(200);

        content.getChildren().add(patternsListView);
        dialog.getDialogPane().setContent(content);

        ButtonType loadButtonType = new ButtonType("Load", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loadButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loadButtonType) {
                return patternsListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(patternName -> {
            String pattern = regexController.getSavedPattern(patternName);
            if (pattern != null) {
                patternField.setText(pattern);
                savedPatternsComboBox.setValue(patternName);
            }
        });
    }
}