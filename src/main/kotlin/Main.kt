package dev.elysium.eauth

import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger

class Main : JavaPlugin() {

    companion object {
        lateinit var instance: Main
            private set
        lateinit var logger: Logger
            private set
    }

    override fun onLoad() {
        instance = this
        dataFolder.mkdirs()
    }

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(onPreLoginListener(), this)
        logger.info("EAuth включен.")
    }

    override fun onDisable() {
        logger.info("EAuth выключен.")
    }
}
