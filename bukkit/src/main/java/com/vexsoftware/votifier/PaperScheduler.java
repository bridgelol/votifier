package com.vexsoftware.votifier;

import com.vexsoftware.votifier.platform.scheduler.ScheduledVotifierTask;
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

class PaperScheduler implements VotifierScheduler {
    private final NuVotifierBukkit plugin;

    public PaperScheduler(NuVotifierBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScheduledVotifierTask delayedOnPool(Runnable runnable, long delay, TimeUnit unit) {
        return new BukkitTaskWrapper(plugin.getServer().getAsyncScheduler().runDelayed(plugin, (task) -> runnable.run(), delay, unit));
    }

    @Override
    public ScheduledVotifierTask repeatOnPool(Runnable runnable, long delay, long repeat, TimeUnit unit) {
        return new BukkitTaskWrapper(plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, (task) -> runnable.run(), delay, repeat, unit));
    }

    private static class BukkitTaskWrapper implements ScheduledVotifierTask {
        private final ScheduledTask task;

        private BukkitTaskWrapper(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }
    }
}
