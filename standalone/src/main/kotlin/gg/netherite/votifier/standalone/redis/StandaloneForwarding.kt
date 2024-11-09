package gg.netherite.votifier.standalone.redis

import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.support.forwarding.ForwardingVoteSource
import gg.netherite.votifier.standalone.VotifierStandalone
import gg.netherite.votifier.standalone.VotifierStandaloneConfig
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock

abstract class StandaloneForwarding(
    protected val config: VotifierStandaloneConfig
) : ForwardingVoteSource {

    private var votes = 0
    private var lastCountReset = Clock.System.now()

    override fun forward(v: Vote) {
        if (config.maxVotesPerSecond > 0 && votes++ >= config.maxVotesPerSecond) {
            VotifierStandalone.LOGGER.info {
                "Hit rate limit of ${config.maxVotesPerSecond} votes per second, dropping vote: ${v.serialize()}"
            }

            return
        }

        forwardVote(v)

        if (Clock.System.now() > lastCountReset.plus(1.seconds)) {
            lastCountReset = Clock.System.now()
            votes = 0
        }
    }

    abstract fun forwardVote(vote: Vote)
}