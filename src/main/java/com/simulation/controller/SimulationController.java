package com.simulation.controller;

import com.simulation.database.DatabaseManager;
import com.simulation.database.SimulationConfigDAO;
import com.simulation.database.SimulationResultsDAO;
import com.simulation.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * FXML Controller that mediates between View and Model
 * Handles UI events and database operations
 */
public class SimulationController {
    // FXML Components
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button stopButton;
    @FXML private Button resetButton;

    @FXML private Label timeLabel;
    @FXML private Label speedLabel;
    @FXML private Label arrivedTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label avgSystemTimeLabel;
    @FXML private Label throughputLabel;
    @FXML private Label queueStatsLabel;
    @FXML private Label userTypeStatsLabel;
    @FXML private Label taskTypeStatsLabel;
    @FXML private Label dbStatusLabel;

    @FXML private TextField arrivalIntervalField;
    @FXML private TextField simulationTimeField;
    @FXML private TextField cpuNodesField;
    @FXML private TextField gpuNodesField;
    @FXML private TextField cpuProbabilityField;

    @FXML private Slider speedSlider;
    @FXML private Canvas visualizationCanvas;

    // Model components
    private SimulationEngine engine;
    private final SimulationConfig config;

    // Database components
    private final DatabaseManager dbManager;
    private final SimulationConfigDAO configDAO;
    private final SimulationResultsDAO resultsDAO;
    private Integer currentRunId;

    public SimulationController() {
        this.config = new SimulationConfig();
        this.dbManager = DatabaseManager.getInstance();
        this.configDAO = new SimulationConfigDAO();
        this.resultsDAO = new SimulationResultsDAO();
    }

