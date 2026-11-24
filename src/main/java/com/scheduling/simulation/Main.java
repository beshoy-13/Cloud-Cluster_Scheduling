package com.scheduling.simulation;

import com.scheduling.model.Process;
import com.scheduling.scheduler.ACSScheduler;
import com.scheduling.scheduler.Scheduler;
import com.scheduling.scheduler.SJFScheduler;
import java.util.Arrays;
import java.util.List;

/*
 * Main — entry point for the scheduling simulation.
 *
 * Runs each test case through the SJF scheduler and prints results.
 * To add a new scheduler (e.g. ACS), create another Scheduler object
 * with the same process list and call run() — no other code changes needed.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("   Cloud-Cluster CPU Scheduling Sim     ");
        System.out.println("========================================");

        runTestCase1();
        runTestCase2();
        runTestCase3();
        runACSAssignmentCases();
    }

    // -----------------------------------------------------------------------
    // TEST CASE 1 — From the assignment (CS = 0, no context-switch overhead)
    // P1: arrival=0, burst=4
    // P2: arrival=1, burst=7
    // P3: arrival=3, burst=2
    //
    // Expected SJF results:
    //   P1 → finish=4,  TAT=4,  WT=0,  RT=0
    //   P3 → finish=6,  TAT=3,  WT=1,  RT=1
    //   P2 → finish=13, TAT=12, WT=5,  RT=5
    //   Avg TAT=6.33   Avg WT=2.00
    // -----------------------------------------------------------------------
    private static void runTestCase1() {
        System.out.println("\n--- Test Case 1 (Assignment TC1, CS=0) ---");

        // Each test case gets FRESH Process objects — the simulation mutates them.
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 4, 2),
                new Process("P2", 1, 7, 3),
                new Process("P3", 3, 2, 1)
        );

        // contextSwitchTime = 0 means preemptions are free (no overhead ticks)
        Scheduler scheduler = new SJFScheduler(processes, 0);
        scheduler.run();
    }

    // -----------------------------------------------------------------------
    // TEST CASE 2 — Verifies context-switch overhead (CS = 1)
    // P1: arrival=0, burst=10
    // P2: arrival=2, burst=3   ← arrives mid-run, preempts P1
    //
    // Expected SJF results (CS=1):
    //   P1 → finish=14, TAT=14, WT=4, RT=0
    //   P2 → finish=6,  TAT=4,  WT=1, RT=1   (1 CS tick at t=2)
    //   Avg TAT=9.00   Avg WT=2.50
    // -----------------------------------------------------------------------
    private static void runTestCase2() {
        System.out.println("\n--- Test Case 2 (Preemption + CS=1) ---");

        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 10, 1),
                new Process("P2", 2,  3, 2)
        );

        // contextSwitchTime = 1: each preemption costs 1 tick of CPU overhead
        Scheduler scheduler = new SJFScheduler(processes, 1);
        scheduler.run();
    }

    // -----------------------------------------------------------------------
    // TEST CASE 3 — All arrive at t=0 (pure non-preemptive SJF ordering), CS=0
    // P1: arrival=0, burst=5
    // P2: arrival=0, burst=3
    // P3: arrival=0, burst=1   ← shortest, should run first
    //
    // Expected SJF results:
    //   P3 → finish=1, TAT=1, WT=0, RT=0
    //   P2 → finish=4, TAT=4, WT=1, RT=1
    //   P1 → finish=9, TAT=9, WT=4, RT=4
    //   Avg TAT=4.67   Avg WT=1.67
    // -----------------------------------------------------------------------
    private static void runTestCase3() {
        System.out.println("\n--- Test Case 3 (All arrive at t=0, CS=0) ---");

        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 5, 1),
                new Process("P2", 0, 3, 2),
                new Process("P3", 0, 1, 3)
        );

        Scheduler scheduler = new SJFScheduler(processes, 0);
        scheduler.run();
    }

    private static void runACSAssignmentCases() {
        System.out.println("\n--- ACS Assignment Test Case 1 (Q=3, CS=0) ---");
        List<Process> firstCase = Arrays.asList(
                new Process("P1", 0, 4, 2),
                new Process("P2", 1, 7, 3),
                new Process("P3", 3, 2, 1)
        );
        new ACSScheduler(firstCase, 3, 0, 4).run();

        System.out.println("\n--- ACS Assignment Test Case 2 (Q=3, CS=1) ---");
        List<Process> secondCase = Arrays.asList(
                new Process("P1", 0, 5, 4),
                new Process("P2", 1, 6, 2),
                new Process("P3", 4, 4, 3)
        );
        new ACSScheduler(secondCase, 3, 1, 4).run();
    }
}
