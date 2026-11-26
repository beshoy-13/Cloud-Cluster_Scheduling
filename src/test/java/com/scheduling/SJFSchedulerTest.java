package com.scheduling;

import com.scheduling.model.Process;
import com.scheduling.scheduler.SJFScheduler;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 * SJFSchedulerTest
 * ----------------
 * JUnit 4 tests for the Preemptive SJF algorithm.
 *
 * HOW TO RUN:
 *   mvn test                        (runs all tests in the project)
 *   mvn test -Dtest=SJFSchedulerTest (runs only this test class)
 *
 * IMPORTANT: Each @Test method builds FRESH Process objects.
 * Never reuse a process list across tests — the scheduler mutates
 * remainingTime, finishTime, firstRunTime etc. inside each Process.
 */
public class SJFSchedulerTest {

    // A tiny helper: runs the scheduler and returns the process list.
    // The process objects now carry all computed metrics inside them.
    private List<Process> runSJF(List<Process> processes, int contextSwitchTime) {
        new SJFScheduler(processes, contextSwitchTime).run();
        return processes;
    }

    // Helper to look up a process by ID in the list
    private Process find(List<Process> list, String id) {
        for (Process p : list) {
            if (p.getId().equals(id)) return p;
        }
        throw new IllegalArgumentException("Process " + id + " not found");
    }

