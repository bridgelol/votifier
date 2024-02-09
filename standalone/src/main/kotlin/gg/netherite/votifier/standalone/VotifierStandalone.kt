package gg.netherite.votifier.standalone

import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen
import mu.KotlinLogging
import java.io.File
import java.security.KeyPair

object VotifierStandalone {

    val LOGGER = KotlinLogging.logger("VotifierStandalone")
    lateinit var config: VotifierStandaloneConfig

    @JvmStatic
    fun main(args: Array<String>) {
        val keyFolder = File("rsa")

        if (!keyFolder.exists()) {
            keyFolder.mkdirs()
            LOGGER.info { "Generating RSA keypair..." }
            val keyPair = RSAKeygen.generate(2048)
            RSAIO.save(keyFolder, keyPair)
        }

        LOGGER.info { "Loading RSA keypair..." }
        val keyPair = RSAIO.load(keyFolder)

        config = VotifierStandaloneConfig(
            System.getenv("VOTIFIER_HOST") ?: "0.0.0.0",
            System.getenv("VOTIFIER_PORT")?.toIntOrNull() ?: 8192,
            keyPair,
            System.getenv("REDIS_URI") ?: "redis://localhost:6379/0"
        )

        LOGGER.info { "Starting VotifierStandalone..." }
        val server = VotifierStandaloneServer()
        server.start(config)

        Runtime.getRuntime().addShutdownHook(Thread {
            LOGGER.info { "Shutting down VotifierStandalone..." }
            server.shutdown()
        })
    }
}

data class VotifierStandaloneConfig(
    val host: String,
    val port: Int,
    val keyPair: KeyPair,
    val redisUri: String
)