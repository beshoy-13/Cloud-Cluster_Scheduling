package com.scheduling.metrics;

import com.scheduling.model.Process;
import java.util.List;

/*
 * ============================================================
 *  Metrics
 * ============================================================
 *  Collects and computes scheduling statistics for a finished
 *  simulation run.
 *
 *  Per-process values come directly from Process getters
 *  (the formulas live in Process.java).
 *  This class adds the AVERAGES across all processes.
 * ============================================================
 */
public class Metrics {

    // The finished process list passed in after the simulation ends
    private List<Process> processes;

    // Averages computed once in calculate()
    private double averageTurnaroundTime;
    private double averageWaitingTime;
    private double averageResponseTime;

    public Metrics(List<Process> processes) {
        this.processes = processes;
    }

    /*
     * calculate()
     * ----------
     * Loops over all processes and sums up turnaround, waiting,
     * and response times, then divides by count to get averages.
     * Call this ONCE after the simulation finishes.
     */
    public void calculate() {
        double totalTurnaround = 0;
        double totalWaiting    = 0;
        double totalResponse   = 0;

        for (Process p : processes) {
            totalTurnaround += p.getTurnaroundTime();
            totalWaiting    += p.getWaitingTime();
            totalResponse   += p.getResponseTime();
        }

        int count = processes.size();
        averageTurnaroundTime = totalTurnaround / count;
        averageWaitingTime    = totalWaiting    / count;
        averageResponseTime   = totalResponse   / count;
    }

    // Getters — used by the Report class to print the results
    public List<Process> getProcesses()          { return processes; }
    public double getAverageTurnaroundTime()     { return averageTurnaroundTime; }
    public double getAverageWaitingTime()        { return averageWaitingTime; }
    public double getAverageResponseTime()       { return averageResponseTime; }
}
