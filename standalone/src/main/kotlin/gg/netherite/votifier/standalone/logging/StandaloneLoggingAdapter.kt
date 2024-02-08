package gg.netherite.votifier.standalone.logging

import com.vexsoftware.votifier.platform.LoggingAdapter
import gg.netherite.votifier.standalone.VotifierStandalone

object StandaloneLoggingAdapter : LoggingAdapter {

    override fun info(message: String) {
        VotifierStandalone.LOGGER.info(message)
    }

    override fun warn(message: String) {
        VotifierStandalone.LOGGER.warn(message)
    }

    override fun error(message: String) {
        VotifierStandalone.LOGGER.error(message)
    }

    override fun error(s: String, vararg o: Any) {
        VotifierStandalone.LOGGER.error(s, o)
    }

    override fun error(s: String, e: Throwable, vararg o: Any) {
        VotifierStandalone.LOGGER.error(s, e, o)
    }

    override fun warn(s: String, vararg o: Any) {
        VotifierStandalone.LOGGER.warn(s, o)
    }

    override fun info(s: String, vararg o: Any) {
        VotifierStandalone.LOGGER.info(s, o)
    }
}