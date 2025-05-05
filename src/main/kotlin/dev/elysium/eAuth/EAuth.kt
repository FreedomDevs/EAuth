package dev.elysium.eAuth

import dev.elysium.eAuth.event.PlayerJoinListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bukkit.plugin.java.JavaPlugin

class EAuth : JavaPlugin() {
    internal val pluginScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        lateinit var instance: EAuth
            private set
    }

    internal lateinit var eLogger: ELogger

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        eLogger = ELogger(name, logger)
        eLogger.info("EAuth включен.")

        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
    }

    override fun onDisable() {
        pluginScope.cancel()
        eLogger.info("EAuth выключен.")
    }
}
