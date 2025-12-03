package com.simulation.database;

import com.simulation.model.SimulationConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SimulationConfig
 * Handles CRUD operations for simulation configurations
 */
public class SimulationConfigDAO {
    private final DatabaseManager dbManager;

    public SimulationConfigDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Save a new configuration to the database
     */
    public int saveConfig(SimulationConfig config, String configName) throws SQLException {
        String sql = "INSERT INTO simulation_configs " +
                "(config_name, mean_arrival_interval, simulation_time, " +
                "num_cpu_nodes, num_gpu_nodes, cpu_task_probability, " +
                "normal_user_probability, personal_vip_probability, enterprise_vip_probability, " +
                "data_storage_service_time, classification_service_time, " +
                "cpu_compute_service_time, gpu_compute_service_time, result_storage_service_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, configName);
            pstmt.setDouble(2, config.getMeanArrivalInterval());
            pstmt.setDouble(3, config.getSimulationTime());
            pstmt.setInt(4, config.getNumCpuNodes());
            pstmt.setInt(5, config.getNumGpuNodes());
            pstmt.setDouble(6, config.getCpuTaskProbability());
            pstmt.setDouble(7, config.getNormalUserProbability());
            pstmt.setDouble(8, config.getPersonalVipProbability());
            pstmt.setDouble(9, config.getEnterpriseVipProbability());
            pstmt.setDouble(10, config.getDataStorageServiceTime());
            pstmt.setDouble(11, config.getClassificationServiceTime());
            pstmt.setDouble(12, config.getCpuComputeServiceTime());
            pstmt.setDouble(13, config.getGpuComputeServiceTime());
            pstmt.setDouble(14, config.getResultStorageServiceTime());

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
     * Update an existing configuration
     */
    public boolean updateConfig(int configId, SimulationConfig config) throws SQLException {
        String sql = "UPDATE simulation_configs SET " +
                "mean_arrival_interval=?, simulation_time=?, " +
                "num_cpu_nodes=?, num_gpu_nodes=?, cpu_task_probability=?, " +
                "normal_user_probability=?, personal_vip_probability=?, enterprise_vip_probability=?, " +
                "data_storage_service_time=?, classification_service_time=?, " +
                "cpu_compute_service_time=?, gpu_compute_service_time=?, result_storage_service_time=? " +
                "WHERE config_id=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, config.getMeanArrivalInterval());
            pstmt.setDouble(2, config.getSimulationTime());
            pstmt.setInt(3, config.getNumCpuNodes());
            pstmt.setInt(4, config.getNumGpuNodes());
            pstmt.setDouble(5, config.getCpuTaskProbability());
            pstmt.setDouble(6, config.getNormalUserProbability());
            pstmt.setDouble(7, config.getPersonalVipProbability());
            pstmt.setDouble(8, config.getEnterpriseVipProbability());
            pstmt.setDouble(9, config.getDataStorageServiceTime());
            pstmt.setDouble(10, config.getClassificationServiceTime());
            pstmt.setDouble(11, config.getCpuComputeServiceTime());
            pstmt.setDouble(12, config.getGpuComputeServiceTime());
            pstmt.setDouble(13, config.getResultStorageServiceTime());
            pstmt.setInt(14, configId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Load configuration by ID
     */
    public SimulationConfig loadConfigById(int configId) throws SQLException {
        String sql = "SELECT * FROM simulation_configs WHERE config_id=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, configId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractConfigFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Load configuration by name
     */
    public SimulationConfig loadConfigByName(String configName) throws SQLException {
        String sql = "SELECT * FROM simulation_configs WHERE config_name=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, configName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractConfigFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all configuration names
     */
    public List<String> getAllConfigNames() throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT config_name FROM simulation_configs ORDER BY created_at DESC";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                names.add(rs.getString("config_name"));
            }
        }
        return names;
    }

    /**
     * Delete configuration by ID
     */
    public boolean deleteConfig(int configId) throws SQLException {
        String sql = "DELETE FROM simulation_configs WHERE config_id=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, configId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Extract SimulationConfig from ResultSet
     */
    private SimulationConfig extractConfigFromResultSet(ResultSet rs) throws SQLException {
        SimulationConfig config = new SimulationConfig();
        config.setMeanArrivalInterval(rs.getDouble("mean_arrival_interval"));
        config.setSimulationTime(rs.getDouble("simulation_time"));
        config.setNumCpuNodes(rs.getInt("num_cpu_nodes"));
        config.setNumGpuNodes(rs.getInt("num_gpu_nodes"));
        config.setCpuTaskProbability(rs.getDouble("cpu_task_probability"));
        config.setNormalUserProbability(rs.getDouble("normal_user_probability"));
        config.setPersonalVipProbability(rs.getDouble("personal_vip_probability"));
        config.setEnterpriseVipProbability(rs.getDouble("enterprise_vip_probability"));
        config.setDataStorageServiceTime(rs.getDouble("data_storage_service_time"));
        config.setClassificationServiceTime(rs.getDouble("classification_service_time"));
        config.setCpuComputeServiceTime(rs.getDouble("cpu_compute_service_time"));
        config.setGpuComputeServiceTime(rs.getDouble("gpu_compute_service_time"));
        config.setResultStorageServiceTime(rs.getDouble("result_storage_service_time"));
        return config;
    }
}

