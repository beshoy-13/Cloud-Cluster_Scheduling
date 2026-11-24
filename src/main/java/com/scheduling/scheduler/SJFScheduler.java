package com.scheduling.scheduler;

import com.scheduling.metrics.Metrics;
import com.scheduling.model.Process;
import com.scheduling.model.TickSnapshot;
import com.scheduling.report.Report;
import java.util.ArrayList;
import java.util.List;

/*
 * ============================================================
 *  Preemptive Shortest-Job First (SJF) Scheduler
 *  Also known as: Shortest Remaining Time First (SRTF)
 * ============================================================
 *
 *  CORE IDEA:
 *  At every clock tick, the CPU always goes to whichever READY
 *  process has the SMALLEST remainingTime.
 *  If a newly arrived process has a shorter remaining time than
 *  the one currently running, it PREEMPTS it immediately.
 *
 *  CONTEXT SWITCH (CS):
 *  Every time a preemption happens (a different process takes
 *  over the CPU), the CPU is BUSY doing the switch for
 *  `contextSwitchTime` ticks. During those ticks no process
 *  runs — the clock just advances. Once the switch completes,
 *  the shortest process is chosen again in case a new process
 *  arrived during the switch.
 *
 *  AFTER THE SIMULATION:
 *  Results are handed to Metrics (calculates averages) and
 *  then to Report (prints the table). No printing happens here.
 * ============================================================
 */
// implements Scheduler → fulfills the OOP contract defined in the interface
public class SJFScheduler implements Scheduler {

    // The full list of processes loaded from a test case
    private List<Process> processes;

    // How many ticks a context switch costs
    private int contextSwitchTime;

    private List<String> timeline = new ArrayList<>();
    private final List<TickSnapshot> snapshots = new ArrayList<>();
    private Metrics metrics;

    public SJFScheduler(List<Process> processes, int contextSwitchTime) {
        this.processes         = processes;
        this.contextSwitchTime = contextSwitchTime;
    }

    /*
     * getName() — required by the Scheduler interface.
     * Returns the human-readable label used by Report to title the output.
     */
    @Override
    public String getName() {
        return "Preemptive SJF";
    }

    /*
     * getProcesses() — required by the Scheduler interface.
     * Returns the process list after the simulation has run so that
     * the Simulation class can compare results across schedulers.
     */
    @Override
    public List<Process> getProcesses() {
        return processes;
    }

    /*
     * ----------------------------------------------------------
     *  run()  —  the main simulation loop
     * ----------------------------------------------------------
     *  Advances one tick at a time until every process finishes,
     *  then delegates to Metrics and Report for output.
     * ----------------------------------------------------------
     */
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

        int currentTime = 0;    // the simulation clock
        int csRemaining = 0;    // ticks left in the current context switch
        Process running = null; // process currently on the CPU (null = idle)
        timeline = new ArrayList<>();
        snapshots.clear();

        while (!allFinished()) {

            // A switch is an indivisible overhead interval. Choose the next
            // process after it finishes so arrivals during the switch count.
            if (csRemaining > 0) {
                log(currentTime, "CONTEXT SWITCH", -1, null);
                csRemaining--;
                currentTime++;
                continue;
            }

            List<Process> readyQueue = getReadyProcesses(currentTime);
            Process candidate = getShortestJob(readyQueue);

            if (candidate == null) {
                log(currentTime, "IDLE", -1, null);
                currentTime++;
                continue;
            }

            // A switch is charged only when an unfinished process is
            // preempted, matching the existing SJF test-case convention.
            if (running != null && running != candidate) {
                if (contextSwitchTime > 0) {
                    log(currentTime, "CONTEXT SWITCH", -1, null);
                    csRemaining = contextSwitchTime - 1;
                    running = null;
                    currentTime++;
                    continue;
                }
            }

            log(currentTime, candidate.getId(), candidate.getRemainingTime(), candidate);
            candidate.markStarted(currentTime); // records firstRunTime on first call
            candidate.tick();                   // remainingTime--
            running = candidate;

            if (candidate.getRemainingTime() == 0) {
                candidate.markFinished(currentTime + 1); // +1: tick just finished
                running = null;
            }

            currentTime++;
        }

        metrics = new Metrics(processes);
        metrics.calculate();
    }

    private void log(int time, String label, int remaining, Process onCpu) {
        timeline.add(formatTimelineEntry(
                time, label, remaining < 0 ? "-" : Integer.toString(remaining)));
        List<String> waiting = new ArrayList<>();
        for (Process p : getReadyProcesses(time)) {
            if (p != onCpu) {
                waiting.add(p.toString());
            }
        }
        snapshots.add(new TickSnapshot(time, label, remaining, waiting, new ArrayList<>()));
    }

    private String formatTimelineEntry(int time, String process, String remainingTime) {
        return String.format("%-10d %-20s %-15s", time, process, remainingTime);
    }

    /*
     * ----------------------------------------------------------
     *  getReadyProcesses()
     *  Returns all processes that have arrived and are not done.
     * ----------------------------------------------------------
     */
    private List<Process> getReadyProcesses(int currentTime) {
        List<Process> ready = new ArrayList<>();
        for (Process p : processes) {
            if (!p.isFinished() && p.getArrivalTime() <= currentTime) {
                ready.add(p);
            }
        }
        return ready;
    }

    /*
     * ----------------------------------------------------------
     *  getShortestJob()
     *  Scans the ready queue and returns the process with the
     *  smallest remainingTime — the SJF selection rule.
     *  Returns null if the queue is empty (CPU idles).
     * ----------------------------------------------------------
     */
    private Process getShortestJob(List<Process> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        Process shortest = null;
        for (Process p : readyQueue) {
            if (shortest == null) {
                shortest = p;
            } else if (p.getRemainingTime() < shortest.getRemainingTime()) {
                // Primary rule: smaller remaining time wins (SJF)
                shortest = p;
            } else if (p.getRemainingTime() == shortest.getRemainingTime()
                    && p.getArrivalTime() < shortest.getArrivalTime()) {
                // Tie-break rule: if two processes have EQUAL remaining time,
                // prefer the one that arrived FIRST (FCFS among equals).
                // Without this, the winner would be random depending on list order.
                shortest = p;
            }
        }
        return shortest;
    }

    /*
     * ----------------------------------------------------------
     *  allFinished()
     *  Returns true when every process has isFinished() == true.
     *  This is the loop termination condition.
     * ----------------------------------------------------------
     */
    private boolean allFinished() {
        for (Process p : processes) {
            if (!p.isFinished()) return false;
        }
        return true;
    }
}
