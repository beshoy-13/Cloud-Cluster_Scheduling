package com.scheduling.report;

import com.scheduling.metrics.Metrics;
import com.scheduling.model.Process;
import java.util.List;

/*
 * Report — prints the final results table.
 * All values come from Metrics; zero math happens here.
 */
public class Report {

    // Extracted into a constant so it appears only once (DRY principle).
    // If you change the column width, you only change this number.
    private static final String SEPARATOR = buildSeparator(90);

    private Metrics metrics;
    private String  schedulerName;
    private List<String> timeline;

    public Report(Metrics metrics, String schedulerName) {
        this(metrics, schedulerName, null);
    }

    public Report(Metrics metrics, String schedulerName, List<String> timeline) {
        this.metrics       = metrics;
        this.schedulerName = schedulerName;
        this.timeline = timeline;
    }

    public void print() {
        System.out.println("\n===== " + schedulerName + " Results =====");

        if (timeline != null) {
            System.out.println("\n--- Execution Timeline ---");
            if (schedulerName.contains("Adaptive Container")) {
                System.out.printf("%-6s %-18s %-10s %-55s %-55s%n",
                        "Time", "Process", "Remaining", "Tier 1", "Tier 2");
                System.out.println(buildSeparator(150));
            } else {
                System.out.printf("%-10s %-20s %-15s%n", "Time", "CPU", "Remaining");
                System.out.println(buildSeparator(45));
            }
            for (String entry : timeline) {
                System.out.println(entry);
            }
        }

        // Header row
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-15s%n",
                "Process", "Arrival", "Finish", "Turnaround", "Waiting", "Response");
        System.out.println(SEPARATOR);

        // One data row per process
        for (Process p : metrics.getProcesses()) {
            System.out.printf("%-10s %-15d %-15d %-20d %-15d %-15d%n",
                    p.getId(),
                    p.getArrivalTime(),
                    p.getFinishTime(),
                    p.getTurnaroundTime(),
                    p.getWaitingTime(),
                    p.getResponseTime());
        }

        // Footer separator + averages
        System.out.println(SEPARATOR);
        System.out.printf("Average Turnaround Time : %.2f%n", metrics.getAverageTurnaroundTime());
        System.out.printf("Average Waiting Time    : %.2f%n", metrics.getAverageWaitingTime());
        System.out.printf("Average Response Time   : %.2f%n", metrics.getAverageResponseTime());
    }

    // Builds a string of `n` dashes — called once at class-load time.
    private static String buildSeparator(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append('-');
        return sb.toString();
    }
}