    // =======================================================================
    // TEST CASE 1 — Assignment test case 1 processes, CS = 0
    //
    // P1(arr=0, burst=4), P2(arr=1, burst=7), P3(arr=3, burst=2)
    //
    // SJF timeline (no CS overhead):
    //   t=0-3  : P1 runs (burst=4, always shortest while running)
    //   t=4-5  : P3 runs (burst=2 < P2.burst=7)
    //   t=6-12 : P2 runs (only one left)
    //
    //   P1: finish=4,  TAT=4-0=4,  WT=4-4=0,   RT=0-0=0
    //   P3: finish=6,  TAT=6-3=3,  WT=3-2=1,   RT=4-3=1
    //   P2: finish=13, TAT=13-1=12, WT=12-7=5, RT=6-1=5
    // =======================================================================
    @Test
    public void testAssignmentTC1_CS0() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 4, 2),
                new Process("P2", 1, 7, 3),
                new Process("P3", 3, 2, 1)
        );
        runSJF(processes, 0);

        Process p1 = find(processes, "P1");
        // P1 finishes at t=4 without ever being preempted
        assertEquals("P1 finish time",       4,  p1.getFinishTime());
        assertEquals("P1 turnaround time",   4,  p1.getTurnaroundTime());
        assertEquals("P1 waiting time",      0,  p1.getWaitingTime());
        assertEquals("P1 response time",     0,  p1.getResponseTime());

        Process p3 = find(processes, "P3");
        // P3 is picked at t=4 because its burst(2) < P2.burst(7)
        assertEquals("P3 finish time",       6,  p3.getFinishTime());
        assertEquals("P3 turnaround time",   3,  p3.getTurnaroundTime());
        assertEquals("P3 waiting time",      1,  p3.getWaitingTime());
        assertEquals("P3 response time",     1,  p3.getResponseTime());

        Process p2 = find(processes, "P2");
        // P2 only runs after P3 finishes (t=6 to t=12)
        assertEquals("P2 finish time",       13, p2.getFinishTime());
        assertEquals("P2 turnaround time",   12, p2.getTurnaroundTime());
        assertEquals("P2 waiting time",      5,  p2.getWaitingTime());
        assertEquals("P2 response time",     5,  p2.getResponseTime());
    }

    // =======================================================================
    // TEST CASE 2 — Verifies context-switch overhead
    //
    // P1(arr=0, burst=10), P2(arr=2, burst=3)   CS = 1
    //
    // SJF timeline:
    //   t=0-1  : P1 runs (rem=8 after t=1)
    //   t=2    : P2 arrives with burst=3 < P1.remaining=8 → PREEMPT
    //            Context switch costs 1 tick (t=2 is the CS tick)
    //   t=3-5  : P2 runs (3 ticks, rem=0 at end of t=5) → finish=6
    //   t=6-13 : P1 resumes (8 remaining ticks)         → finish=14
    //
    //   P1: finish=14, TAT=14, WT=14-10=4,  RT=0
    //   P2: finish=6,  TAT=4,  WT=4-3=1,    RT=3-2=1
    // =======================================================================
    @Test
    public void testPreemptionWithContextSwitch_CS1() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 10, 1),
                new Process("P2", 2,  3, 2)
        );
        runSJF(processes, 1);

        Process p1 = find(processes, "P1");
        assertEquals("P1 finish time",     14, p1.getFinishTime());
        assertEquals("P1 turnaround time", 14, p1.getTurnaroundTime());
        assertEquals("P1 waiting time",     4, p1.getWaitingTime());
        assertEquals("P1 response time",    0, p1.getResponseTime());

        Process p2 = find(processes, "P2");
        assertEquals("P2 finish time",      6, p2.getFinishTime());
        assertEquals("P2 turnaround time",  4, p2.getTurnaroundTime());
        assertEquals("P2 waiting time",     1, p2.getWaitingTime());
        assertEquals("P2 response time",    1, p2.getResponseTime());
    }

    // =======================================================================
    // TEST CASE 3 — All processes arrive at t=0 (pure SJF ordering)
    //
    // P1(arr=0, burst=5), P2(arr=0, burst=3), P3(arr=0, burst=1)   CS = 0
    //
    // All in the ready queue at t=0 → SJF picks the shortest first:
    //   t=0    : P3 runs (burst=1, shortest) → finish=1
    //   t=1-3  : P2 runs (burst=3)           → finish=4
    //   t=4-8  : P1 runs (burst=5)           → finish=9
    //
    //   P3: finish=1, TAT=1, WT=0, RT=0
    //   P2: finish=4, TAT=4, WT=1, RT=1
    //   P1: finish=9, TAT=9, WT=4, RT=4
    // =======================================================================
    @Test
    public void testAllArriveAtZero_CS0() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 5, 1),
                new Process("P2", 0, 3, 2),
                new Process("P3", 0, 1, 3)
        );
        runSJF(processes, 0);

        Process p3 = find(processes, "P3");
        assertEquals("P3 finish time",     1,  p3.getFinishTime());
        assertEquals("P3 turnaround time", 1,  p3.getTurnaroundTime());
        assertEquals("P3 waiting time",    0,  p3.getWaitingTime());
        assertEquals("P3 response time",   0,  p3.getResponseTime());

        Process p2 = find(processes, "P2");
        assertEquals("P2 finish time",     4,  p2.getFinishTime());
        assertEquals("P2 turnaround time", 4,  p2.getTurnaroundTime());
        assertEquals("P2 waiting time",    1,  p2.getWaitingTime());
        assertEquals("P2 response time",   1,  p2.getResponseTime());

        Process p1 = find(processes, "P1");
        assertEquals("P1 finish time",     9,  p1.getFinishTime());
        assertEquals("P1 turnaround time", 9,  p1.getTurnaroundTime());
        assertEquals("P1 waiting time",    4,  p1.getWaitingTime());
        assertEquals("P1 response time",   4,  p1.getResponseTime());
    }

    // =======================================================================
    // TEST CASE 4 — Single process (edge case)
    //
    // P1(arr=0, burst=5)  CS = 0
    // Should run uninterrupted from t=0 to t=4, finish=5.
    //   TAT=5, WT=0, RT=0
    // =======================================================================
    @Test
    public void testSingleProcess() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 5, 1)
        );
        runSJF(processes, 0);

        Process p1 = find(processes, "P1");
        assertEquals("P1 finish time",     5, p1.getFinishTime());
        assertEquals("P1 turnaround time", 5, p1.getTurnaroundTime());
        assertEquals("P1 waiting time",    0, p1.getWaitingTime());
        assertEquals("P1 response time",   0, p1.getResponseTime());
    }

    // =======================================================================
    // TEST CASE 5 — CPU idles before first process arrives
    //
    // P1(arr=3, burst=2)  CS = 0
    // CPU is idle at t=0,1,2. P1 arrives at t=3, runs t=3-4, finish=5.
    //   TAT=5-3=2, WT=0, RT=0
    // =======================================================================
    @Test
    public void testCPUIdleBeforeArrival() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 3, 2, 1)
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output));
            runSJF(processes, 0);
        } finally {
            System.setOut(originalOut);
        }

        Process p1 = find(processes, "P1");
        assertEquals("P1 finish time",     5, p1.getFinishTime());
        assertEquals("P1 turnaround time", 2, p1.getTurnaroundTime());
        assertEquals("P1 waiting time",    0, p1.getWaitingTime());
        assertEquals("P1 response time",   0, p1.getResponseTime());

        String report = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(report.contains(String.format("%-10d %-20s %-15s", 0, "IDLE", "-")));
        assertTrue(report.contains(String.format("%-10d %-20s %-15s", 2, "IDLE", "-")));
    }

    @Test
    public void testTimelineReevaluatesShortestProcessAfterContextSwitch() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 8, 1),
                new Process("P2", 1, 4, 2),
                new Process("P3", 2, 1, 3)
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output));
            runSJF(processes, 2);
        } finally {
            System.setOut(originalOut);
        }

        String report = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(report.contains("--- Execution Timeline ---"));
        assertTrue(report.contains(String.format("%-10d %-20s %-15s", 1, "CONTEXT SWITCH", "-")));
        assertTrue(report.contains(String.format("%-10d %-20s %-15s", 2, "CONTEXT SWITCH", "-")));
        assertTrue(report.contains(String.format("%-10d %-20s %-15s", 3, "P3", "1")));
        assertTrue(report.contains(String.format("%-10d %-20s %-15s", 4, "P2", "4")));
        assertEquals("Context-switch overhead should be exactly two ticks", 2,
                report.split("CONTEXT SWITCH", -1).length - 1);

        Process p1 = find(processes, "P1");
        assertEquals("P1 finish time", 15, p1.getFinishTime());
        assertEquals("P1 waiting time", 7, p1.getWaitingTime());

        Process p2 = find(processes, "P2");
        assertEquals("P2 finish time", 8, p2.getFinishTime());
        assertEquals("P2 response time", 3, p2.getResponseTime());

        Process p3 = find(processes, "P3");
        assertEquals("P3 finish time", 4, p3.getFinishTime());
        assertEquals("P3 response time", 1, p3.getResponseTime());
    }
}
