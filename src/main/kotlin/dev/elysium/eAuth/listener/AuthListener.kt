package dev.elysium.eAuth.listener

import dev.elysium.eAuth.EAuth
import dev.elysium.eAuth.utils.ChatUtil
import dev.elysium.eAuth.utils.SessionManager
import dev.elysium.eapi.plugin.EAPIBukkit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable

object AuthListener: Listener {
    private val notAuthenticated = mutableSetOf<String>()

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        val ip = player.address?.address?.hostAddress ?: return
        val nick = player.name

        if (SessionManager.isValid(ip, nick)) {
            ChatUtil.title(player, "&aУспешная авторизация!")
            return
        }

        notAuthenticated += nick
        player.gameMode = GameMode.ADVENTURE
        player.isFlying = false
        startAuthTimer(player)

        EAuth.instance.pluginScope.launch(Dispatchers.IO) {
            val res = EAPIBukkit.instance.api.getUser.fetch(nick)
            if (res  == null) {
                ChatUtil.message(player, "&eВы не зарегистрированы. Используйте /reg <пароль>")
            } else {
                ChatUtil.message(player, "&eВойдите с помощью /login <пароль>")
            }
        }
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player
        if (notAuthenticated.contains(player.name)) {
            if (e.from.x != e.to.x || e.from.z != e.to.z) {
                e.to = e.from
            }
        }
    }

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        val player = e.player
        if (!notAuthenticated.contains(player.name)) return

        val cmd = e.message.lowercase()
        if (!cmd.startsWith("/login") && !cmd.startsWith("/l") && !cmd.startsWith("/reg") && !cmd.startsWith("/register")) {
            ChatUtil.message(player, "&cВы должны сначала авторизоваться.")
            e.isCancelled = true
        }
    }

    private fun startAuthTimer(player: Player) {
        object : BukkitRunnable() {
            var time = 180
            override fun run() {
                if (!notAuthenticated.contains(player.name)) {
                    cancel()
                    return
                }
                if (time <= 0) {
                    player.kickPlayer("Время на авторизацию истекло.")
                    cancel()
                    return
                }
                player.sendActionBar("§cАвторизуйтесь! Осталось $time секунд")
                time--
            }
        }.runTaskTimer(EAuth.instance, 0L, 20L)
    }

    fun authenticate(player: Player) {
        val ip = player.address?.address?.hostAddress ?: return
        val nick = player.name

        SessionManager.addSession(ip, nick)
        notAuthenticated.remove(nick)
        ChatUtil.title(player, "&aУспешная авторизация!")
        player.gameMode = GameMode.SURVIVAL
    }
}