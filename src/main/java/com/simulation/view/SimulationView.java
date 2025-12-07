package com.simulation.view;

import com.simulation.controller.SimulationController;
import com.simulation.model.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main JavaFX view for the simulation
 */
public class SimulationView {
    private final SimulationController controller;
    private Stage stage;

    // UI Components
    private Canvas canvas;
    private TextArea statsArea;
    private Label timeLabel;
    private Button startButton;
    private Button pauseButton;
    private Button stepButton;
    private Button stopButton;
    private Button resetButton;

    // Configuration controls
    private TextField arrivalIntervalField;
    private TextField cpuNodesField;
    private TextField gpuNodesField;
    private TextField simulationTimeField;
    private Slider speedSlider;

    public SimulationView(SimulationController controller) {
        this.controller = controller;
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("Cloud Computing Service Queue Simulation");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Control panel
        root.setTop(createControlPanel());

        // Center: Visualization canvas
        root.setCenter(createVisualizationPanel());

        // Right: Statistics panel
        root.setRight(createStatsPanel());

        // Bottom: Configuration panel
        root.setBottom(createConfigPanel());

        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Set up simulation listener
        updateSimulationListener();
    }

    private VBox createControlPanel() {
        VBox controlBox = new VBox(10);
        controlBox.setPadding(new Insets(10));
        controlBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Simulation Controls");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        startButton = new Button("Start");
        startButton.setOnAction(e -> startSimulation());

        pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> pauseSimulation());
        pauseButton.setDisable(true);

        stepButton = new Button("Step");
        stepButton.setOnAction(e -> stepSimulation());

        stopButton = new Button("Stop");
        stopButton.setOnAction(e -> stopSimulation());
        stopButton.setDisable(true);

        resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetSimulation());

        timeLabel = new Label("Time: 0.00s");
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        buttonBox.getChildren().addAll(startButton, pauseButton, stepButton, stopButton, resetButton, timeLabel);
        controlBox.getChildren().addAll(titleLabel, buttonBox);

        return controlBox;
    }

    private VBox createVisualizationPanel() {
        VBox vizBox = new VBox(10);
        vizBox.setPadding(new Insets(10));
        vizBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("System Visualization");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        canvas = new Canvas(800, 600);
        drawInitialState();

        vizBox.getChildren().addAll(titleLabel, canvas);
        return vizBox;
    }

    private VBox createStatsPanel() {
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(10));
        statsBox.setPrefWidth(350);

        Label titleLabel = new Label("Statistics");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setPrefHeight(700);
        statsArea.setFont(Font.font("Courier New", 11));

        statsBox.getChildren().addAll(titleLabel, statsArea);
        return statsBox;
    }

    private VBox createConfigPanel() {
        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Configuration Parameters");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        int row = 0;

        // Arrival interval
        grid.add(new Label("Mean Arrival Interval (s):"), 0, row);
        arrivalIntervalField = new TextField(String.valueOf(controller.getConfig().getMeanArrivalInterval()));
        grid.add(arrivalIntervalField, 1, row++);

        // CPU nodes
        grid.add(new Label("CPU Compute Nodes:"), 0, row);
        cpuNodesField = new TextField(String.valueOf(controller.getConfig().getNumCpuNodes()));
        grid.add(cpuNodesField, 1, row++);

        // GPU nodes
        grid.add(new Label("GPU Compute Nodes:"), 0, row);
        gpuNodesField = new TextField(String.valueOf(controller.getConfig().getNumGpuNodes()));
        grid.add(gpuNodesField, 1, row++);

        // Simulation time
        grid.add(new Label("Simulation Time (s):"), 0, row);
        simulationTimeField = new TextField(String.valueOf(controller.getConfig().getSimulationTime()));
        grid.add(simulationTimeField, 1, row++);

        // Speed control
        grid.add(new Label("Simulation Speed:"), 0, row);
        speedSlider = new Slider(0.1, 5.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            controller.getConfig().setSpeedMultiplier(newVal.doubleValue());
        });
        grid.add(speedSlider, 1, row++);

        Button applyButton = new Button("Apply Configuration");
        applyButton.setOnAction(e -> applyConfiguration());
        grid.add(applyButton, 0, row, 2, 1);

        configBox.getChildren().addAll(titleLabel, grid);
        return configBox;
    }

    private void drawInitialState() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawSystemDiagram(gc);
    }

    private void drawSystemDiagram(GraphicsContext gc) {
        // Draw the system flow diagram
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        double startX = 50;
        double startY = 300;
        double spacing = 100;

        // Draw service points as boxes
        drawServicePoint(gc, startX, startY, "Arrival", Color.LIGHTGREEN, 0, 0);
        drawArrow(gc, startX + 60, startY, startX + spacing, startY);

        startX += spacing;
        drawServicePoint(gc, startX, startY, "Data\nStorage", Color.LIGHTBLUE, 0, 0);
        drawArrow(gc, startX + 60, startY, startX + spacing, startY);

        startX += spacing;
        drawServicePoint(gc, startX, startY, "Classification", Color.LIGHTYELLOW, 0, 0);

        // Split into CPU and GPU paths
        double cpuY = startY - 100;
        double gpuY = startY + 100;

        // CPU path
        drawArrow(gc, startX + 30, startY - 30, startX + 30, cpuY + 30);
        drawServicePoint(gc, startX, cpuY, "CPU\nQueue", Color.ORANGE, 0, 0);
        drawArrow(gc, startX + 60, cpuY, startX + spacing, cpuY);

        double cpuComputeX = startX + spacing;
        drawServicePoint(gc, cpuComputeX, cpuY, "CPU\nCompute", Color.CORAL, 0, 0);

        // GPU path
        drawArrow(gc, startX + 30, startY + 30, startX + 30, gpuY - 30);
        drawServicePoint(gc, startX, gpuY, "GPU\nQueue", Color.PINK, 0, 0);
        drawArrow(gc, startX + 60, gpuY, startX + spacing, gpuY);

        double gpuComputeX = startX + spacing;
        drawServicePoint(gc, gpuComputeX, gpuY, "GPU\nCompute", Color.PLUM, 0, 0);

        // Merge to result storage
        double resultX = cpuComputeX + spacing;
        drawArrow(gc, cpuComputeX + 60, cpuY, resultX, startY);
        drawArrow(gc, gpuComputeX + 60, gpuY, resultX, startY);

        drawServicePoint(gc, resultX, startY, "Result\nStorage", Color.LIGHTGREEN, 0, 0);
        drawArrow(gc, resultX + 60, startY, resultX + 80, startY);

        gc.fillText("Exit", resultX + 85, startY + 5);
    }

    private void drawServicePoint(GraphicsContext gc, double x, double y, String name, Color color, int queueLength, int busyServers) {
        // Draw box
        gc.setFill(color);
        gc.fillRect(x, y - 30, 60, 60);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y - 30, 60, 60);

        // Draw name
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 10));
        String[] lines = name.split("\n");
        for (int i = 0; i < lines.length; i++) {
            gc.fillText(lines[i], x + 5, y - 10 + i * 12);
        }

        // Draw queue length if > 0
        if (queueLength > 0) {
            gc.setFill(Color.RED);
            gc.fillText("Q:" + queueLength, x + 5, y + 20);
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

    private void updateVisualization() {
        if (controller.getEngine() == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Redraw with current state
        SimulationEngine engine = controller.getEngine();
        drawSystemWithState(gc, engine);
    }

    private void drawSystemWithState(GraphicsContext gc, SimulationEngine engine) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        double startX = 50;
        double startY = 300;
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

        // CPU and GPU paths - compute service points now include their own queues
        double cpuY = startY - 100;
        double gpuY = startY + 100;

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

    private void updateStatistics() {
        if (controller.getEngine() == null) return;

        SimulationEngine engine = controller.getEngine();
        SimulationResults results = engine.getResults();

        StringBuilder stats = new StringBuilder();
        stats.append("=== SIMULATION STATISTICS ===\n\n");

        stats.append("Current Time: ").append(String.format("%.2f", controller.getCurrentTime())).append(" s\n\n");

        stats.append("--- Task Statistics ---\n");
        stats.append("Tasks Arrived: ").append(results.getTotalTasksArrived()).append("\n");
        stats.append("Tasks Completed: ").append(results.getTotalTasksCompleted()).append("\n");
        stats.append("CPU Tasks: ").append(results.getCpuTasksCompleted()).append("\n");
        stats.append("GPU Tasks: ").append(results.getGpuTasksCompleted()).append("\n\n");

        stats.append("--- User Type Statistics ---\n");
        stats.append("Normal Users: ").append(results.getNormalUserTasksCompleted()).append("\n");
        stats.append("Personal VIP: ").append(results.getPersonalVipTasksCompleted()).append("\n");
        stats.append("Enterprise VIP: ").append(results.getEnterpriseVipTasksCompleted()).append("\n\n");

        stats.append("--- Performance Metrics ---\n");
        stats.append("Avg System Time: ").append(String.format("%.2f", results.getAverageSystemTime())).append(" s\n");
        stats.append("Min System Time: ").append(String.format("%.2f", results.getMinSystemTime())).append(" s\n");
        stats.append("Max System Time: ").append(String.format("%.2f", results.getMaxSystemTime())).append(" s\n");
        stats.append("Throughput: ").append(String.format("%.4f", results.getThroughput(controller.getCurrentTime()))).append(" tasks/s\n\n");

        stats.append("--- Service Point Statistics ---\n");
        addServicePointStats(stats, "Data Storage", engine.getDataStorage());
        addServicePointStats(stats, "Classification", engine.getClassification());
        addServicePointStats(stats, "CPU Compute", engine.getCpuCompute());
        addServicePointStats(stats, "GPU Compute", engine.getGpuCompute());
        addServicePointStats(stats, "Result Storage", engine.getResultStorage());

        statsArea.setText(stats.toString());
    }

    private void addServicePointStats(StringBuilder stats, String name, ServicePoint sp) {
        stats.append(name).append(":\n");
        stats.append("  Queue Length: ").append(sp.getQueueLength()).append("\n");
        stats.append("  Max Queue: ").append(sp.getMaxQueueLength()).append("\n");
        stats.append("  Tasks Served: ").append(sp.getTasksServed()).append("\n");
        stats.append("  Avg Queue Time: ").append(String.format("%.2f", sp.getAverageQueueTime())).append(" s\n");
        stats.append("  Utilization: ").append(String.format("%.2f%%", sp.getUtilization(controller.getCurrentTime()) * 100)).append("\n\n");
    }

    private void updateSimulationListener() {
        controller.setSimulationListener(new SimulationEngine.SimulationListener() {
            @Override
            public void onTimeUpdate(double time) {
                Platform.runLater(() -> {
                    timeLabel.setText(String.format("Time: %.2fs", time));
                    updateVisualization();
                    updateStatistics();
                });
            }

            @Override
            public void onSimulationComplete() {
                Platform.runLater(() -> {
                    updateVisualization();
                    updateStatistics();
                    startButton.setDisable(true);
                    pauseButton.setDisable(true);
                    stepButton.setDisable(true);
                    stopButton.setDisable(true);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Simulation Complete");
                    alert.setHeaderText("Simulation has finished");
                    alert.setContentText("The simulation has completed. Check the statistics for results.");
                    alert.showAndWait();
                });
            }
        });
    }

    private void startSimulation() {
        applyConfiguration();
        controller.initializeSimulation();
        updateSimulationListener();
        controller.startSimulation();

        startButton.setDisable(true);
        pauseButton.setDisable(false);
        stopButton.setDisable(false);
    }

    private void pauseSimulation() {
        if (controller.getEngine().isPaused()) {
            controller.resumeSimulation();
            pauseButton.setText("Pause");
        } else {
            controller.pauseSimulation();
            pauseButton.setText("Resume");
        }
    }

    private void stepSimulation() {
        controller.stepSimulation();
    }

    private void stopSimulation() {
        controller.stopSimulation();
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
    }

    private void resetSimulation() {
        controller.stopSimulation();
        controller.initializeSimulation();
        updateSimulationListener();

        timeLabel.setText("Time: 0.00s");
        drawInitialState();
        statsArea.clear();

        startButton.setDisable(false);
        pauseButton.setDisable(true);
        pauseButton.setText("Pause");
        stepButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void applyConfiguration() {
        try {
            SimulationConfig config = controller.getConfig();
            config.setMeanArrivalInterval(Double.parseDouble(arrivalIntervalField.getText()));
            config.setNumCpuNodes(Integer.parseInt(cpuNodesField.getText()));
            config.setNumGpuNodes(Integer.parseInt(gpuNodesField.getText()));
            config.setSimulationTime(Double.parseDouble(simulationTimeField.getText()));
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Configuration Error");
            alert.setContentText("Please enter valid numbers for all configuration fields.");
            alert.showAndWait();
        }
    }
}

