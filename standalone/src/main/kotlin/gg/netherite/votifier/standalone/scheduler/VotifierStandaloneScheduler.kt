package gg.netherite.votifier.standalone.scheduler

import com.vexsoftware.votifier.platform.scheduler.ScheduledVotifierTask
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object VotifierStandaloneScheduler : VotifierScheduler {

    override fun delayedOnPool(runnable: Runnable?, delay: Int, unit: TimeUnit?): ScheduledVotifierTask {
        val thread = thread(start = true) {
            Thread.sleep(unit!!.toMillis(delay.toLong()))
            runnable?.run()
        }

        val task = ScheduledVotifierTask { thread.interrupt() }
        return task
    }

    override fun repeatOnPool(runnable: Runnable?, delay: Int, repeat: Int, unit: TimeUnit?): ScheduledVotifierTask {
        val thread = thread(start = true) {
            repeat(repeat) {
                runnable?.run()
                Thread.sleep(unit!!.toMillis(delay.toLong()))
            }
        }

        val task = ScheduledVotifierTask { thread.interrupt() }
        return task
    }
}