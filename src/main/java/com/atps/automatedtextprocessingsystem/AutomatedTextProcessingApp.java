package com.atps.automatedtextprocessingsystem;

import com.atps.automatedtextprocessingsystem.controller.MainController;
import com.atps.automatedtextprocessingsystem.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for the Automated Text Processing System
 * Initializes the JavaFX application, controllers, and main view
 */
public class AutomatedTextProcessingApp extends Application {

    private MainController mainController;

    /**
     * The main entry point for the JavaFX application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialize and start the JavaFX application
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize the main controller
            mainController = new MainController();
            mainController.setPrimaryStage(primaryStage);

            // Initialize the main view
            MainView mainView = new MainView(mainController);

            // Create the main scene
            Scene scene = new Scene(mainView.getRoot(), 1024, 830);

            // Set up the primary stage
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setTitle("Automated Text Processing System");

            // Initialize the application components
            mainController.initializeApplication();

            // Show the primary stage
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the application should stop, used for cleanup
     */
    @Override
    public void stop() {
        if (mainController != null) {
            mainController.shutdown();
        }
    }
}