package com.simulation;

import com.simulation.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main application class for the Cloud Computing Service Queue Simulation
 *
 * This application simulates a cloud computing service provider where users
 * submit computational tasks (CPU or GPU types). The system handles three
 * types of users with different priorities:
 * - Normal users (lowest priority)
 * - Personal VIP users (medium priority)
 * - Enterprise VIP users (highest priority)
 *
 * The simulation follows the task flow:
 * Arrival → Data Storage → Classification → CPU/GPU Queue → Compute → Result Storage → Exit
 *
 * The system implements a discrete event simulation using the MVC pattern:
 * - Model: SimulationEngine, Task, ServicePoint, etc.
 * - View: FXML-based JavaFX UI
 * - Controller: SimulationController
 *
 * Features:
 * - JavaFX 20.0.1 with FXML and Scene Builder support
 * - MariaDB database integration for configuration and results storage
 * - HikariCP connection pooling
 * - Real-time visualization and statistics
 *
 * @author Cloud Simulation Team
 * @version 2.0
 */
public class SimulatorApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/simulation_view.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root);

            // Configure stage
            primaryStage.setTitle("☁️ Cloud Computing Service Queue Simulation");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1280);
            primaryStage.setMinHeight(750);

            // Set application icon (optional)
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

            // Handle window close event
            primaryStage.setOnCloseRequest(event -> {
                shutdown();
            });

            primaryStage.show();

            System.out.println("=".repeat(60));
            System.out.println("Cloud Computing Service Queue Simulation Started");
            System.out.println("JavaFX Version: " + System.getProperty("javafx.version"));
            System.out.println("Database: MariaDB with HikariCP");
            System.out.println("=".repeat(60));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        shutdown();
    }

    /**
     * Cleanup resources before application shutdown
     */
    private void shutdown() {
        System.out.println("Shutting down application...");
        DatabaseManager.getInstance().shutdown();
        System.out.println("Database connection pool closed.");
    }

    /**
     * Main entry point for the application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

