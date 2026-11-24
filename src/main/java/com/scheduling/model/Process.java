package com.scheduling.model;

public class Process {

    private String id;

    private int arrivalTime;
    private int burstTime;
    private int initialPriority;

    private int remainingTime;
    private int currentPriority;

    private int firstRunTime;
    private int finishTime;

    private boolean hasStarted;
    private boolean isFinished;

    public Process(String id, int arrivalTime, int burstTime, int initialPriority) {
        this.id              = id;
        this.arrivalTime     = arrivalTime;
        this.burstTime       = burstTime;
        this.initialPriority = initialPriority;
        this.remainingTime   = burstTime;
        this.currentPriority = initialPriority;
        this.firstRunTime    = -1;
        this.finishTime      = -1;
        this.hasStarted      = false;
        this.isFinished      = false;
    }

    public void markStarted(int currentTime) {
        if (!hasStarted) {
            this.firstRunTime = currentTime;
            this.hasStarted   = true;
        }
    }

    public void tick() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }

    public void markFinished(int currentTime) {
        this.finishTime = currentTime;
        this.isFinished = true;
    }

    public int getTurnaroundTime() {
        return finishTime - arrivalTime;
    }

    public int getWaitingTime() {
        return getTurnaroundTime() - burstTime;
    }

    public int getResponseTime() {
        return firstRunTime - arrivalTime;
    }

    public String getId()                               { return id; }
    public int getArrivalTime()                         { return arrivalTime; }
    public int getBurstTime()                           { return burstTime; }
    public int getInitialPriority()                     { return initialPriority; }
    public int getRemainingTime()                       { return remainingTime; }
    public void setRemainingTime(int remainingTime)     { this.remainingTime = remainingTime; }
    public int getCurrentPriority()                     { return currentPriority; }
    public void setCurrentPriority(int currentPriority) { this.currentPriority = currentPriority; }
    public int getFirstRunTime()                        { return firstRunTime; }
    public int getFinishTime()                          { return finishTime; }
    public boolean isFinished()                         { return isFinished; }
    public boolean hasStarted()                         { return hasStarted; }

    @Override
    public String toString() {
        return id + " (remaining: " + remainingTime + ", Priority: " + currentPriority + ")";
    }
}
