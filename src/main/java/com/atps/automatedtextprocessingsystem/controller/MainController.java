package com.atps.automatedtextprocessingsystem.controller;

import com.atps.automatedtextprocessingsystem.service.TextProcessingService;
import com.atps.automatedtextprocessingsystem.util.TextProcessingUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MainController - Coordinates overall application flow
 * Acts as the central controller that initializes and manages other controllers
 * and handles application-wide operations.
 */
public class MainController {

    private final TextProcessingService service;
    private final TextProcessingController textProcessingController;
    private final FileController fileController;
    private final RegexController regexController;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);


    private Stage primaryStage;
    private boolean hasUnsavedChanges = false;

    /**
     * Constructor - initializes the service and all sub-controllers
     */
    public MainController() {
        // Initialize the service
        this.service = new TextProcessingService();

        // Initialize sub-controllers and pass service and this controller reference
        this.textProcessingController = new TextProcessingController(service, this, executorService);
        this.fileController = new FileController(service, this, executorService);
        this.regexController = new RegexController(service, this, executorService);

        TextProcessingUtils.logInfo("Application initialized");
    }

    /**
     * Set the primary stage reference
     * @param primaryStage JavaFX primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Set up window close event handler to check for unsaved changes
        primaryStage.setOnCloseRequest(event -> {
            if (hasUnsavedChanges) {
                boolean shouldClose = showUnsavedChangesDialog();
                if (!shouldClose) {
                    event.consume(); // Prevent the window from closing
                }
            }
        });
    }

    /**
     * Get the primary stage
     * @return The primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Get the text processing controller
     * @return TextProcessingController instance
     */
    public TextProcessingController getTextProcessingController() {
        return textProcessingController;
    }

    /**
     * Get the file controller
     * @return FileController instance
     */
    public FileController getFileController() {
        return fileController;
    }

    /**
     * Get the regex controller
     * @return RegexController instance
     */
    public RegexController getRegexController() {
        return regexController;
    }

    /**
     * Initialize the application views and controllers
     */
    public void initializeApplication() {
        // Initialize components, can be used to restore previous state if needed
        TextProcessingUtils.logInfo("Application views and controllers initialized");
    }

    /**
     * Set the unsaved changes flag
     * @param hasUnsavedChanges true if there are unsaved changes
     */
    public void setHasUnsavedChanges(boolean hasUnsavedChanges) {
        this.hasUnsavedChanges = hasUnsavedChanges;
        updateWindowTitle();
    }

    /**
     * Check if there are unsaved changes
     * @return true if there are unsaved changes
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Update the window title to reflect unsaved changes
     */
    private void updateWindowTitle() {
        if (primaryStage != null) {
            String title = "Automated Text Processing System";
            if (hasUnsavedChanges) {
                title += " *";
            }
            primaryStage.setTitle(title);
        }
    }

    /**
     * Show a dialog when there are unsaved changes
     * @return true, if it's OK to proceed, false to cancel
     */
    public boolean showUnsavedChangesDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes");
        alert.setContentText("Do you want to save your changes before closing?");

        ButtonType buttonTypeSave = new ButtonType("Save");
        ButtonType buttonTypeDiscard = new ButtonType("Discard");
        ButtonType buttonTypeCancel = ButtonType.CANCEL;

        alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDiscard, buttonTypeCancel);

        var result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == buttonTypeSave) {
                // Try to save and return true if successful
                return fileController.saveCurrentText();
            } else {
                // Return true for Discard (proceed without saving), false for Cancel
                return result.get() == buttonTypeDiscard;
            }
        }

        return false;
    }

    /**
     * Show an error dialog to the user
     * @param title Error title
     * @param header Error header text
     * @param content Error content details
     */
    public void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show an information dialog to the user
     * @param title Dialog title
     * @param header Dialog header text
     * @param content Dialog content details
     */
    public void showInfoDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Exit the application after checking for unsaved changes
     */
    public void exitApplication() {
        if (hasUnsavedChanges) {
            boolean shouldExit = showUnsavedChangesDialog();
            if (!shouldExit) {
                return;
            }
        }

        TextProcessingUtils.logInfo("Application shutting down");
        primaryStage.close();
    }

    /**
     * Gracefully shut down the application (used from Application.stop)
     */
    public void shutdown() {
        TextProcessingUtils.logInfo("Shutting down background services");
        executorService.shutdownNow();
    }

}
