package com.simulation.database;

import com.simulation.model.SimulationResults;
import com.simulation.model.Task;
import com.simulation.model.TaskType;
import com.simulation.model.UserType;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) for {@link SimulationResults}.
 *
 * <p>This class provides database operations for persisting simulation run results,
 * including:
 * <ul>
 *   <li>Creating new simulation run records</li>
 *   <li>Updating run results with statistics</li>
 *   <li>Saving individual task data</li>
 *   <li>Querying historical simulation runs</li>
 * </ul>
 *
 * <p>Database Tables:
 * <ul>
 *   <li>{@code simulation_runs}: Stores overall run metadata and statistics</li>
 *   <li>{@code simulation_tasks}: Stores individual task details</li>
 * </ul>
 *
 * @author Cloud Simulation Team
 * @version 2.0
 * @see SimulationResults
 * @see DatabaseManager
 */
public class SimulationResultsDAO {
    private final DatabaseManager dbManager;

    public SimulationResultsDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Creates a new simulation run record in the database.
     *
     * <p>This method inserts a new row into the {@code simulation_runs} table
     * with initial status 'RUNNING'. The run can be associated with a saved
     * configuration by providing a configId, or can be standalone (configId = null).
     *
     * @param configId the ID of the associated configuration, or null if none
     * @param runName a descriptive name for this simulation run
     * @return the auto-generated run_id, or -1 if creation failed
     * @throws SQLException if a database access error occurs
     */
    public int createSimulationRun(Integer configId, String runName) throws SQLException {
        String sql = "INSERT INTO simulation_runs (config_id, run_name, status) VALUES (?, ?, 'RUNNING')";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (configId != null) {
                pstmt.setInt(1, configId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, runName);

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Updates a simulation run with final results and statistics.
     *
     * <p>This method is called when the simulation completes. It updates the
     * run record with final statistics including duration, task counts, average
     * system time, throughput, and final status (COMPLETED or FAILED).
     *
     * @param runId the ID of the simulation run to update
     * @param results the SimulationResults containing final statistics
     * @param status the final status ('COMPLETED', 'FAILED', 'STOPPED')
     * @return true if update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean updateSimulationRun(int runId, SimulationResults results, String status) throws SQLException {
        String sql = "UPDATE simulation_runs SET " +
                "end_time=CURRENT_TIMESTAMP, " +
                "simulation_duration=?, " +
                "total_tasks_completed=?, " +
                "total_tasks_arrived=?, " +
                "avg_system_time=?, " +
                "throughput=?, " +
                "status=? " +
                "WHERE run_id=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, results.getTotalSimulationTime());
            pstmt.setInt(2, results.getTotalCompletedTasks());
            pstmt.setInt(3, results.getTotalArrivedTasks());
            pstmt.setDouble(4, results.getAverageSystemTime());
            pstmt.setDouble(5, results.getThroughput());
            pstmt.setString(6, status);
            pstmt.setInt(7, runId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Save a completed task to database
     */
    public boolean saveTask(int runId, Task task) throws SQLException {
        String sql = "INSERT INTO tasks " +
                "(run_id, task_type, user_type, arrival_time, completion_time, system_time) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, runId);
            pstmt.setString(2, task.getTaskType().name());
            pstmt.setString(3, task.getUserType().name());
            pstmt.setDouble(4, task.getArrivalTime());
            pstmt.setDouble(5, task.getCompletionTime());
            pstmt.setDouble(6, task.getTotalSystemTime());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Batch save multiple tasks
     */
    public int saveTasks(int runId, List<Task> tasks) throws SQLException {
        String sql = "INSERT INTO tasks " +
                "(run_id, task_type, user_type, arrival_time, completion_time, system_time) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Task task : tasks) {
                pstmt.setInt(1, runId);
                pstmt.setString(2, task.getTaskType().name());
                pstmt.setString(3, task.getUserType().name());
                pstmt.setDouble(4, task.getArrivalTime());
                pstmt.setDouble(5, task.getCompletionTime());
                pstmt.setDouble(6, task.getTotalSystemTime());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);

            return results.length;
        }
    }

    /**
     * Save service point statistics
     */
    public boolean saveServicePointStats(int runId, String servicePointName,
                                          int totalServed, double avgQueueLength,
                                          int maxQueueLength, double avgWaitingTime,
                                          double utilization) throws SQLException {
        String sql = "INSERT INTO service_point_stats " +
                "(run_id, service_point_name, total_served, avg_queue_length, " +
                "max_queue_length, avg_waiting_time, utilization) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, runId);
            pstmt.setString(2, servicePointName);
            pstmt.setInt(3, totalServed);
            pstmt.setDouble(4, avgQueueLength);
            pstmt.setInt(5, maxQueueLength);
            pstmt.setDouble(6, avgWaitingTime);
            pstmt.setDouble(7, utilization);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Save user type statistics
     */
    public boolean saveUserTypeStats(int runId, UserType userType,
                                      int totalTasks, double avgSystemTime,
                                      double avgWaitingTime) throws SQLException {
        String sql = "INSERT INTO user_type_stats " +
                "(run_id, user_type, total_tasks, avg_system_time, avg_waiting_time) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, runId);
            pstmt.setString(2, userType.name());
            pstmt.setInt(3, totalTasks);
            pstmt.setDouble(4, avgSystemTime);
            pstmt.setDouble(5, avgWaitingTime);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Save task type statistics
     */
    public boolean saveTaskTypeStats(int runId, TaskType taskType,
                                      int totalTasks, double avgComputeTime,
                                      double avgSystemTime) throws SQLException {
        String sql = "INSERT INTO task_type_stats " +
                "(run_id, task_type, total_tasks, avg_compute_time, avg_system_time) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, runId);
            pstmt.setString(2, taskType.name());
            pstmt.setInt(3, totalTasks);
            pstmt.setDouble(4, avgComputeTime);
            pstmt.setDouble(5, avgSystemTime);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all simulation runs
     */
    public List<Map<String, Object>> getAllSimulationRuns() throws SQLException {
        List<Map<String, Object>> runs = new ArrayList<>();
        String sql = "SELECT run_id, run_name, start_time, end_time, " +
                "total_tasks_completed, avg_system_time, throughput, status " +
                "FROM simulation_runs ORDER BY start_time DESC LIMIT 100";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String,Object> run = new java.util.HashMap<>();
                run.put("runId", rs.getInt("run_id"));
                run.put("runName", rs.getString("run_name"));
                run.put("startTime", rs.getTimestamp("start_time"));
                run.put("endTime", rs.getTimestamp("end_time"));
                run.put("totalTasks", rs.getInt("total_tasks_completed"));
                run.put("avgSystemTime", rs.getDouble("avg_system_time"));
                run.put("throughput", rs.getDouble("throughput"));
                run.put("status", rs.getString("status"));
                runs.add(run);
            }

        }
        return runs;
    }

    /**
     * Delete a simulation run and all associated data
     */
    public boolean deleteSimulationRun(int runId) throws SQLException {
        String sql = "DELETE FROM simulation_runs WHERE run_id=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, runId);
            return pstmt.executeUpdate() > 0;
        }
    }
}

