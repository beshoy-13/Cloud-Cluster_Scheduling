package com.scheduling.scheduler;

import com.scheduling.model.Process;
import com.scheduling.model.TickSnapshot;
import java.util.List;

/*
 * ============================================================
 *  Scheduler  —  Interface (OOP: Abstraction + Polymorphism)
 * ============================================================
 *
 *  WHY AN INTERFACE?
 *  -----------------
 *  Every scheduling algorithm in this project (SJF, ACS, ...)
 *  shares the SAME contract from the outside world:
 *    1. Receive a list of processes and configuration
 *    2. Run the simulation
 *    3. Return the finished, metrics-ready process list
 *
 *  By programming to this interface, the Simulation class never
 *  needs to know WHICH algorithm it is running. It just calls:
 *
 *      Scheduler s = new SJFScheduler(...);
 *      s.run();
 *      s.getProcesses();
 *
 *  OOP PRINCIPLES APPLIED:
 *  -----------------------
 *  - Abstraction   : callers depend on the interface, not the class
 *  - Polymorphism  : any Scheduler implementation can be swapped in
 *  - Open/Closed   : add new algorithms without touching existing code
 * ============================================================
 */
public interface Scheduler {

    /*
     * run()
     * -----
     * Executes the full scheduling simulation from time 0
     * until all processes finish.
     * Internally computes metrics and prints the report.
     */
    void run();

    void simulate();

    List<TickSnapshot> getTimeline();

    /*
     * getProcesses()
     * --------------
     * Returns the list of processes AFTER the simulation has run.
     * Each Process object holds its own computed metrics
     * (waiting time, turnaround time, response time).
     * Used by the Simulation class to collect results from
     * multiple schedulers and compare them.
     */
    List<Process> getProcesses();

    /*
     * getName()
     * ---------
     * Returns a human-readable name for this scheduler,
     * e.g. "Preemptive SJF" or "Adaptive Container Scheduler".
     * Used by the Report class to label the output table.
     */
    String getName();
}
