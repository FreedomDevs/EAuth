package dev.elysium.eAuth

import java.util.logging.Level
import java.util.logging.Logger


class ELogger(private val pluginName: String, private val logger: Logger) {

    fun info(message: String) {
        log(Level.INFO, message)
    }

    fun warning(message: String) {
        log(Level.WARNING, message)
    }

    fun error(message: String) {
        log(Level.SEVERE, message)
    }

    fun debug(message: String) {
        log(Level.FINE, message)
    }

    private fun log(level: Level, message: String) {
        logger.log(level, "[$pluginName] $message")
    }
}