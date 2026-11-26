package com.scheduling;

import com.scheduling.model.Process;
import com.scheduling.scheduler.ACSScheduler;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ACSSchedulerTest {

    @Test
    public void demotesProcessAfterItsQuantumExpires() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 5, 4),
                new Process("P2", 0, 1, 1)
        );

        new ACSScheduler(processes, 3, 0, 4).run();

        Process p1 = find(processes, "P1");
        assertEquals(2, p1.getTier());
        assertEquals(6, p1.getFinishTime());
    }

    @Test
    public void tierTwoRunsToCompletionWithoutPreemption() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 4, 4),
                new Process("P2", 1, 1, 1)
        );

        new ACSScheduler(processes, 2, 0, 4).run();

        Process p1 = find(processes, "P1");
        Process p2 = find(processes, "P2");
        assertEquals(5, p1.getFinishTime());
        assertEquals(3, p2.getFinishTime());
    }

    @Test
    public void agingUsesSmallerEffectivePriorityValue() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 7, 4),
                new Process("P2", 0, 1, 1)
        );

        new ACSScheduler(processes, 3, 0, 4).run();

        Process p1 = find(processes, "P1");
        assertEquals(2, p1.getTier());
        assertEquals(8, p1.getFinishTime());
    }

    @Test
    public void pdfTestCaseOneMatchesReferenceMetrics() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 4, 2),
                new Process("P2", 1, 7, 3),
                new Process("P3", 3, 2, 1)
        );

        new ACSScheduler(processes, 3, 0, 4).run();

        assertMetrics(processes, "P1", 9, 9, 5, 0);
        assertMetrics(processes, "P2", 13, 12, 5, 2);
        assertMetrics(processes, "P3", 8, 5, 3, 3);
    }

    @Test
    public void pdfTestCaseTwoMatchesReferenceMetrics() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 5, 4),
                new Process("P2", 1, 6, 2),
                new Process("P3", 4, 4, 3)
        );

        new ACSScheduler(processes, 3, 1, 4).run();

        assertMetrics(processes, "P1", 18, 18, 13, 0);
        assertMetrics(processes, "P2", 15, 14, 8, 3);
        assertMetrics(processes, "P3", 20, 16, 12, 4);
    }

    private void assertMetrics(List<Process> processes, String id,
            int finish, int turnaround, int waiting, int response) {
        Process process = find(processes, id);
        assertEquals(id + " finish time", finish, process.getFinishTime());
        assertEquals(id + " turnaround time", turnaround, process.getTurnaroundTime());
        assertEquals(id + " waiting time", waiting, process.getWaitingTime());
        assertEquals(id + " response time", response, process.getResponseTime());
    }

    private Process find(List<Process> processes, String id) {
        for (Process process : processes) {
            if (process.getId().equals(id)) {
                return process;
            }
        }
        throw new AssertionError("Process not found: " + id);
    }
}
