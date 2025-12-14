package com.simulation.controller;

import com.simulation.database.DatabaseManager;
import com.simulation.database.SimulationConfigDAO;
import com.simulation.database.SimulationResultsDAO;
import com.simulation.model.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.*;

/**
 * FXML Controller that mediates between the View (UI) and Model (simulation engine).
 *
 * <p>This controller follows the MVC (Model-View-Controller) design pattern and is responsible for:
 * <ul>
 *   <li>Handling all UI events (button clicks, slider changes, input validation)</li>
 *   <li>Managing the lifecycle of the simulation engine (start, pause, resume, stop, reset)</li>
 *   <li>Updating UI components with real-time simulation data</li>
 *   <li>Coordinating database operations (save/load configurations and results)</li>
 *   <li>Rendering visualization of the simulation system on canvas</li>
 * </ul>
 *
 * <p>The controller implements {@link SimulationEngine.SimulationListener} to receive
 * callbacks from the simulation thread and update the UI accordingly using JavaFX's
 * {@link Platform#runLater(Runnable)} for thread-safe UI updates.
 *
 * <p>UI Update Throttling: To prevent performance issues during fast simulations,
 * UI updates are throttled to occur at most every {@code UPDATE_INTERVAL_MS} milliseconds.
 *
 * @author Cloud Simulation Team
 * @version 2.0
 * @see SimulationEngine
 * @see SimulationConfig
 * @see SimulationResults
 * @see SimulationView
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
    @FXML private TextField resultStorageServiceTimeField;

    @FXML private Slider speedSlider;
    @FXML private Canvas visualizationCanvas;

    @FXML private TableView<Map<String,Object>> historyTable;
    @FXML private TableColumn<Map<String,Object>, String> runIdColumn;
    @FXML private TableColumn<Map<String,Object>, String> runNameColumn;
    @FXML private TableColumn<Map<String,Object>, String> startTimeColumn;
    @FXML private TableColumn<Map<String,Object>, String> endTimeColumn;
    @FXML private TableColumn<Map<String,Object>, String> completedTasksColumn;
    @FXML private TableColumn<Map<String,Object>, String> avgSystemTimeColumn;
    @FXML private TableColumn<Map<String,Object>, String> throughputColumn;
    @FXML private TableColumn<Map<String,Object>, String> statusColumn;



    // Model components
    private SimulationEngine engine;
    private final SimulationConfig config;

    // Database components
    private final DatabaseManager dbManager;
    private final SimulationConfigDAO configDAO;
    private final SimulationResultsDAO resultsDAO;
    private Integer currentRunId;

    // UI update throttling
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 100; // Update UI every 100ms

    public SimulationController() {
        this.config = new SimulationConfig();
        this.dbManager = DatabaseManager.getInstance();

        // Initialize DAO objects, but they will handle database unavailability internally
        SimulationConfigDAO tempConfigDAO = null;
        SimulationResultsDAO tempResultsDAO = null;
        try {
            tempConfigDAO = new SimulationConfigDAO();
            tempResultsDAO = new SimulationResultsDAO();
        } catch (Exception e) {
            System.err.println("⚠️  Could not initialize database DAOs: " + e.getMessage());
        }
        this.configDAO = tempConfigDAO;
        this.resultsDAO = tempResultsDAO;
    }

    /**
     * Initialize method called after FXML loading
     */
    @FXML
    public void initialize() {
        // Setup speed slider listener
        if (speedSlider != null) {
            speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double speed = newVal.doubleValue();
                // Format speed display: show 1 decimal for < 10x, integer for >= 10x
                if (speed >= 10.0) {
                    speedLabel.setText(String.format("%.0fx", speed));
                } else {
                    speedLabel.setText(String.format("%.1fx", speed));
                }
                if (engine != null) {
                    engine.setSpeed(speed);
                }
            });
        }

        // Test database connection
        testDatabaseConnection();

        // Initialize canvas
        if (visualizationCanvas != null) {
            drawInitialCanvas();
        }
        // === Table column bindings ===
        runIdColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().get("runId")))
        );

        runNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().get("runName")))
        );

        startTimeColumn.setCellValueFactory(cell -> {
            Object ts = cell.getValue().get("startTime");
            return new SimpleStringProperty(ts == null ? "" : ts.toString());
        });

        endTimeColumn.setCellValueFactory(cell -> {
            Object ts = cell.getValue().get("endTime");
            return new SimpleStringProperty(ts == null ? "" : ts.toString());
        });

        completedTasksColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().get("totalTasks")))
        );

        avgSystemTimeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        String.format("%.3f",
                                (double) cell.getValue().get("avgSystemTime"))
                )
        );

        throughputColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        String.format("%.3f",
                                (double) cell.getValue().get("throughput"))
                )
        );

        statusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().get("status")))
        );

        // Auto-load history on startup
        loadHistoryData();
    }


        // ========== FXML Event Handlers ==========

    @FXML
    private void handleStart() {
        updateConfigFromUI();
        initializeSimulation();

        // Apply current speed from slider to the new engine
        if (engine != null && speedSlider != null) {
            engine.setSpeed(speedSlider.getValue());
        }

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

        // Apply current speed from slider to the reset engine
        if (engine != null && speedSlider != null) {
            engine.setSpeed(speedSlider.getValue());
        }

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

    /**
     * Load history data from database and display in table
     */
    private void loadHistoryData() {
        if (resultsDAO == null) {
            System.out.println("⚠️  Database not available - cannot load history");
            return;
        }

        try {
            List<Map<String,Object>> runs = resultsDAO.getAllSimulationRuns();
            System.out.println("✅ History loaded: " + runs.size() + " simulation runs found");

            if (historyTable != null) {
                historyTable.setItems(FXCollections.observableArrayList(runs));
                historyTable.refresh();
                System.out.println("✅ History table updated in UI");
            } else {
                System.err.println("⚠️  History table is null!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to load history: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error loading history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewHistory() {
        System.out.println("📊 View History button clicked!");
        loadHistoryData();
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
                    // Throttle UI updates to prevent stuttering
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= UPDATE_INTERVAL_MS) {
                        lastUpdateTime = currentTime;
                        Platform.runLater(() -> {
                            timeLabel.setText(String.format("%.2fs", time));
                            updateStatistics();
                            updateVisualization();
                        });
                    }
                }

                @Override
                public void onSimulationComplete() {
                    Platform.runLater(() -> {
                        // Final update to show accurate completion state
                        updateStatistics();
                        updateVisualization();
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
        // Use current clock time for real-time throughput calculation
        double currentTime = Clock.getInstance().getTime();
        throughputLabel.setText(String.format("%.3f/s", results.getThroughput(currentTime)));

        // Update queue stats
        updateQueueStatistics();

        // Update user type stats
        updateUserTypeStatistics();

        // Update task type stats
        updateTaskTypeStatistics();
    }

    /**
     * Update Queue Statistics display
     */
    private void updateQueueStatistics() {
        if (engine == null || queueStatsLabel == null) return;

        ServicePoint dataStorage = engine.getDataStorage();
        ServicePoint classification = engine.getClassification();
        ServicePoint cpuCompute = engine.getCpuCompute();
        ServicePoint gpuCompute = engine.getGpuCompute();
        ServicePoint resultStorage = engine.getResultStorage();

        double currentTime = Clock.getInstance().getTime();

        StringBuilder sb = new StringBuilder();
        sb.append("📦 Data Storage:\n");
        sb.append(String.format("   Queue: %d | Busy: %d/%d | Max: %d\n",
            dataStorage.getQueueLength(), dataStorage.getBusyServers(), 1, dataStorage.getMaxQueueLength()));
        sb.append(String.format("   Utilization: %.1f%%\n\n", dataStorage.getUtilization(currentTime) * 100));

        sb.append("🔍 Classification:\n");
        sb.append(String.format("   Queue: %d | Busy: %d/%d | Max: %d\n",
            classification.getQueueLength(), classification.getBusyServers(), 1, classification.getMaxQueueLength()));
        sb.append(String.format("   Utilization: %.1f%%\n\n", classification.getUtilization(currentTime) * 100));

        sb.append("💻 CPU Compute:\n");
        sb.append(String.format("   Queue: %d | Busy: %d/%d | Max: %d\n",
            cpuCompute.getQueueLength(), cpuCompute.getBusyServers(),
            config.getNumCpuNodes(), cpuCompute.getMaxQueueLength()));
        sb.append(String.format("   Utilization: %.1f%% | Served: %d\n\n",
            cpuCompute.getUtilization(currentTime) * 100, cpuCompute.getTasksServed()));


        sb.append("🎮 GPU Compute:\n");
        sb.append(String.format("   Queue: %d | Busy: %d/%d | Max: %d\n",
            gpuCompute.getQueueLength(), gpuCompute.getBusyServers(),
            config.getNumGpuNodes(), gpuCompute.getMaxQueueLength()));
        sb.append(String.format("   Utilization: %.1f%% | Served: %d\n\n",
            gpuCompute.getUtilization(currentTime) * 100, gpuCompute.getTasksServed()));

        sb.append("💾 Result Storage:\n");
        sb.append(String.format("   Queue: %d | Busy: %d/%d | Max: %d\n",
            resultStorage.getQueueLength(), resultStorage.getBusyServers(), 1, resultStorage.getMaxQueueLength()));
        sb.append(String.format("   Utilization: %.1f%%", resultStorage.getUtilization(currentTime) * 100));

        queueStatsLabel.setText(sb.toString());
    }

    /**
     * Update User Type Statistics display
     */
    private void updateUserTypeStatistics() {
        if (engine == null || userTypeStatsLabel == null) return;

        SimulationResults results = engine.getResults();
        int totalCompleted = results.getTotalCompletedTasks();

        if (totalCompleted == 0) {
            userTypeStatsLabel.setText("No tasks completed yet...");
            return;
        }

        int normalCompleted = results.getNormalUserTasksCompleted();
        int personalVipCompleted = results.getPersonalVipTasksCompleted();
        int enterpriseVipCompleted = results.getEnterpriseVipTasksCompleted();

        // Calculate average system time for each user type
        Map<UserType, Double> avgSystemTimeByUser = calculateAverageSystemTimeByUserType();

        StringBuilder sb = new StringBuilder();
        sb.append("👤 NORMAL Users:\n");
        sb.append(String.format("   Completed: %d (%.1f%%)\n",
            normalCompleted, (normalCompleted * 100.0 / totalCompleted)));
        sb.append(String.format("   Avg System Time: %.2fs\n\n",
            avgSystemTimeByUser.getOrDefault(UserType.NORMAL, 0.0)));

        sb.append("⭐ PERSONAL VIP:\n");
        sb.append(String.format("   Completed: %d (%.1f%%)\n",
            personalVipCompleted, (personalVipCompleted * 100.0 / totalCompleted)));
        sb.append(String.format("   Avg System Time: %.2fs\n\n",
            avgSystemTimeByUser.getOrDefault(UserType.PERSONAL_VIP, 0.0)));

        sb.append("⭐⭐ ENTERPRISE VIP:\n");
        sb.append(String.format("   Completed: %d (%.1f%%)\n",
            enterpriseVipCompleted, (enterpriseVipCompleted * 100.0 / totalCompleted)));
        sb.append(String.format("   Avg System Time: %.2fs\n\n",
            avgSystemTimeByUser.getOrDefault(UserType.ENTERPRISE_VIP, 0.0)));

        // Add comparison
        sb.append("📊 Priority Effect:\n");
        double normalTime = avgSystemTimeByUser.getOrDefault(UserType.NORMAL, 0.0);
        double personalVipTime = avgSystemTimeByUser.getOrDefault(UserType.PERSONAL_VIP, 0.0);
        double enterpriseTime = avgSystemTimeByUser.getOrDefault(UserType.ENTERPRISE_VIP, 0.0);

        if (normalTime > 0 && enterpriseTime > 0) {
            double improvement = ((normalTime - enterpriseTime) / normalTime) * 100;
            if (improvement > 0) {
                sb.append(String.format("   Enterprise VIP is %.1f%% faster than Normal\n", improvement));
            } else {
                sb.append(String.format("   Enterprise VIP is %.1f%% slower than Normal\n", -improvement));
                sb.append("   (May occur with small samples or low load)\n");
            }
        } else {
            sb.append("   Insufficient data for comparison\n");
        }

        // Add Personal VIP comparison
        if (normalTime > 0 && personalVipTime > 0) {
            double personalImprovement = ((normalTime - personalVipTime) / normalTime) * 100;
            if (personalImprovement > 0) {
                sb.append(String.format("   Personal VIP is %.1f%% faster than Normal", personalImprovement));
            } else {
                sb.append(String.format("   Personal VIP is %.1f%% slower than Normal", -personalImprovement));
            }
        }

        userTypeStatsLabel.setText(sb.toString());
    }

    /**
     * Update Task Type Statistics display
     */
    private void updateTaskTypeStatistics() {
        if (engine == null || taskTypeStatsLabel == null) return;

        SimulationResults results = engine.getResults();
        int totalCompleted = results.getTotalCompletedTasks();

        if (totalCompleted == 0) {
            taskTypeStatsLabel.setText("No tasks completed yet...");
            return;
        }

        int cpuCompleted = results.getCpuTasksCompleted();
        int gpuCompleted = results.getGpuTasksCompleted();

        // Calculate average system time for each task type
        Map<TaskType, Double> avgSystemTimeByTask = calculateAverageSystemTimeByTaskType();

        ServicePoint cpuCompute = engine.getCpuCompute();
        ServicePoint gpuCompute = engine.getGpuCompute();
        double currentTime = Clock.getInstance().getTime();

        StringBuilder sb = new StringBuilder();
        sb.append("💻 CPU Tasks:\n");
        sb.append(String.format("   Completed: %d (%.1f%%)\n",
            cpuCompleted, (cpuCompleted * 100.0 / totalCompleted)));
        sb.append(String.format("   Avg System Time: %.2fs\n",
            avgSystemTimeByTask.getOrDefault(TaskType.CPU, 0.0)));
        sb.append(String.format("   Node Utilization: %.1f%%\n\n",
            cpuCompute.getUtilization(currentTime) * 100));

        sb.append("🎮 GPU Tasks:\n");
        sb.append(String.format("   Completed: %d (%.1f%%)\n",
            gpuCompleted, (gpuCompleted * 100.0 / totalCompleted)));
        sb.append(String.format("   Avg System Time: %.2fs\n",
            avgSystemTimeByTask.getOrDefault(TaskType.GPU, 0.0)));
        sb.append(String.format("   Node Utilization: %.1f%%\n\n",
            gpuCompute.getUtilization(currentTime) * 100));

        // Add resource efficiency analysis
        sb.append("⚡ Resource Efficiency:\n");
        double cpuUtil = cpuCompute.getUtilization(currentTime) * 100;
        double gpuUtil = gpuCompute.getUtilization(currentTime) * 100;

        if (cpuUtil > 90) {
            sb.append("   ⚠️ CPU nodes overloaded\n");
        } else if (cpuUtil < 30) {
            sb.append("   💡 CPU nodes underutilized\n");
        } else {
            sb.append("   ✅ CPU nodes balanced\n");
        }

        if (gpuUtil > 90) {
            sb.append("   ⚠️ GPU nodes overloaded");
        } else if (gpuUtil < 30) {
            sb.append("   💡 GPU nodes underutilized");
        } else {
            sb.append("   ✅ GPU nodes balanced");
        }

        taskTypeStatsLabel.setText(sb.toString());
    }

    /**
     * Calculate average system time grouped by user type
     */
    private Map<UserType, Double> calculateAverageSystemTimeByUserType() {
        Map<UserType, Double> result = new HashMap<>();
        Map<UserType, Integer> counts = new HashMap<>();

        if (engine == null) return result;

        SimulationResults results = engine.getResults();
        List<Task> completedTasks = results.getCompletedTasks();

        for (Task task : completedTasks) {
            UserType type = task.getUserType();
            double systemTime = task.getTotalSystemTime();
            result.put(type, result.getOrDefault(type, 0.0) + systemTime);
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }

        // Calculate averages
        for (UserType type : result.keySet()) {
            int count = counts.get(type);
            if (count > 0) {
                result.put(type, result.get(type) / count);
            }
        }

        return result;
    }

    /**
     * Calculate average system time grouped by task type
     */
    private Map<TaskType, Double> calculateAverageSystemTimeByTaskType() {
        Map<TaskType, Double> result = new HashMap<>();
        Map<TaskType, Integer> counts = new HashMap<>();

        if (engine == null) return result;

        SimulationResults results = engine.getResults();
        List<Task> completedTasks = results.getCompletedTasks();

        for (Task task : completedTasks) {
            TaskType type = task.getTaskType();
            double systemTime = task.getTotalSystemTime();
            result.put(type, result.getOrDefault(type, 0.0) + systemTime);
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }

        // Calculate averages
        for (TaskType type : result.keySet()) {
            int count = counts.get(type);
            if (count > 0) {
                result.put(type, result.get(type) / count);
            }
        }

        return result;
    }

    private void updateVisualization() {
        if (visualizationCanvas == null || engine == null) return;

        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());

        // Draw system with current state
        drawSystemWithState(gc);
    }

    private void drawInitialCanvas() {
        if (visualizationCanvas == null) return;

        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(16));
        gc.fillText("System Visualization Area", visualizationCanvas.getWidth() / 2 - 90,
                    visualizationCanvas.getHeight() / 2);
    }

    private void drawSystemWithState(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        double startX = 50;
        double startY = 200;  // Reduced from 300 to 200 to fit in smaller canvas
        double spacing = 100;

        // Draw service points with current state
        drawServicePoint(gc, startX, startY, "Arrival", Color.LIGHTGREEN, 0, 0);
        drawArrow(gc, startX + 60, startY, startX + spacing, startY);

        startX += spacing;
        ServicePoint dataStorage = engine.getDataStorage();
        drawServicePoint(gc, startX, startY, "Data\nStorage", Color.LIGHTBLUE,
            dataStorage.getQueueLength(), dataStorage.getBusyServers());
        drawArrow(gc, startX + 60, startY, startX + spacing, startY);

        startX += spacing;
        ServicePoint classification = engine.getClassification();
        drawServicePoint(gc, startX, startY, "Classification", Color.LIGHTYELLOW,
            classification.getQueueLength(), classification.getBusyServers());

        // CPU and GPU paths - now compute service points include their own queues
        double cpuY = startY - 80;  // Reduced from 100 to 80 for more compact layout
        double gpuY = startY + 80;  // Reduced from 100 to 80 for more compact layout

        drawArrow(gc, startX + 30, startY - 30, startX + 30, cpuY + 30);
        double cpuComputeX = startX;
        ServicePoint cpuCompute = engine.getCpuCompute();
        drawServicePoint(gc, cpuComputeX, cpuY, "CPU\nCompute", Color.CORAL,
            cpuCompute.getQueueLength(), cpuCompute.getBusyServers());

        drawArrow(gc, startX + 30, startY + 30, startX + 30, gpuY - 30);
        double gpuComputeX = startX;
        ServicePoint gpuCompute = engine.getGpuCompute();
        drawServicePoint(gc, gpuComputeX, gpuY, "GPU\nCompute", Color.PLUM,
            gpuCompute.getQueueLength(), gpuCompute.getBusyServers());

        double resultX = cpuComputeX + spacing;
        drawArrow(gc, cpuComputeX + 60, cpuY, resultX, startY);
        drawArrow(gc, gpuComputeX + 60, gpuY, resultX, startY);

        ServicePoint resultStorage = engine.getResultStorage();
        drawServicePoint(gc, resultX, startY, "Result\nStorage", Color.LIGHTGREEN,
            resultStorage.getQueueLength(), resultStorage.getBusyServers());
        drawArrow(gc, resultX + 60, startY, resultX + 80, startY);

        gc.fillText("Exit", resultX + 85, startY + 5);
    }

    private void drawServicePoint(GraphicsContext gc, double x, double y, String name,
                                   Color color, int queueLength, int busyServers) {
        // Draw box
        gc.setFill(color);
        gc.fillRect(x, y - 30, 60, 60);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y - 30, 60, 60);

        // Draw name
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 10));
        String[] lines = name.split("\n");
        for (int i = 0; i < lines.length; i++) {
            gc.fillText(lines[i], x + 5, y - 10 + i * 12);
        }

        // Draw queue length if > 0
        if (queueLength > 0) {
            gc.setFill(Color.RED);
            gc.fillText("Q:" + queueLength, x + 5, y + 20);
        }

        // Draw busy servers if > 0
        if (busyServers > 0) {
            gc.setFill(Color.BLUE);
            gc.fillText("B:" + busyServers, x + 35, y + 20);
        }
    }

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);

        // Draw arrowhead
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowLength = 10;
        double arrowAngle = Math.PI / 6;

        double x3 = x2 - arrowLength * Math.cos(angle - arrowAngle);
        double y3 = y2 - arrowLength * Math.sin(angle - arrowAngle);
        double x4 = x2 - arrowLength * Math.cos(angle + arrowAngle);
        double y4 = y2 - arrowLength * Math.sin(angle + arrowAngle);

        gc.strokeLine(x2, y2, x3, y3);
        gc.strokeLine(x2, y2, x4, y4);
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
            config.setResultStorageServiceTime(Double.parseDouble(resultStorageServiceTimeField.getText()));
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
        resultStorageServiceTimeField.setText(String.valueOf(config.getResultStorageServiceTime()));
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

