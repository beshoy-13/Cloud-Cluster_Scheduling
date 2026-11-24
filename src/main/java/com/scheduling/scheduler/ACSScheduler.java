package com.scheduling.scheduler;

import com.scheduling.metrics.Metrics;
import com.scheduling.model.Process;
import com.scheduling.model.TickSnapshot;
import com.scheduling.report.Report;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * Adaptive Container Scheduler.
 *
 * <p>New processes use Tier 1 Round Robin. A process that consumes its
 * complete quantum is demoted to the non-preemptive Tier 2 priority queue.
 * Smaller effective priority values have precedence. Waiting Tier 2
 * processes age every configured number of ticks.</p>
 */
public class ACSScheduler implements Scheduler {

    private final List<Process> processes;
    private final int quantum;
    private final int contextSwitchTime;
    private final int agingInterval;
    private List<String> timeline = new ArrayList<>();
    private final List<TickSnapshot> snapshots = new ArrayList<>();
    private Metrics metrics;

    public ACSScheduler(
            List<Process> processes,
            int quantum,
            int contextSwitchTime,
            int agingInterval) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than zero");
        }
        if (contextSwitchTime < 0) {
            throw new IllegalArgumentException("Context-switch time cannot be negative");
        }
        if (agingInterval <= 0) {
            throw new IllegalArgumentException("Aging interval must be greater than zero");
        }
        this.processes = processes;
        this.quantum = quantum;
        this.contextSwitchTime = contextSwitchTime;
        this.agingInterval = agingInterval;
    }

    @Override
    public String getName() {
        return "Adaptive Container Scheduler";
    }

    @Override
    public List<Process> getProcesses() {
        return processes;
    }

    @Override
    public List<TickSnapshot> getTimeline() {
        return snapshots;
    }

    @Override
    public void run() {
        simulate();
        new Report(metrics, getName(), timeline).print();
    }

    @Override
    public void simulate() {
        timeline = new ArrayList<>();
        snapshots.clear();
        int currentTime = 0;
        int contextSwitchRemaining = 0;
        int quantumRemaining = 0;
        Process running = null;
        boolean runningTier2 = false;
        Deque<Process> tier1Queue = new ArrayDeque<>();
        List<Process> tier2Queue = new ArrayList<>();

        while (!allFinished()) {
            addArrivals(currentTime, tier1Queue);

            if (contextSwitchRemaining > 0) {
                log(currentTime, "CONTEXT SWITCH", -1, tier1Queue, tier2Queue);
                contextSwitchRemaining--;
                currentTime++;
                continue;
            }

            if (running == null) {
                running = selectNext(tier1Queue, tier2Queue, currentTime);
                if (running == null) {
                    log(currentTime, "IDLE", -1, tier1Queue, tier2Queue);
                    currentTime++;
                    continue;
                }

                runningTier2 = running.getTier() == 2;
                quantumRemaining = quantum;
            }

            log(currentTime, running.getId(), running.getRemainingTime(), tier1Queue, tier2Queue);
            running.markStarted(currentTime);
            running.tick();
            currentTime++;

            if (running.getRemainingTime() == 0) {
                running.markFinished(currentTime);
                removeFromTier2(running, tier2Queue);
                running = null;
                runningTier2 = false;
                if (contextSwitchTime > 0
                        && hasReadyWork(currentTime, tier1Queue, tier2Queue)) {
                    contextSwitchRemaining = contextSwitchTime;
                }
                continue;
            }

            if (runningTier2) {
                continue;
            }

            quantumRemaining--;
            if (quantumRemaining == 0) {
                Process demoted = running;
                demoted.setTier(2);
                demoted.setTier2EntryTime(currentTime);
                demoted.setCurrentPriority(effectivePriority(demoted, currentTime));
                tier2Queue.add(demoted);
                running = null;

                if (contextSwitchTime > 0) {
                    contextSwitchRemaining = contextSwitchTime;
                }
            }
        }

        metrics = new Metrics(processes);
        metrics.calculate();
    }

    private void log(
            int time,
            String label,
            int remaining,
            Deque<Process> tier1Queue,
            List<Process> tier2Queue) {
        timeline.add(formatTimelineEntry(
                time, label, remaining < 0 ? "-" : Integer.toString(remaining), tier1Queue, tier2Queue));
        snapshots.add(new TickSnapshot(
                time, label, remaining, describe(tier1Queue), describe(tier2Queue)));
    }

    private List<String> describe(Iterable<Process> queue) {
        List<String> result = new ArrayList<>();
        for (Process process : queue) {
            result.add(process.toString());
        }
        return result;
    }

    private void addArrivals(int currentTime, Deque<Process> tier1Queue) {
        for (Process process : processes) {
            if (!process.isFinished()
                    && process.getTier() == 1
                    && process.getArrivalTime() <= currentTime
                    && !process.hasStarted()
                    && !tier1Queue.contains(process)) {
                tier1Queue.addLast(process);
            }
        }
    }

    private Process selectNext(
            Deque<Process> tier1Queue,
            List<Process> tier2Queue,
            int currentTime) {
        if (!tier1Queue.isEmpty()) {
            return tier1Queue.removeFirst();
        }

        if (tier2Queue.isEmpty()) {
            return null;
        }

        for (Process process : tier2Queue) {
            process.setCurrentPriority(effectivePriority(process, currentTime));
        }
        return tier2Queue.stream()
                .min(Comparator
                        .comparingInt(Process::getCurrentPriority)
                        .thenComparingInt(Process::getArrivalTime)
                        .thenComparingInt(Process::getTier2EntryTime))
                .orElse(null);
    }

    private int effectivePriority(Process process, int currentTime) {
        int waitingTime = Math.max(0, currentTime - process.getTier2EntryTime());
        int agingSteps = waitingTime / agingInterval;
        return process.getInitialPriority() - agingSteps;
    }

    private void removeFromTier2(Process process, List<Process> tier2Queue) {
        tier2Queue.remove(process);
    }

    private boolean hasReadyWork(
            int currentTime,
            Deque<Process> tier1Queue,
            List<Process> tier2Queue) {
        if (!tier1Queue.isEmpty() || !tier2Queue.isEmpty()) {
            return true;
        }
        for (Process process : processes) {
            if (!process.isFinished()
                    && process.getTier() == 1
                    && process.getArrivalTime() <= currentTime) {
                return true;
            }
        }
        return false;
    }

    private String formatTimelineEntry(
            int time,
            String process,
            String remaining,
            Deque<Process> tier1Queue,
            List<Process> tier2Queue) {
        return String.format(
                "%-6d %-18s %-10s Tier 1: %-45s Tier 2: %s",
                time,
                process,
                remaining,
                formatQueue(tier1Queue),
                formatQueue(tier2Queue));
    }

    private String formatQueue(Iterable<Process> queue) {
        StringBuilder result = new StringBuilder("[");
        boolean first = true;
        for (Process process : queue) {
            if (!first) {
                result.append(", ");
            }
            result.append(process.getId())
                    .append(" (remaining: ")
                    .append(process.getRemainingTime())
                    .append(", Priority: ")
                    .append(process.getCurrentPriority())
                    .append(")");
            first = false;
        }
        return result.append(']').toString();
    }

    private boolean allFinished() {
        for (Process process : processes) {
            if (!process.isFinished()) {
                return false;
            }
        }
        return true;
    }
}
