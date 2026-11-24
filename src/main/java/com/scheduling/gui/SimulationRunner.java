package com.scheduling.gui;

import com.scheduling.metrics.Metrics;
import com.scheduling.model.Process;
import com.scheduling.model.TickSnapshot;
import com.scheduling.scheduler.ACSScheduler;
import com.scheduling.scheduler.SJFScheduler;
import com.scheduling.scheduler.Scheduler;
import java.util.ArrayList;
import java.util.List;

public final class SimulationRunner {

    public static final String SJF = "Preemptive SJF";
    public static final String ACS = "Adaptive Container Scheduler";

    public record ProcessSpec(String id, int arrival, int burst, int priority) {}

    public record Config(String algorithm, int quantum, int contextSwitch, int agingInterval) {}

    public record Result(
            Config config,
            List<Process> processes,
            List<TickSnapshot> timeline,
            Metrics metrics,
            double cpuUtilization) {}

    private SimulationRunner() {}

    public static Result run(List<ProcessSpec> specs, Config config) {
        List<Process> processes = new ArrayList<>();
        for (ProcessSpec spec : specs) {
            processes.add(new Process(spec.id(), spec.arrival(), spec.burst(), spec.priority()));
        }

        Scheduler scheduler = SJF.equals(config.algorithm())
                ? new SJFScheduler(processes, config.contextSwitch())
                : new ACSScheduler(
                        processes, config.quantum(), config.contextSwitch(), config.agingInterval());
        scheduler.simulate();

        Metrics metrics = new Metrics(processes);
        metrics.calculate();

        List<TickSnapshot> timeline = scheduler.getTimeline();
        long busy = timeline.stream().filter(TickSnapshot::isBusy).count();
        double utilization = timeline.isEmpty() ? 0 : 100.0 * busy / timeline.size();

        return new Result(config, processes, timeline, metrics, utilization);
    }
}
