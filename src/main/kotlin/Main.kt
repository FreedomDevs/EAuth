package dev.elysium.eauth

import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        logger.info("EAuth включен.")
    }

    override fun onDisable() {
        logger.info("EAuth выключен.")
    }
}