    /**
     * Initialize method called after FXML loading
     */
    @FXML
    public void initialize() {
        // Setup speed slider listener
        if (speedSlider != null) {
            speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                speedLabel.setText(String.format("%.1fx", newVal.doubleValue()));
                if (engine != null) {
                    engine.setSpeed(newVal.doubleValue());
                }
            });
        }

        // Test database connection
        testDatabaseConnection();

        // Initialize canvas
        if (visualizationCanvas != null) {
            drawInitialCanvas();
        }
    }

    // ========== FXML Event Handlers ==========

    @FXML
    private void handleStart() {
        updateConfigFromUI();
        initializeSimulation();

        // Create database run record
        try {
            currentRunId = resultsDAO.createSimulationRun(null,
                "Simulation Run " + System.currentTimeMillis());
        } catch (SQLException e) {
            showError("Database Error", "Failed to create run record: " + e.getMessage());
        }

        startSimulation();
        updateButtonStates(true);
    }

    @FXML
    private void handlePause() {
        pauseSimulation();
        pauseButton.setDisable(true);
        resumeButton.setDisable(false);
    }

    @FXML
    private void handleResume() {
        resumeSimulation();
        pauseButton.setDisable(false);
        resumeButton.setDisable(true);
    }

    @FXML
    private void handleStop() {
        stopSimulation();

        // Save results to database
        if (currentRunId != null) {
            saveResultsToDatabase();
        }

        updateButtonStates(false);
    }

    @FXML
    private void handleReset() {
        stopSimulation();
        initializeSimulation();
        updateStatistics();
        updateButtonStates(false);
    }

    @FXML
    private void handleSaveConfig() {
        TextInputDialog dialog = new TextInputDialog("My Configuration");
        dialog.setTitle("Save Configuration");
        dialog.setHeaderText("Save current configuration to database");
        dialog.setContentText("Configuration Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                updateConfigFromUI();
                configDAO.saveConfig(config, name);
                showInfo("Success", "Configuration saved: " + name);
            } catch (SQLException e) {
                showError("Error", "Failed to save configuration: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleLoadConfig() {
        try {
            List<String> configNames = configDAO.getAllConfigNames();

            ChoiceDialog<String> dialog = new ChoiceDialog<>(
                configNames.isEmpty() ? null : configNames.get(0), configNames);
            dialog.setTitle("Load Configuration");
            dialog.setHeaderText("Load configuration from database");
            dialog.setContentText("Select Configuration:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                try {
                    SimulationConfig loadedConfig = configDAO.loadConfigByName(name);
                    if (loadedConfig != null) {
                        copyConfig(loadedConfig, config);
                        updateUIFromConfig();
                        showInfo("Success", "Configuration loaded: " + name);
                    }
                } catch (SQLException e) {
                    showError("Error", "Failed to load configuration: " + e.getMessage());
                }
            });
        } catch (SQLException e) {
            showError("Error", "Failed to get configuration list: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewHistory() {
        // TODO: Implement history viewer window
        showInfo("Under Development", "History viewer feature coming soon");
    }

    @FXML
    private void handleTestDatabase() {
        testDatabaseConnection();
    }

    // ========== Simulation Control Methods ==========

    public void initializeSimulation() {
        if (engine != null && engine.isAlive()) {
            engine.stopSimulation();
            try {
                engine.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        engine = new SimulationEngine(config);
        engine.initialize();
        setupSimulationListener();
    }

    public void startSimulation() {
        if (engine != null && !engine.isRunning()) {
            engine.start();
        }
    }

    public void pauseSimulation() {
        if (engine != null) {
            engine.pauseSimulation();
        }
    }

    public void resumeSimulation() {
        if (engine != null) {
            engine.resumeSimulation();
        }
    }

    public void stopSimulation() {
        if (engine != null) {
            engine.stopSimulation();
        }
    }

    public void stepSimulation() {
        if (engine != null) {
            engine.stepForward();
        }
    }

    public void setSimulationListener(SimulationEngine.SimulationListener listener) {
        if (engine != null) {
            engine.setListener(listener);
        }
    }

    // ========== Database Methods ==========

    private void testDatabaseConnection() {
        boolean connected = dbManager.testConnection();
        Platform.runLater(() -> {
            if (connected) {
                dbStatusLabel.setText("✅ Connected");
                dbStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                dbStatusLabel.setText("❌ Not Connected");
                dbStatusLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }

    private void saveResultsToDatabase() {
        if (currentRunId == null || engine == null) return;

        try {
            SimulationResults results = engine.getResults();
            resultsDAO.updateSimulationRun(currentRunId, results, "COMPLETED");

            // Save completed tasks
            if (!results.getCompletedTasks().isEmpty()) {
                resultsDAO.saveTasks(currentRunId, results.getCompletedTasks());
            }

            showInfo("Success", "Simulation results saved to database");
        } catch (SQLException e) {
            showError("Database Error", "Failed to save results: " + e.getMessage());
        }
    }

    // ========== UI Update Methods ==========

    private void setupSimulationListener() {
        if (engine != null) {
            engine.setListener(new SimulationEngine.SimulationListener() {
                @Override
                public void onTimeUpdate(double time) {
                    Platform.runLater(() -> {
                        timeLabel.setText(String.format("%.2fs", time));
                        updateStatistics();
                        updateVisualization();
                    });
                }

                @Override
                public void onSimulationComplete() {
                    Platform.runLater(() -> {
                        updateButtonStates(false);
                        if (currentRunId != null) {
                            saveResultsToDatabase();
                        }
                        showInfo("Complete", "Simulation completed!");
                    });
                }
            });
        }
    }

    private void updateStatistics() {
        if (engine == null) return;

        SimulationResults results = engine.getResults();
        arrivedTasksLabel.setText(String.valueOf(results.getTotalArrivedTasks()));
        completedTasksLabel.setText(String.valueOf(results.getTotalCompletedTasks()));
        avgSystemTimeLabel.setText(String.format("%.2fs", results.getAverageSystemTime()));
        throughputLabel.setText(String.format("%.3f/s", results.getThroughput()));

        // Update queue stats, user type stats, task type stats
        // TODO: Format and display detailed statistics
    }

    private void updateVisualization() {
        if (visualizationCanvas == null) return;

        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());

        // TODO: Draw service points, queues, tasks in transit
        drawInitialCanvas();
    }

    private void drawInitialCanvas() {
        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(16));
        gc.fillText("System Visualization Area", visualizationCanvas.getWidth() / 2 - 90,
                    visualizationCanvas.getHeight() / 2);
    }

    private void updateButtonStates(boolean running) {
        startButton.setDisable(running);
        pauseButton.setDisable(!running);
        resumeButton.setDisable(true);
        stopButton.setDisable(!running);
    }

    private void updateConfigFromUI() {
        try {
            config.setMeanArrivalInterval(Double.parseDouble(arrivalIntervalField.getText()));
            config.setSimulationTime(Double.parseDouble(simulationTimeField.getText()));
            config.setNumCpuNodes(Integer.parseInt(cpuNodesField.getText()));
            config.setNumGpuNodes(Integer.parseInt(gpuNodesField.getText()));
            config.setCpuTaskProbability(Double.parseDouble(cpuProbabilityField.getText()));
        } catch (NumberFormatException e) {
            showError("Input Error", "Please check configuration parameter format");
        }
    }

    private void updateUIFromConfig() {
        arrivalIntervalField.setText(String.valueOf(config.getMeanArrivalInterval()));
        simulationTimeField.setText(String.valueOf(config.getSimulationTime()));
        cpuNodesField.setText(String.valueOf(config.getNumCpuNodes()));
        gpuNodesField.setText(String.valueOf(config.getNumGpuNodes()));
        cpuProbabilityField.setText(String.valueOf(config.getCpuTaskProbability()));
    }

    private void copyConfig(SimulationConfig source, SimulationConfig dest) {
        dest.setMeanArrivalInterval(source.getMeanArrivalInterval());
        dest.setSimulationTime(source.getSimulationTime());
        dest.setNumCpuNodes(source.getNumCpuNodes());
        dest.setNumGpuNodes(source.getNumGpuNodes());
        dest.setCpuTaskProbability(source.getCpuTaskProbability());
        dest.setNormalUserProbability(source.getNormalUserProbability());
        dest.setPersonalVipProbability(source.getPersonalVipProbability());
        dest.setEnterpriseVipProbability(source.getEnterpriseVipProbability());
    }

    // ========== Utility Methods ==========

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========== Getters ==========

    public SimulationEngine getEngine() {
        return engine;
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public SimulationResults getResults() {
        return engine != null ? engine.getResults() : null;
    }

    public double getCurrentTime() {
        return Clock.getInstance().getTime();
    }
}

