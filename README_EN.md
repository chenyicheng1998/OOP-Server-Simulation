# Cloud Computing Service Queue Simulation System

## Overview
A discrete event simulation system for cloud computing service queues, built with JavaFX 20.0.1, FXML, Scene Builder, and MariaDB database integration.

## System Architecture

### User Types (Priority-based)
1. **Enterprise VIP** - Highest priority
2. **Personal VIP** - Medium priority  
3. **Normal User** - Lowest priority

### Task Types
- **CPU Tasks** (70% probability by default)
- **GPU Tasks** (30% probability by default)

### Service Flow
```
Task Arrival → Data Storage → Classification → CPU/GPU Queue → 
Compute Nodes → Result Storage → Task Complete
```

### Service Points
1. **Data Storage** - Initial data storage service
2. **Classification** - Task type classification (CPU/GPU)
3. **CPU Queue** - Priority queue for CPU tasks
4. **GPU Queue** - Priority queue for GPU tasks
5. **CPU Compute** - CPU computation nodes (configurable count)
6. **GPU Compute** - GPU computation nodes (configurable count)
7. **Result Storage** - Final result storage service

## Technology Stack

- **Java 17+**
- **Maven 3.6+**
- **JavaFX 20.0.1** (Controls, FXML, Graphics)
- **MariaDB** with JDBC Driver
- **HikariCP** - Connection pool management
- **Scene Builder** - Visual FXML editor

## Prerequisites

1. **JDK 17** or higher
2. **Maven 3.6+**
3. **MariaDB** database server
4. **HeidiSQL** or similar DB tool (recommended)
5. **Scene Builder** (optional, for UI editing)

## Quick Start

### 1. Setup Database

Using HeidiSQL or command line:
```bash
mysql -u root -p < src/main/resources/database_schema.sql
```

See detailed instructions: [DATABASE_SETUP.md](DATABASE_SETUP.md)

### 2. Configure Database Connection

Edit `src/main/resources/database.properties`:
```properties
db.url=jdbc:mariadb://localhost:3306/cloud_simulation
db.username=root
db.password=your_password
```

### 3. Compile and Run

```bash
# Clean and compile
mvn clean compile

# Run application
mvn javafx:run
```

## Features

### 1. User Interface (JavaFX + FXML)
- ✅ Real-time simulation visualization
- ✅ Dynamic statistics display
- ✅ Adjustable simulation speed (0.1x - 10x)
- ✅ Pause/Resume/Step-through execution
- ✅ Designed with Scene Builder

### 2. Database Integration (MariaDB)
- ✅ Save and load simulation configurations
- ✅ Record all simulation runs
- ✅ Store detailed task data
- ✅ Statistical analysis (by user type, task type, service point)
- ✅ Historical record queries
- ✅ HikariCP connection pool

### 3. Simulation Features
- ✅ Three-tier user priority scheduling
- ✅ Two task types (CPU/GPU)
- ✅ Multi-service-point workflow
- ✅ Probabilistic arrival and service times
- ✅ Real-time performance metrics

## Core Simulation Algorithm

### Discrete Event Simulation (Three-Phase Approach)
1. **Phase A**: Advance clock to next event time
2. **Phase B**: Execute due events (conditional events)
3. **Phase C**: Start activities that can begin (bound events)

### Priority Scheduling
- Enterprise VIP > Personal VIP > Normal User
- Same priority: FIFO (First-In-First-Out)

## Performance Metrics

- **Average System Time** - Mean time tasks spend in system
- **Throughput** - Tasks completed per second
- **Queue Length Statistics** - Average and maximum queue lengths
- **Service Point Utilization** - Percentage of time busy
- **User Type Statistics** - Performance by user type
- **Task Type Statistics** - Performance by task type

## Editing UI with Scene Builder

See detailed guide: [SCENE_BUILDER_GUIDE.md](SCENE_BUILDER_GUIDE.md)

1. Download Scene Builder: https://gluonhq.com/products/scene-builder/
2. Open FXML file: `src/main/resources/simulation_view.fxml`
3. Visually edit interface layout
4. Changes automatically reflect in application

## Project Structure

