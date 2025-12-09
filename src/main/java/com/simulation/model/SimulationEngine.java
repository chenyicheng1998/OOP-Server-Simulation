package com.simulation.model;
import com.simulation.util.RandomGenerator;

/**
 * Core discrete-event simulation engine for cloud computing service queue simulation.
 * 
 * <p>This class extends {@link Thread} and implements a discrete-event simulation (DES)
 * engine that models a cloud computing service provider. The simulation processes tasks
 * through a series of service points:
 * <ol>
 *   <li>Data Storage</li>
 *   <li>Classification</li>
 *   <li>CPU/GPU Queue</li>
 *   <li>CPU/GPU Compute</li>
 *   <li>Result Storage</li>
 * </ol>
 * 
 * <p>The engine uses an event-driven approach where events are scheduled and processed
 * in chronological order. Tasks flow through the system based on their type (CPU/GPU)
 * and user priority (Normal/Personal VIP/Enterprise VIP).
 * 
 * <p>Thread Safety: This class is thread-safe for concurrent access from UI thread.
 * The simulation runs in its own thread and uses synchronized methods in ServicePoint
 * and SimulationResults for thread-safe data access.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 * @see SimulationConfig
 * @see SimulationResults
 * @see ServicePoint
 * @see Task
 * @see Event
 */
public class SimulationEngine extends Thread {
    private final SimulationConfig config;
    private final SimulationResults results;
    private final EventList eventList;
    private final Clock clock;
    private ServicePoint dataStorage, classification, cpuQueue, gpuQueue, cpuCompute, gpuCompute, resultStorage;
    private volatile boolean running, paused;
    private volatile double speedMultiplier = 1.0;
    private final Object pauseLock = new Object();
    private SimulationListener listener;
    /**
     * Listener interface for simulation events.
     * 
     * <p>Implementations of this interface receive notifications when simulation
     * time updates or when the simulation completes. Used to update UI components
     * in real-time during simulation execution.
     * 
     * @since 2.0
     */
    public interface SimulationListener {
        /**
         * Called when simulation time advances.
         * 
         * @param time the current simulation time
         */
        void onTimeUpdate(double time);
        
        /**
         * Called when simulation completes normally or is stopped.
         */
        void onSimulationComplete();
        
        /**
         * Default implementation that calls onTimeUpdate with current clock time.
         */
        default void onSimulationUpdate() {
            onTimeUpdate(Clock.getInstance().getTime());
        }
    }
    /**
     * Constructs a new SimulationEngine with the given configuration.
     * 
     * @param config the simulation configuration parameters
     * @throws NullPointerException if config is null
     */
    public SimulationEngine(SimulationConfig config) {
        this.config = config;
        this.results = new SimulationResults();
        this.eventList = new EventList();
        this.clock = Clock.getInstance();
        initializeServicePoints();
    }
    private void initializeServicePoints() {
        dataStorage = new ServicePoint("Data Storage", 1, config.getDataStorageServiceTime(), false);
        classification = new ServicePoint("Classification", 1, config.getClassificationServiceTime(), false);
        cpuQueue = new ServicePoint("CPU Queue", Integer.MAX_VALUE, 0, true);
        gpuQueue = new ServicePoint("GPU Queue", Integer.MAX_VALUE, 0, true);
        cpuCompute = new ServicePoint("CPU Compute", config.getNumCpuNodes(), config.getCpuComputeServiceTime(), false);
        gpuCompute = new ServicePoint("GPU Compute", config.getNumGpuNodes(), config.getGpuComputeServiceTime(), false);
        resultStorage = new ServicePoint("Result Storage", 1, config.getResultStorageServiceTime(), false);
    }
    /**
     * Sets the listener for simulation events.
     * 
     * @param listener the listener to receive simulation updates, or null to remove listener
     */
    public void setListener(SimulationListener listener) {
        this.listener = listener;
    }
    
    /**
     * Sets the simulation speed multiplier.
     * 
     * <p>The speed multiplier controls how fast simulation time advances relative
     * to real time. A value of 1.0 means 1 simulation second = 1 real second.
     * A value of 2.0 means simulation runs twice as fast.
     * 
     * @param speed the speed multiplier (clamped between 0.1 and 100.0)
     */
    public void setSpeed(double speed) {
        this.speedMultiplier = Math.max(0.1, Math.min(100.0, speed));
    }
    
