-- 云计算服务排队模拟系统数据库架构
-- Database Schema for Cloud Computing Service Queue Simulation

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cloud_simulation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cloud_simulation;

-- 1. 仿真配置表
CREATE TABLE IF NOT EXISTS simulation_configs (
    config_id INT AUTO_INCREMENT PRIMARY KEY,
    config_name VARCHAR(100) NOT NULL UNIQUE,
    mean_arrival_interval DOUBLE NOT NULL DEFAULT 2.0,
    simulation_time DOUBLE NOT NULL DEFAULT 1000.0,
    num_cpu_nodes INT NOT NULL DEFAULT 2,
    num_gpu_nodes INT NOT NULL DEFAULT 1,
    cpu_task_probability DOUBLE NOT NULL DEFAULT 0.7,
    normal_user_probability DOUBLE NOT NULL DEFAULT 0.6,
    personal_vip_probability DOUBLE NOT NULL DEFAULT 0.3,
    enterprise_vip_probability DOUBLE NOT NULL DEFAULT 0.1,
    data_storage_service_time DOUBLE NOT NULL DEFAULT 0.5,
    classification_service_time DOUBLE NOT NULL DEFAULT 0.3,
    cpu_compute_service_time DOUBLE NOT NULL DEFAULT 5.0,
    gpu_compute_service_time DOUBLE NOT NULL DEFAULT 8.0,
    result_storage_service_time DOUBLE NOT NULL DEFAULT 0.5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 仿真运行记录表
CREATE TABLE IF NOT EXISTS simulation_runs (
    run_id INT AUTO_INCREMENT PRIMARY KEY,
    config_id INT,
    run_name VARCHAR(100),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    simulation_duration DOUBLE,
    total_tasks_completed INT DEFAULT 0,
    total_tasks_arrived INT DEFAULT 0,
    avg_system_time DOUBLE DEFAULT 0.0,
    throughput DOUBLE DEFAULT 0.0,
    status ENUM('RUNNING', 'COMPLETED', 'STOPPED', 'ERROR') DEFAULT 'RUNNING',
    FOREIGN KEY (config_id) REFERENCES simulation_configs(config_id) ON DELETE SET NULL
);

-- 3. 任务记录表
CREATE TABLE IF NOT EXISTS tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    run_id INT NOT NULL,
    task_type ENUM('CPU', 'GPU') NOT NULL,
    user_type ENUM('NORMAL', 'PERSONAL_VIP', 'ENTERPRISE_VIP') NOT NULL,
    arrival_time DOUBLE NOT NULL,
    completion_time DOUBLE,
    system_time DOUBLE,
    data_storage_entry_time DOUBLE,
    data_storage_exit_time DOUBLE,
    classification_entry_time DOUBLE,
    classification_exit_time DOUBLE,
    queue_entry_time DOUBLE,
    queue_exit_time DOUBLE,
    compute_entry_time DOUBLE,
    compute_exit_time DOUBLE,
    result_storage_entry_time DOUBLE,
    result_storage_exit_time DOUBLE,
    FOREIGN KEY (run_id) REFERENCES simulation_runs(run_id) ON DELETE CASCADE
);

-- 4. 服务点统计表
CREATE TABLE IF NOT EXISTS service_point_stats (
    stat_id INT AUTO_INCREMENT PRIMARY KEY,
    run_id INT NOT NULL,
    service_point_name VARCHAR(50) NOT NULL,
    total_served INT DEFAULT 0,
    avg_queue_length DOUBLE DEFAULT 0.0,
    max_queue_length INT DEFAULT 0,
    avg_waiting_time DOUBLE DEFAULT 0.0,
    utilization DOUBLE DEFAULT 0.0,
    FOREIGN KEY (run_id) REFERENCES simulation_runs(run_id) ON DELETE CASCADE
);

-- 5. 按用户类型统计表
CREATE TABLE IF NOT EXISTS user_type_stats (
    stat_id INT AUTO_INCREMENT PRIMARY KEY,
    run_id INT NOT NULL,
    user_type ENUM('NORMAL', 'PERSONAL_VIP', 'ENTERPRISE_VIP') NOT NULL,
    total_tasks INT DEFAULT 0,
    avg_system_time DOUBLE DEFAULT 0.0,
    avg_waiting_time DOUBLE DEFAULT 0.0,
    FOREIGN KEY (run_id) REFERENCES simulation_runs(run_id) ON DELETE CASCADE
);

-- 6. 按任务类型统计表
CREATE TABLE IF NOT EXISTS task_type_stats (
    stat_id INT AUTO_INCREMENT PRIMARY KEY,
    run_id INT NOT NULL,
    task_type ENUM('CPU', 'GPU') NOT NULL,
    total_tasks INT DEFAULT 0,
    avg_compute_time DOUBLE DEFAULT 0.0,
    avg_system_time DOUBLE DEFAULT 0.0,
    FOREIGN KEY (run_id) REFERENCES simulation_runs(run_id) ON DELETE CASCADE
);

-- 插入默认配置
INSERT INTO simulation_configs (
    config_name, mean_arrival_interval, simulation_time,
    num_cpu_nodes, num_gpu_nodes, cpu_task_probability,
    normal_user_probability, personal_vip_probability, enterprise_vip_probability
) VALUES
    ('Default Configuration', 2.0, 1000.0, 2, 1, 0.7, 0.6, 0.3, 0.1),
    ('High Load Configuration', 1.0, 1000.0, 3, 2, 0.7, 0.6, 0.3, 0.1),
    ('VIP Heavy Configuration', 2.0, 1000.0, 2, 1, 0.7, 0.3, 0.4, 0.3),
    ('GPU Intensive Configuration', 2.0, 1000.0, 2, 3, 0.3, 0.6, 0.3, 0.1);

-- 创建索引以提高查询性能
CREATE INDEX idx_tasks_run_id ON tasks(run_id);
CREATE INDEX idx_tasks_task_type ON tasks(task_type);
CREATE INDEX idx_tasks_user_type ON tasks(user_type);
CREATE INDEX idx_simulation_runs_status ON simulation_runs(status);
CREATE INDEX idx_simulation_runs_start_time ON simulation_runs(start_time);

