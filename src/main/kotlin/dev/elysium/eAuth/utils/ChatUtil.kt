package dev.elysium.eAuth.utils

import org.bukkit.ChatColor
import org.bukkit.entity.Player

object ChatUtil {
    private fun format(text: String, vararg args: Pair<String, String>): String {
        return ChatColor.translateAlternateColorCodes('&', applyArgs(text, * args))
    }

    private fun applyArgs(text: String, vararg args: Pair<String, String>): String {
        var result = text
        for(arg in args) {
            result = result.replace(arg.first, arg.second)
        }
        return result
    }

    fun message(player: Player, msg: String, vararg args: Pair<String, String>) {
        player.sendMessage(format(msg, *args))
    }

    fun title(
        player: Player,
        title: String,
        subtitle: String = "",
        fadeIn: Int = 10,
        stay: Int = 70,
        fadeOut: Int = 20,
        vararg args: Pair<String, String>
    ) {
        val formattedTitle = format(title, *args)
        val formattedSubtitle = format(subtitle, *args)
        player.sendTitle(formattedTitle, formattedSubtitle, fadeIn, stay, fadeOut)
    }
}