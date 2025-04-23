package gg.netherite.votifier.standalone.redis

import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.support.forwarding.ForwardingVoteSource
import gg.netherite.votifier.standalone.VotifierStandalone
import gg.netherite.votifier.standalone.VotifierStandaloneConfig
import gg.netherite.votifier.standalone.VotifierStandaloneServer
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

abstract class StandaloneForwarding(
    private val standalone: VotifierStandaloneServer,
    private val config: VotifierStandaloneConfig
) : ForwardingVoteSource {

    private val cachedVotes: MutableList<CachedVote> = CopyOnWriteArrayList()
    private val cacheThread = Thread {
        while (standalone.running) {
            try {
                Thread.sleep(1.seconds.inWholeMilliseconds)

                if (!config.cacheOfflineVotes) {
                    continue
                }

                val now = Clock.System.now()
                cachedVotes.removeIf {
                    now > it.timestamp.plus(config.cacheOfflineVotesSeconds.seconds)
                }

                val iterator = cachedVotes.iterator()

                while (iterator.hasNext()) {
                    val cachedVote = iterator.next()

                    if (!isOnline(cachedVote.vote.username)) {
                        continue
                    }

                    VotifierStandalone.LOGGER.info { "Forwarding cached offline vote: ${cachedVote.vote.serialize()}" }
                    forwardVote(cachedVote.vote)
                    iterator.remove()
                }
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    private var votes = 0
    private var lastCountReset = Clock.System.now()

    init {
        cacheThread.start()
    }

    override fun forward(v: Vote) {
        if (config.maxVotesPerSecond > 0 && votes++ >= config.maxVotesPerSecond) {
            VotifierStandalone.LOGGER.info {
                "Hit rate limit of ${config.maxVotesPerSecond} votes per second, dropping vote: ${v.serialize()}"
            }

            return
        }

        if (Clock.System.now() > lastCountReset.plus(1.seconds)) {
            lastCountReset = Clock.System.now()
            votes = 0
        }

        if (config.cacheOfflineVotes && !config.forwardVotesEvenIfOffline && !isOnline(v.username)) {
            VotifierStandalone.LOGGER.info { "Caching offline vote: ${v.serialize()}" }
            return
        }

        forwardVote(v)
    }

    abstract fun forwardVote(vote: Vote)

    abstract fun isOnline(username: String): Boolean

    data class CachedVote(
        val vote: Vote,
        val timestamp: Instant
    )
}