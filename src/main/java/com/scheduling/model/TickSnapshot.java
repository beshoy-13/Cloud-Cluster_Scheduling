package com.scheduling.model;

import java.util.List;

public record TickSnapshot(
        int time,
        String running,
        int remaining,
        List<String> tier1,
        List<String> tier2) {

    public static final String CONTEXT_SWITCH = "CONTEXT SWITCH";
    public static final String IDLE = "IDLE";

    public boolean isContextSwitch() {
        return CONTEXT_SWITCH.equals(running);
    }

    public boolean isIdle() {
        return IDLE.equals(running);
    }

    public boolean isBusy() {
        return !isContextSwitch() && !isIdle();
    }
}
