package gg.netherite.votifier.standalone

import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.net.VotifierServerBootstrap
import com.vexsoftware.votifier.net.VotifierSession
import com.vexsoftware.votifier.platform.LoggingAdapter
import com.vexsoftware.votifier.platform.VotifierPlugin
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler
import gg.netherite.votifier.standalone.logging.StandaloneLoggingAdapter
import gg.netherite.votifier.standalone.redis.RedisForwarding
import gg.netherite.votifier.standalone.scheduler.VotifierStandaloneScheduler
import java.security.Key
import java.security.KeyPair
import kotlin.concurrent.thread

class VotifierStandaloneServer : VotifierPlugin {

    private lateinit var config: VotifierStandaloneConfig
    private lateinit var redisForwarding: RedisForwarding
    private lateinit var bootstrap: VotifierServerBootstrap

    @Volatile
    var running = false

    fun start(config: VotifierStandaloneConfig) {
        if (running) return

        this.running = true
        this.config = config

        thread(start = true) {
            try {
                this.redisForwarding = RedisForwarding(this, config)
                this.bootstrap = VotifierServerBootstrap(config.host, config.port, this, false)

                this.bootstrap.start { error ->
                    error?.let {
                        VotifierStandalone.LOGGER.error("Error in VotifierServerBootstrap", it)
                    } ?: VotifierStandalone.LOGGER.info("Successfully bound to ${config.host}:${config.port}")
                }

                while (running) {
                    Thread.sleep(1000)
                }
            } catch (e: Exception) {
                VotifierStandalone.LOGGER.error("Error in VotifierStandaloneServer", e)
            }
        }
    }

    fun shutdown() {
        running = false
        redisForwarding.halt()
    }

    override fun onVoteReceived(
        vote: Vote,
        protocolVersion: VotifierSession.ProtocolVersion,
        remoteAddress: String?
    ) {
        if (config.debug) {
            VotifierStandalone.LOGGER.info(
                "Received vote from $remoteAddress: $vote (protocol version: ${protocolVersion.humanReadable})"
            )
        }

        redisForwarding.forward(vote)

        if (config.bedrockPrefix.isNotEmpty()) {
            redisForwarding.forward(vote.cloneAsBedrockPrefix(config.bedrockPrefix))
        }
    }

    override fun getTokens(): Map<String, Key> = config.v2Keys
    override fun getProtocolV1Key(): KeyPair = config.keyPair
    override fun getPluginLogger(): LoggingAdapter = StandaloneLoggingAdapter
    override fun getScheduler(): VotifierScheduler = VotifierStandaloneScheduler
}