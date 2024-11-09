package gg.netherite.votifier.standalone

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.source.decodeFromStream
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen
import java.io.File
import java.security.KeyPair
import kotlinx.serialization.Serializable
import mu.KotlinLogging

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

        val toml = Toml(
            inputConfig = TomlInputConfig(
                ignoreUnknownNames = false,
                allowEmptyValues = false,
                allowNullValues = true,
                allowEscapedQuotesInLiteralStrings = true,
                allowEmptyToml = true
            ), outputConfig = TomlOutputConfig(
                indentation = TomlIndentation.FOUR_SPACES,
            )
        )
        val file = File("config.toml")

        if (!file.exists()) {
            file.createNewFile()
            file.writeText(
                toml.encodeToString(
                    TomlVotifierConfig.serializer(), TomlVotifierConfig()
                )
            )
        }

        val tomlConfig = toml.decodeFromStream<TomlVotifierConfig>(file.inputStream())
        config = tomlConfig.toConfig(keyPair)

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
    val redisUri: String = "redis://localhost:6379/0",
    val bedrockPrefix: String = "", // NOTE: Only use this if the vote site doesn't support Bedrock usernames, i.e they do not allow users to enter a * in their IGN
    val maxVotesPerSecond: Int = 10
) {

    fun toConfig(keyPair: KeyPair): VotifierStandaloneConfig =
        VotifierStandaloneConfig(host, port, keyPair, redisUri, bedrockPrefix, maxVotesPerSecond)
}

data class VotifierStandaloneConfig(
    val host: String,
    val port: Int,
    val keyPair: KeyPair,
    val redisUri: String,
    val bedrockPrefix: String,
    val maxVotesPerSecond: Int
)