```
src/main/
├── java/com/simulation/
│   ├── SimulatorApp.java              # Application entry point
│   ├── controller/
│   │   └── SimulationController.java  # FXML controller (handles UI events)
│   ├── model/                         # Business logic layer
│   │   ├── Clock.java                 # Singleton clock
│   │   ├── Event.java                 # Event representation
│   │   ├── EventList.java             # Event queue (priority queue)
│   │   ├── EventType.java             # Event types enum
│   │   ├── ServicePoint.java          # Service point with queue
│   │   ├── SimulationConfig.java      # Configuration parameters
│   │   ├── SimulationEngine.java      # Main simulation engine (Thread)
│   │   ├── SimulationResults.java     # Results collector
│   │   ├── Task.java                  # Task entity
│   │   ├── TaskType.java              # CPU/GPU enum
│   │   └── UserType.java              # User priority enum
│   ├── database/                      # Database access layer
│   │   ├── DatabaseManager.java       # Connection pool manager
│   │   ├── SimulationConfigDAO.java   # Config CRUD operations
│   │   └── SimulationResultsDAO.java  # Results CRUD operations
│   ├── util/
│   │   └── RandomGenerator.java       # Random number generators
│   └── view/                          # Legacy view (deprecated)
└── resources/
    ├── simulation_view.fxml           # UI layout (Scene Builder compatible)
    ├── styles.css                     # CSS styling
    ├── database.properties            # Database configuration
    └── database_schema.sql            # Database schema script
```

## Database Schema

### Tables
1. **simulation_configs** - Stores simulation configurations
2. **simulation_runs** - Records each simulation run
3. **tasks** - Detailed task records
4. **service_point_stats** - Service point statistics
5. **user_type_stats** - User type performance
6. **task_type_stats** - Task type performance

See [DATABASE_SETUP.md](DATABASE_SETUP.md) for details.

## Configuration Parameters

### Arrival Parameters
- **Mean Arrival Interval** - Average time between task arrivals (exponential distribution)

### Simulation Parameters
- **Simulation Time** - Total simulation duration (seconds)
- **CPU Nodes** - Number of CPU compute nodes
- **GPU Nodes** - Number of GPU compute nodes
- **CPU Task Probability** - Probability a task is CPU type

### User Distribution
- **Normal User Probability** - Default: 0.6 (60%)
- **Personal VIP Probability** - Default: 0.3 (30%)
- **Enterprise VIP Probability** - Default: 0.1 (10%)

### Service Times (Mean values for exponential distribution)
- **Data Storage Service Time** - Default: 1.0s
- **Classification Service Time** - Default: 0.5s
- **CPU Compute Service Time** - Default: 5.0s
- **GPU Compute Service Time** - Default: 8.0s
- **Result Storage Service Time** - Default: 1.5s

## Using the Application

### Basic Operations
1. **Start** - Begin simulation with current configuration
2. **Pause** - Pause running simulation
3. **Resume** - Continue paused simulation
4. **Stop** - Stop and save results to database
5. **Reset** - Clear and reinitialize simulation

### Configuration Management
- **Save Config** - Save current parameters to database
- **Load Config** - Load previously saved configuration
- **View History** - View past simulation runs (coming soon)

### Real-time Monitoring
- Monitor current simulation time
- View task arrival and completion counts
- Track average system time
- Observe throughput rate
- Check queue statistics

### Database Connection
- Green checkmark (✅): Connected
- Red X (❌): Not connected
- Click "Test" to verify connection

## Generating Documentation

```bash
mvn javadoc:javadoc
```
Generated docs in: `target/site/apidocs/`

## Database Management

### View Simulation History
```sql
SELECT * FROM simulation_runs ORDER BY start_time DESC LIMIT 10;
```

### Clear All Data
```sql
DELETE FROM tasks;
DELETE FROM simulation_runs;
```

### Backup Database
```bash
mysqldump -u root -p cloud_simulation > backup.sql
```

See [DATABASE_SETUP.md](DATABASE_SETUP.md) for more operations.

## Extension Ideas

### Visualization Enhancements
- Add real-time charts (JavaFX Charts)
- Task flow animation
- Heat map for queue states

### Statistical Analysis
- Export Excel reports
- Multi-run comparison analysis
- Parameter sensitivity analysis

### Advanced Features
- Multi-scenario simulation
- Real-time parameter tuning
- Automatic optimization algorithms

## Troubleshooting

### Q1: Database connection fails
**A:** Check MariaDB service is running, verify credentials in `database.properties`

### Q2: FXML loading fails
**A:** Ensure `simulation_view.fxml` is in resources folder, controller path is correct

### Q3: Maven compilation errors
**A:** Run `mvn clean install` to re-download dependencies

### Q4: Application won't start
**A:** Check Java version (must be 17+), verify JavaFX dependencies

## Learning Resources

- **JavaFX Documentation**: https://openjfx.io/
- **Scene Builder**: https://gluonhq.com/products/scene-builder/
- **FXML Guide**: https://docs.oracle.com/javafx/2/fxml_get_started/jfxpub-fxml_get_started.htm
- **MariaDB**: https://mariadb.org/documentation/

## Development Team

Cloud Simulation Team - Metropolia University of Applied Sciences

## License

Educational Use - Metropolia University of Applied Sciences

---

**Note:** This is an academic project for learning discrete event simulation, MVC architecture, JavaFX development, and database integration.

For questions or issues, please refer to the documentation files or contact the development team.

