package gg.netherite.votifier.standalone.redis

import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.support.forwarding.redis.RedisConstants
import gg.netherite.votifier.standalone.VotifierStandalone
import gg.netherite.votifier.standalone.VotifierStandaloneConfig
import gg.netherite.votifier.standalone.VotifierStandaloneServer
import redis.clients.jedis.JedisPooled

class RedisForwarding(
    server: VotifierStandaloneServer,
    config: VotifierStandaloneConfig
) : StandaloneForwarding(server, config) {

    private val jedis = JedisPooled(config.redisUri)

    override fun forwardVote(vote: Vote) {
        VotifierStandalone.LOGGER.info { "Forwarding vote to Redis: ${vote.serialize()}" }
        jedis.publish("votifier", vote.serialize().toString())
    }

    override fun isOnline(username: String): Boolean =
        jedis.sismember(RedisConstants.ONLINE_PLAYERS_KEY, username.lowercase())

    override fun halt() {
        jedis.close()
    }
}