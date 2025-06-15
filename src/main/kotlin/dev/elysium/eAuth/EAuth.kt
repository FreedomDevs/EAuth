package dev.elysium.eAuth

import dev.elysium.eAuth.command.LoginCommand
import dev.elysium.eAuth.command.RegisterCommand
import dev.elysium.eAuth.listener.AuthListener
import dev.elysium.eAuth.utils.SessionManager
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

    fun checkEAPI()  {
        val eapiPlugin = server.pluginManager.getPlugin("EAPI")
        if (eapiPlugin == null) {
            eLogger.error("Плагин EAPI не найден! Отключение")
            server.pluginManager.disablePlugin(this)
        } else {
            eLogger.info("Плагин EAPI найден.")
        }
    }

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        eLogger = ELogger(name, logger)
        checkEAPI()

        eLogger.info("EAuth включен.")
        server.pluginManager.registerEvents(AuthListener, this)
        getCommand("login")?.setExecutor(LoginCommand())
        getCommand("l")?.setExecutor(LoginCommand())
        getCommand("register")?.setExecutor(RegisterCommand())
        getCommand("reg")?.setExecutor(RegisterCommand())
    }

    override fun onDisable() {
        pluginScope.cancel()
        SessionManager.clearAll()
        eLogger.info("EAuth выключен.")
    }
}
