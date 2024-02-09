package gg.netherite.votifier.standalone

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.source.decodeFromStream
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream
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

        // Temporarily using pterodactyl
        if (true) {
            val toml = Toml(
                inputConfig = TomlInputConfig(
                    ignoreUnknownNames = false,
                    allowEmptyValues = false,
                    allowNullValues = true,
                    allowEscapedQuotesInLiteralStrings = true,
                    allowEmptyToml = true
                ),
                outputConfig = TomlOutputConfig(
                    indentation = TomlIndentation.FOUR_SPACES,
                )
            )
            val file = File("config.toml")

            if (!file.exists()) {
                file.createNewFile()
                file.writeText(
                    toml.encodeToString(
                        TomlVotifierConfig.serializer(),
                        TomlVotifierConfig()
                    )
                )
            }

            toml.decodeFromStream<TomlVotifierConfig>(file.inputStream()).let {
                config = VotifierStandaloneConfig(
                    it.host,
                    it.port,
                    keyPair,
                    it.redisUri
                )
            }
        } else {
            config = VotifierStandaloneConfig(
                System.getenv("VOTIFIER_HOST") ?: "0.0.0.0",
                System.getenv("VOTIFIER_PORT")?.toIntOrNull() ?: 8192,
                keyPair,
                System.getenv("REDIS_URI") ?: "redis://localhost:6379/0"
            )
        }

        LOGGER.info { "Starting VotifierStandalone..." }
        val server = VotifierStandaloneServer()
        server.start(config)

        Runtime.getRuntime().addShutdownHook(Thread {
            LOGGER.info { "Shutting down VotifierStandalone..." }
            server.shutdown()
        })
    }
}

@Serializable
data class TomlVotifierConfig(
    val host: String = "0.0.0.0",
    val port: Int = 8192,
    val redisUri: String = "redis://localhost:6379/0"
)

data class VotifierStandaloneConfig(
    val host: String,
    val port: Int,
    val keyPair: KeyPair,
    val redisUri: String
)