    /**
     * Initializes the simulation engine.
     * 
     * <p>This method resets the clock, clears all events, resets statistics,
     * and schedules the first arrival event. Must be called before starting
     * the simulation.
     */
    public void initialize() {
        clock.reset();
        eventList.clear();
        results.reset();
        dataStorage.reset();
        classification.reset();
        cpuCompute.reset();
        gpuCompute.reset();
        resultStorage.reset();
        scheduleNextArrival();
    }
    private void scheduleNextArrival() {
        double arrivalTime = clock.getTime() + RandomGenerator.exponential(config.getMeanArrivalInterval());
        if (arrivalTime <= config.getSimulationTime()) {
            TaskType taskType = RandomGenerator.bernoulli(config.getCpuTaskProbability()) ? TaskType.CPU : TaskType.GPU;
            UserType userType = generateUserType();
            Task task = new Task(taskType, userType, arrivalTime);
            eventList.addEvent(new Event(EventType.ARRIVAL, arrivalTime, task));
        }
    }
    private UserType generateUserType() {
        double rand = Math.random();
        if (rand < config.getNormalUserProbability()) return UserType.NORMAL;
        else if (rand < config.getNormalUserProbability() + config.getPersonalVipProbability()) return UserType.PERSONAL_VIP;
        else return UserType.ENTERPRISE_VIP;
    }
    @Override
    public void run() {
        running = true;
        double previousTime = 0.0;

        while (running && eventList.hasEvents() && clock.getTime() <= config.getSimulationTime()) {
            synchronized (pauseLock) {
                while (paused && running) {
                    try { pauseLock.wait(); } catch (InterruptedException e) { return; }
                }
            }
            if (!running) break;
            Event event = eventList.getNextEvent();
            if (event != null) {
                double targetTime = event.getTime();
                double timeElapsed = targetTime - previousTime;

                // Sleep proportional to simulated time elapsed, but break into small chunks
                // for smooth time display updates
                // At 1x speed: 1 simulated second = 1 real second (1000ms)
                // At 2x speed: 1 simulated second = 0.5 real seconds (500ms)
                if (timeElapsed > 0) {
                    // Capture current speed to avoid inconsistency if slider is adjusted during sleep
                    double currentSpeed = speedMultiplier;
                    long totalSleepTime = (long)(timeElapsed * 1000.0 / currentSpeed);
                    long sleepChunk = 50; // Update UI every 50ms for smooth display
                    long initialSleepTime = totalSleepTime;

                    // Break the sleep into small chunks and update time progressively
                    while (totalSleepTime > 0 && running && !paused) {
                        long currentSleep = Math.min(sleepChunk, totalSleepTime);
                        try {
                            Thread.sleep(currentSleep);
                        } catch (InterruptedException e) {
                            return;
                        }

                        // Update clock progressively for smooth display
                        // Use initial sleep time to calculate consistent progress
                        double progress = (double)(initialSleepTime - totalSleepTime + currentSleep) / initialSleepTime;
                        double intermediateTime = previousTime + (timeElapsed * progress);
                        clock.setTime(intermediateTime);

                        if (listener != null) listener.onTimeUpdate(clock.getTime());

                        totalSleepTime -= currentSleep;
                    }
                }

                clock.setTime(targetTime);
                processEvent(event);
                previousTime = targetTime;

                if (listener != null) listener.onTimeUpdate(clock.getTime());
            }
        }
        running = false;
        results.setTotalSimulationTime(clock.getTime());
        if (listener != null) listener.onSimulationComplete();
    }
    private void processEvent(Event event) {
        Task task = event.getTask();
        switch (event.getType()) {
            case ARRIVAL: handleArrival(task); break;
            case DEP_DATA_STORAGE: handleDepartureDataStorage(task); break;
            case DEP_CLASSIFICATION: handleDepartureClassification(task); break;
            case DEP_CPU_COMPUTE: handleDepartureCpuCompute(task); break;
            case DEP_GPU_COMPUTE: handleDepartureGpuCompute(task); break;
            case DEP_RESULT_STORAGE: handleDepartureResultStorage(task); break;
        }
    }
    private void handleArrival(Task task) {
        results.recordArrival();
        dataStorage.addToQueue(task);
        if (dataStorage.isAvailable()) startServiceDataStorage();
        scheduleNextArrival();
    }
    private void startServiceDataStorage() {
        Task task = dataStorage.beginService(clock.getTime());
        if (task != null) {
            double serviceTime = dataStorage.getServiceTime();
            eventList.addEvent(new Event(EventType.DEP_DATA_STORAGE, clock.getTime() + serviceTime, task));
        }
    }
    private void handleDepartureDataStorage(Task task) {
        dataStorage.endService(clock.getTime());
        classification.addToQueue(task);
        if (classification.isAvailable()) startServiceClassification();
        if (dataStorage.isAvailable() && !dataStorage.isQueueEmpty()) startServiceDataStorage();
    }
    private void startServiceClassification() {
        Task task = classification.beginService(clock.getTime());
        if (task != null) {
            double serviceTime = classification.getServiceTime();
            eventList.addEvent(new Event(EventType.DEP_CLASSIFICATION, clock.getTime() + serviceTime, task));
        }
    }
    private void handleDepartureClassification(Task task) {
        classification.endService(clock.getTime());
        // Add task directly to CPU/GPU compute service point (which now has its own queue)
        if (task.getTaskType() == TaskType.CPU) {
            cpuCompute.addToQueue(task);
            if (cpuCompute.isAvailable()) startServiceCpuCompute();
        } else {
            gpuCompute.addToQueue(task);
            if (gpuCompute.isAvailable()) startServiceGpuCompute();
        }
        if (classification.isAvailable() && !classification.isQueueEmpty()) startServiceClassification();
    }

