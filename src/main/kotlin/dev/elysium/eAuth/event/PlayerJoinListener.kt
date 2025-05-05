package dev.elysium.eAuth.event

import dev.elysium.eAuth.EAuth
import dev.elysium.eapi.plugin.EAPIBukkit
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: EAuth): Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendTitle(
            "Ожидайте...",
            "Проверка токена...",
            20,
            60,
            20
        )

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            plugin.pluginScope.launch {
                val token = event.player.uniqueId

                val res = EAPIBukkit.instance.api.checkTokenValid.fetch(token.toString())
                if (res == null || res.isValid == false) {
                    Bukkit.getScheduler().runTask(plugin) { task ->
                        event.player.kick(Component.text("Вы не авторизированны"))
                    }
                }

            }
        }, 20)
    }
}