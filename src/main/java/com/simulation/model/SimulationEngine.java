package com.simulation.model;
import com.simulation.util.RandomGenerator;
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
     * Listener interface for simulation events
     */
    public interface SimulationListener {
        void onTimeUpdate(double time);
        void onSimulationComplete();
        default void onSimulationUpdate() {
            onTimeUpdate(Clock.getInstance().getTime());
        }
    }
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
    public void setListener(SimulationListener listener) {
        this.listener = listener;
    }
    public void setSpeed(double speed) {
        this.speedMultiplier = Math.max(0.1, Math.min(100.0, speed));
    }
    public void initialize() {
        clock.reset();
        eventList.clear();
        results.reset();
        dataStorage.reset(); classification.reset(); cpuQueue.reset(); gpuQueue.reset();
        cpuCompute.reset(); gpuCompute.reset(); resultStorage.reset();
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
        dataStorage.endService();
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
        classification.endService();
        if (task.getTaskType() == TaskType.CPU) {
            cpuQueue.addToQueue(task);
            if (cpuCompute.isAvailable()) startServiceCpuCompute();
        } else {
            gpuQueue.addToQueue(task);
            if (gpuCompute.isAvailable()) startServiceGpuCompute();
        }
        if (classification.isAvailable() && !classification.isQueueEmpty()) startServiceClassification();
    }
    private void startServiceCpuCompute() {
        Task task = cpuQueue.beginService(clock.getTime());
        if (task != null) {
            double serviceTime = cpuCompute.getServiceTime();
            eventList.addEvent(new Event(EventType.DEP_CPU_COMPUTE, clock.getTime() + serviceTime, task));
        }
    }
    private void startServiceGpuCompute() {
        Task task = gpuQueue.beginService(clock.getTime());
        if (task != null) {
            double serviceTime = gpuCompute.getServiceTime();
            eventList.addEvent(new Event(EventType.DEP_GPU_COMPUTE, clock.getTime() + serviceTime, task));
        }
    }
    private void handleDepartureCpuCompute(Task task) {
        cpuCompute.endService();
        resultStorage.addToQueue(task);
        if (resultStorage.isAvailable()) startServiceResultStorage();
        if (cpuCompute.isAvailable() && !cpuQueue.isQueueEmpty()) startServiceCpuCompute();
    }
    private void handleDepartureGpuCompute(Task task) {
        gpuCompute.endService();
        resultStorage.addToQueue(task);
        if (resultStorage.isAvailable()) startServiceResultStorage();
        if (gpuCompute.isAvailable() && !gpuQueue.isQueueEmpty()) startServiceGpuCompute();
    }
    private void startServiceResultStorage() {
        Task task = resultStorage.beginService(clock.getTime());
        if (task != null) {
            double serviceTime = resultStorage.getServiceTime();
            eventList.addEvent(new Event(EventType.DEP_RESULT_STORAGE, clock.getTime() + serviceTime, task));
        }
    }
    private void handleDepartureResultStorage(Task task) {
        resultStorage.endService();
        task.setCompletionTime(clock.getTime());
        results.recordCompletion(task);
        if (resultStorage.isAvailable() && !resultStorage.isQueueEmpty()) startServiceResultStorage();
    }
    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pauseSimulation() {
        paused = true;
    }

    public void resumeSimulation() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public void stopSimulation() {
        running = false;
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

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

    public SimulationResults getResults() {
        return results;
    }
    public ServicePoint getDataStorage() { return dataStorage; }
    public ServicePoint getClassification() { return classification; }
    public ServicePoint getCpuQueue() { return cpuQueue; }
    public ServicePoint getGpuQueue() { return gpuQueue; }
    public ServicePoint getCpuCompute() { return cpuCompute; }
    public ServicePoint getGpuCompute() { return gpuCompute; }
    public ServicePoint getResultStorage() { return resultStorage; }
}
