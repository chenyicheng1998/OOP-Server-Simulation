package com.simulation.model;
public class SimulationConfig {
    private double meanArrivalInterval = 1.0;
    private double dataStorageServiceTime = 1.0;
    private double classificationServiceTime = 0.5;
    private double cpuComputeServiceTime = 5.0;
    private double gpuComputeServiceTime = 8.0;
    private double resultStorageServiceTime = 1.5;
    private int numCpuNodes = 4;
    private int numGpuNodes = 3;
    private double cpuTaskProbability = 0.7;
    private double normalUserProbability = 0.6;
    private double personalVipProbability = 0.3;
    private double simulationTime = 1000.0;
    private double speedMultiplier = 1.0;
    public double getMeanArrivalInterval() { return meanArrivalInterval; }
    public void setMeanArrivalInterval(double v) { this.meanArrivalInterval = v; }
    public double getDataStorageServiceTime() { return dataStorageServiceTime; }
    public void setDataStorageServiceTime(double v) { this.dataStorageServiceTime = v; }
    public double getClassificationServiceTime() { return classificationServiceTime; }
    public void setClassificationServiceTime(double v) { this.classificationServiceTime = v; }
    public double getCpuComputeServiceTime() { return cpuComputeServiceTime; }
    public void setCpuComputeServiceTime(double v) { this.cpuComputeServiceTime = v; }
    public double getGpuComputeServiceTime() { return gpuComputeServiceTime; }
    public void setGpuComputeServiceTime(double v) { this.gpuComputeServiceTime = v; }
    public double getResultStorageServiceTime() { return resultStorageServiceTime; }
    public void setResultStorageServiceTime(double v) { this.resultStorageServiceTime = v; }
    public int getNumCpuNodes() { return numCpuNodes; }
    public void setNumCpuNodes(int v) { this.numCpuNodes = v; }
    public int getNumGpuNodes() { return numGpuNodes; }
    public void setNumGpuNodes(int v) { this.numGpuNodes = v; }
    public double getCpuTaskProbability() { return cpuTaskProbability; }
    public void setCpuTaskProbability(double v) { this.cpuTaskProbability = v; }
    public double getNormalUserProbability() { return normalUserProbability; }
    public void setNormalUserProbability(double v) { this.normalUserProbability = v; }
    public double getPersonalVipProbability() { return personalVipProbability; }
    public void setPersonalVipProbability(double v) { this.personalVipProbability = v; }
    public double getSimulationTime() { return simulationTime; }
    public void setSimulationTime(double v) { this.simulationTime = v; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(double v) { this.speedMultiplier = v; }
    public double getEnterpriseVipProbability() { return 1.0 - normalUserProbability - personalVipProbability; }
    public void setEnterpriseVipProbability(double v) {
        // Adjust normalUserProbability to maintain total probability = 1.0
        // Keep personalVipProbability unchanged if possible
        this.normalUserProbability = Math.max(0, 1.0 - personalVipProbability - v);
    }
}
