package gg.netherite.votifier.standalone.redis

import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.support.forwarding.redis.RedisConstants
import gg.netherite.votifier.standalone.VotifierStandalone
import gg.netherite.votifier.standalone.VotifierStandaloneConfig
import gg.netherite.votifier.standalone.VotifierStandaloneServer
import java.net.URI
import redis.clients.jedis.ConnectionPoolConfig
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.exceptions.JedisConnectionException

class RedisForwarding(
    server: VotifierStandaloneServer,
    config: VotifierStandaloneConfig
) : StandaloneForwarding(server, config) {

    private val jedis = JedisPooled(poolConfig(), URI.create(config.redisUri))

    override fun forwardVote(vote: Vote) {
        VotifierStandalone.LOGGER.info { "Forwarding vote to Redis: ${vote.serialize()}" }
        reconnecting("forwarding vote for ${vote.username}") {
            jedis.publish(VOTE_CHANNEL, vote.serialize().toString())
        }
    }

    override fun isOnline(username: String): Boolean =
        reconnecting("online lookup for $username") {
            jedis.sismember(RedisConstants.ONLINE_PLAYERS_KEY, username.lowercase())
        }

    override fun halt() {
        jedis.close()
    }

    /**
     * Runs [action], retrying once if the pooled connection turns out to be dead.
     *
     * When Redis restarts, sockets already in the pool are left half-open: the first command
     * written to one fails with a broken pipe. Borrow-time validation catches most of those, but
     * a connection can still die between validation and use, so the retry closes the gap. Votes
     * arrive too rarely to keep the pool warm on its own, which is exactly when this matters.
     */
    private fun <T> reconnecting(description: String, action: () -> T): T =
        try {
            action()
        } catch (e: JedisConnectionException) {
            VotifierStandalone.LOGGER.warn(e) { "Redis connection lost while $description, retrying once" }
            action()
        }

    private companion object {
        const val VOTE_CHANNEL = "votifier"

        /**
         * Validates connections as they leave the pool so a Redis restart cannot strand the
         * forwarder on a permanently broken socket. A vote is worth far more than the PING.
         */
        fun poolConfig() = ConnectionPoolConfig().apply {
            testOnBorrow = true
            testWhileIdle = true
        }
    }
}
