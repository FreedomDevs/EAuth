package dev.elysium.eauth

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(JoinDialogListener(), this)
        logger.info("EAuth включен.")
    }

    override fun onDisable() {
        logger.info("EAuth выключен.")
    }
}