    private void startServiceCpuCompute() {
        Task task = cpuCompute.beginService(clock.getTime());
        if (task != null) {
            double serviceTime = cpuCompute.getServiceTime();
            eventList.addEvent(new Event(EventType.DEP_CPU_COMPUTE, clock.getTime() + serviceTime, task));
        }
    }

    private void startServiceGpuCompute() {
        Task task = gpuCompute.beginService(clock.getTime());
        if (task != null) {
            double serviceTime = gpuCompute.getServiceTime();
            eventList.addEvent(new Event(EventType.DEP_GPU_COMPUTE, clock.getTime() + serviceTime, task));
        }
    }

    private void handleDepartureCpuCompute(Task task) {
        cpuCompute.endService(clock.getTime());
        resultStorage.addToQueue(task);
        if (resultStorage.isAvailable()) startServiceResultStorage();
        if (cpuCompute.isAvailable() && !cpuCompute.isQueueEmpty()) startServiceCpuCompute();
    }

    private void handleDepartureGpuCompute(Task task) {
        gpuCompute.endService(clock.getTime());
        resultStorage.addToQueue(task);
        if (resultStorage.isAvailable()) startServiceResultStorage();
        if (gpuCompute.isAvailable() && !gpuCompute.isQueueEmpty()) startServiceGpuCompute();
    }
    private void startServiceResultStorage() {
        Task task = resultStorage.beginService(clock.getTime());
        if (task != null) {
            double serviceTime = resultStorage.getServiceTime();
            eventList.addEvent(new Event(EventType.DEP_RESULT_STORAGE, clock.getTime() + serviceTime, task));
        }
    }
    private void handleDepartureResultStorage(Task task) {
        resultStorage.endService(clock.getTime());
        task.setCompletionTime(clock.getTime());
        results.recordCompletion(task);
        if (resultStorage.isAvailable() && !resultStorage.isQueueEmpty()) startServiceResultStorage();
    }
    /**
     * Checks if the simulation is currently running.
     * 
     * @return true if simulation is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Checks if the simulation is currently paused.
     * 
     * @return true if simulation is paused, false otherwise
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Pauses the simulation execution.
     * 
     * <p>The simulation thread will wait until {@link #resumeSimulation()} is called.
     */
    public void pauseSimulation() {
        paused = true;
    }

    /**
     * Resumes a paused simulation.
     * 
     * <p>This method wakes up the simulation thread if it's waiting due to pause.
     */
    public void resumeSimulation() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    /**
     * Stops the simulation execution.
     * 
     * <p>The simulation will finish processing the current event and then stop.
     * The thread will exit after completing current operations.
     */
    public void stopSimulation() {
        running = false;
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    /**
     * Executes a single simulation step when paused.
     * 
     * <p>This method processes the next event in the queue. Only works when
     * simulation is paused. Useful for debugging or step-by-step execution.
     */
    public void stepForward() {
        if (paused && eventList.hasEvents()) {
            Event event = eventList.getNextEvent();
            if (event != null) {
                clock.setTime(event.getTime());
                processEvent(event);
                if (listener != null) listener.onTimeUpdate(clock.getTime());
            }
        }
    }

    /**
     * Gets the simulation results.
     * 
     * @return the SimulationResults object containing all statistics
     */
    public SimulationResults getResults() {
        return results;
    }
    
    /**
     * Gets the Data Storage service point.
     * 
     * @return the Data Storage ServicePoint
     */
    public ServicePoint getDataStorage() { return dataStorage; }
    
    /**
     * Gets the Classification service point.
     * 
     * @return the Classification ServicePoint
     */
    public ServicePoint getClassification() { return classification; }
    
    /**
     * Gets the CPU Compute service point.
     * 
     * @return the CPU Compute ServicePoint
     */
    public ServicePoint getCpuCompute() { return cpuCompute; }
    
    /**
     * Gets the GPU Compute service point.
     * 
     * @return the GPU Compute ServicePoint
     */
    public ServicePoint getGpuCompute() { return gpuCompute; }
    
    /**
     * Gets the Result Storage service point.
     * 
     * @return the Result Storage ServicePoint
     */
    public ServicePoint getResultStorage() { return resultStorage; }
}
