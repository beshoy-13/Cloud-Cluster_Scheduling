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

    private Process find(List<Process> processes, String id) {
        for (Process process : processes) {
            if (process.getId().equals(id)) {
                return process;
            }
        }
        throw new AssertionError("Process not found: " + id);
    }
}
