package gg.netherite.votifier.standalone.redis

import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.support.forwarding.ForwardingVoteSource
import gg.netherite.votifier.standalone.VotifierStandalone
import gg.netherite.votifier.standalone.VotifierStandaloneConfig
import redis.clients.jedis.Jedis

class RedisForwarding(config: VotifierStandaloneConfig) : StandaloneForwarding(config) {

    private val jedis = Jedis(config.redisUri)

    override fun forwardVote(vote: Vote) {
        VotifierStandalone.LOGGER.info { "Forwarding vote to Redis: ${vote.serialize()}" }
        jedis.publish("votifier", vote.serialize().toString())
    }

    override fun halt() {
        jedis.close()
    }
}