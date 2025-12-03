# 数据库设置指南 (Database Setup Guide)

## 前置条件 (Prerequisites)
- **MariaDB** 已安装并运行
- **HeidiSQL** 已安装（或其他MySQL客户端）
- MariaDB服务正在运行

## 第1步：创建数据库和表 (Step 1: Create Database and Tables)

### 方法A：使用HeidiSQL图形界面

1. 打开HeidiSQL
2. 连接到你的MariaDB服务器
   - 主机名：`localhost`
   - 用户名：`root`
   - 端口：`3306`
   - 密码：（你设置的root密码）

3. 点击"文件" → "运行SQL文件"
4. 选择项目中的SQL脚本：
   ```
   src/main/resources/database_schema.sql
   ```
5. 点击执行

### 方法B：使用命令行

在PowerShell或命令提示符中运行：

```powershell
# 进入MariaDB
mysql -u root -p

# 在MySQL提示符下，执行脚本
source C:/Users/cheny/IdeaProjects/OOP-Server-Simulation/src/main/resources/database_schema.sql

# 或者直接执行
mysql -u root -p < src/main/resources/database_schema.sql
```

## 第2步：配置数据库连接 (Step 2: Configure Database Connection)

编辑文件：`src/main/resources/database.properties`

```properties
# 数据库连接URL
db.url=jdbc:mariadb://localhost:3306/cloud_simulation

# 用户名（根据你的设置修改）
db.username=root

# 密码（根据你的设置修改）
db.password=你的密码

# 连接池设置（通常不需要修改）
db.pool.maximumPoolSize=10
db.pool.minimumIdle=2
db.pool.connectionTimeout=30000
db.pool.idleTimeout=600000
db.pool.maxLifetime=1800000
```

## 第3步：验证数据库设置 (Step 3: Verify Database Setup)

### 在HeidiSQL中验证：

1. 刷新数据库列表
2. 你应该看到 `cloud_simulation` 数据库
3. 展开该数据库，验证以下表已创建：
   - `simulation_configs` （仿真配置表）
   - `simulation_runs` （仿真运行记录表）
   - `tasks` （任务记录表）
   - `service_point_stats` （服务点统计表）
   - `user_type_stats` （用户类型统计表）
   - `task_type_stats` （任务类型统计表）

4. 查看 `simulation_configs` 表，应该有4条默认配置记录

### 在应用中验证：

1. 运行应用：`mvn clean javafx:run`
2. 在UI界面底部，查看"数据库连接"状态
3. 如果显示"✅ 已连接"，说明配置成功
4. 如果显示"❌ 未连接"，点击"测试"按钮查看错误信息

## 数据库表结构说明 (Database Schema Description)

### 1. simulation_configs（仿真配置表）
存储不同的仿真配置参数，可以保存和加载。

**主要字段：**
- `config_id`: 配置ID（主键）
- `config_name`: 配置名称
- `mean_arrival_interval`: 平均到达间隔
- `simulation_time`: 仿真时间
- `num_cpu_nodes`: CPU节点数
- `num_gpu_nodes`: GPU节点数
- 其他配置参数...

### 2. simulation_runs（仿真运行记录表）
记录每次仿真运行的基本信息和结果。

**主要字段：**
- `run_id`: 运行ID（主键）
- `config_id`: 使用的配置ID（外键）
- `run_name`: 运行名称
- `start_time`: 开始时间
- `end_time`: 结束时间
- `total_tasks_completed`: 完成任务总数
- `avg_system_time`: 平均系统时间
- `throughput`: 吞吐量
- `status`: 状态（RUNNING/COMPLETED/STOPPED/ERROR）

### 3. tasks（任务记录表）
记录每个任务的详细信息和时间戳。

**主要字段：**
- `task_id`: 任务ID（主键）
- `run_id`: 所属运行ID（外键）
- `task_type`: 任务类型（CPU/GPU）
- `user_type`: 用户类型（NORMAL/PERSONAL_VIP/ENTERPRISE_VIP）
- `arrival_time`: 到达时间
- `completion_time`: 完成时间
- `system_time`: 系统时间
- 各服务点的进出时间...

### 4. service_point_stats（服务点统计表）
存储各服务点的性能统计。

### 5. user_type_stats（用户类型统计表）
按用户类型统计性能指标。

### 6. task_type_stats（任务类型统计表）
按任务类型统计性能指标。

## 常见问题 (Common Issues)

### 问题1：无法连接到数据库

**解决方案：**
1. 确认MariaDB服务正在运行
2. 检查用户名和密码是否正确
3. 检查端口是否为3306
4. 在防火墙中允许MariaDB连接

### 问题2：数据库不存在

**解决方案：**
重新执行 `database_schema.sql` 脚本

### 问题3：权限不足

**解决方案：**
```sql
GRANT ALL PRIVILEGES ON cloud_simulation.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

## 数据库维护 (Database Maintenance)

### 清空所有仿真记录：
```sql
USE cloud_simulation;
DELETE FROM tasks;
DELETE FROM service_point_stats;
DELETE FROM user_type_stats;
DELETE FROM task_type_stats;
DELETE FROM simulation_runs;
```

### 重置自增ID：
```sql
ALTER TABLE simulation_runs AUTO_INCREMENT = 1;
ALTER TABLE tasks AUTO_INCREMENT = 1;
```

### 备份数据库：
```powershell
mysqldump -u root -p cloud_simulation > backup.sql
```

### 恢复数据库：
```powershell
mysql -u root -p cloud_simulation < backup.sql
```

## 查询示例 (Query Examples)

### 查看最近10次仿真运行：
```sql
SELECT run_id, run_name, start_time, total_tasks_completed, avg_system_time, throughput, status
FROM simulation_runs
ORDER BY start_time DESC
LIMIT 10;
```

### 查看特定运行的所有任务：
```sql
SELECT task_id, task_type, user_type, arrival_time, completion_time, system_time
FROM tasks
WHERE run_id = 1;
```

### 统计各用户类型的平均系统时间：
```sql
SELECT user_type, AVG(system_time) as avg_time, COUNT(*) as count
FROM tasks
WHERE run_id = 1
GROUP BY user_type;
```

## 使用JDBC连接（在代码中）

应用程序使用HikariCP连接池自动管理数据库连接。连接配置在 `DatabaseManager` 类中处理。

你不需要手动管理连接，只需确保 `database.properties` 文件配置正确即可。

---

如有问题，请检查：
1. MariaDB服务状态
2. database.properties文件配置
3. 应用程序日志输出

