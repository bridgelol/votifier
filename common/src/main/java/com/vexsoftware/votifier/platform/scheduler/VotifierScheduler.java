package com.vexsoftware.votifier.platform.scheduler;

import java.util.concurrent.TimeUnit;

public interface VotifierScheduler {

    ScheduledVotifierTask delayedOnPool(Runnable runnable, long delay, TimeUnit unit);

    ScheduledVotifierTask repeatOnPool(Runnable runnable, long delay, long repeat, TimeUnit unit);
}
