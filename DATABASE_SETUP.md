# Database Setup Guide

## Prerequisites

* MariaDB installed and running
* HeidiSQL installed (or any other MySQL client)
* MariaDB service running

---

## Step 1: Create Database and Tables

### Method A: Using HeidiSQL GUI

1. Open HeidiSQL
2. Connect to your MariaDB server

    * Hostname: `localhost`
    * Username: `root`
    * Port: `3306`
    * Password: (your root password)
3. Click **File → Run SQL file**
4. Select the SQL script in the project:

   ```
   src/main/resources/database_schema.sql
   ```
5. Click **Execute**

### Method B: Using Command Line

1. Open PowerShell or Command Prompt
2. Run:

   ```bash
   # Enter MariaDB
   mysql -u root -p

   # At the MySQL prompt, execute the script
   source C:/Users/cheny/IdeaProjects/OOP-Server-Simulation/src/main/resources/database_schema.sql
   ```

   Or directly:

   ```bash
   mysql -u root -p < src/main/resources/database_schema.sql
   ```

---

## Step 2: Configure Database Connection

Edit the file:

```
src/main/resources/database.properties
```

```properties
# Database URL
db.url=jdbc:mariadb://localhost:3306/cloud_simulation

# Username and password
db.username=root
db.password=your_password

# Connection pool settings (usually no need to modify)
db.pool.maximumPoolSize=10
db.pool.minimumIdle=2
db.pool.connectionTimeout=30000
db.pool.idleTimeout=600000
db.pool.maxLifetime=1800000
```

---

## Step 3: Verify Database Setup

### Using HeidiSQL

* Refresh the database list

* You should see the `cloud_simulation` database

* Expand it and verify the following tables:

    * `simulation_configs` (simulation configuration)
    * `simulation_runs` (simulation run records)
    * `tasks` (task records)
    * `service_point_stats` (service point stats)
    * `user_type_stats` (user type stats)
    * `task_type_stats` (task type stats)

* Check `simulation_configs` table for 4 default configuration records

### Using Application

* Run the app:

  ```bash
  mvn clean javafx:run
  ```
* At the bottom of the UI, check **Database Connection** status:

    * ✅ Connected → setup successful
    * ❌ Not Connected → click **Test** to see errors

---

## Database Schema Description

### 1. simulation_configs

* Stores simulation configuration parameters
* **Fields:**

    * `config_id`: primary key
    * `config_name`
    * `mean_arrival_interval`
    * `simulation_time`
    * `num_cpu_nodes`
    * `num_gpu_nodes`
    * Other parameters...

### 2. simulation_runs

* Records each simulation run and results
* **Fields:**

    * `run_id`: primary key
    * `config_id`: foreign key
    * `run_name`
    * `start_time`, `end_time`
    * `total_tasks_completed`
    * `avg_system_time`
    * `throughput`
    * `status`: RUNNING/COMPLETED/STOPPED/ERROR

### 3. tasks

* Detailed task records with timestamps
* **Fields:**

    * `task_id`: primary key
    * `run_id`: foreign key
    * `task_type`: CPU/GPU
    * `user_type`: NORMAL/PERSONAL_VIP/ENTERPRISE_VIP
    * `arrival_time`, `completion_time`, `system_time`
    * Service point entry/exit times

### 4. service_point_stats

* Performance statistics for each service point

### 5. user_type_stats

* Performance stats grouped by user type

### 6. task_type_stats

* Performance stats grouped by task type

---

## Common Issues

**Issue 1: Cannot connect to database**

* Ensure MariaDB service is running
* Check username/password
* Ensure port 3306 is open
* Allow MariaDB through firewall

**Issue 2: Database does not exist**

* Re-run `database_schema.sql` script

**Issue 3: Insufficient privileges**

```sql
GRANT ALL PRIVILEGES ON cloud_simulation.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

---

## Database Maintenance

**Clear all simulation records**

```sql
USE cloud_simulation;
DELETE FROM tasks;
DELETE FROM service_point_stats;
DELETE FROM user_type_stats;
DELETE FROM task_type_stats;
DELETE FROM simulation_runs;
```

**Reset auto-increment IDs**

```sql
ALTER TABLE simulation_runs AUTO_INCREMENT = 1;
ALTER TABLE tasks AUTO_INCREMENT = 1;
```

**Backup database**

```bash
mysqldump -u root -p cloud_simulation > backup.sql
```

**Restore database**

```bash
mysql -u root -p cloud_simulation < backup.sql
```

---

## Query Examples

**View last 10 simulation runs**

```sql
SELECT run_id, run_name, start_time, total_tasks_completed, avg_system_time, throughput, status
FROM simulation_runs
ORDER BY start_time DESC
LIMIT 10;
```

**View all tasks for a specific run**

```sql
SELECT task_id, task_type, user_type, arrival_time, completion_time, system_time
FROM tasks
WHERE run_id = 1;
```

**Average system time per user type**

```sql
SELECT user_type, AVG(system_time) as avg_time, COUNT(*) as count
FROM tasks
WHERE run_id = 1
GROUP BY user_type;
```

---

## JDBC Connection in Code

* The application uses **HikariCP** connection pool
* Connection configuration handled by `DatabaseManager` class
* No manual connection management required, ensure `database.properties` is correct

**Check if issues persist:**

* MariaDB service status
* `database.properties` configuration
* Application logs
