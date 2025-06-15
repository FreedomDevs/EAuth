package dev.elysium.eAuth.command

import dev.elysium.eAuth.EAuth
import dev.elysium.eAuth.listener.AuthListener
import dev.elysium.eAuth.utils.ChatUtil
import dev.elysium.eapi.lib.endpoints.RegisterUser
import dev.elysium.eapi.plugin.EAPIBukkit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RegisterCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        if (args.isEmpty()) {
            ChatUtil.message(sender, "&cИспользуй: /register <пароль>")
            return true
        }

        val password = args[0]
        EAuth.instance.pluginScope.launch(Dispatchers.IO) {
            val existingUser = EAPIBukkit.instance.api.getUser.fetch(sender.name)

            if (existingUser == null) {
                val res = EAPIBukkit.instance.api.registerUser.fetch(
                    RegisterUser.RequestBody(
                        name = sender.name,
                        password = password
                    )
                )
                ChatUtil.title(sender, "&eОжидайте данные обрабатываются")
                if (res != null) {
                    AuthListener.authenticate(sender)
                } else {
                    ChatUtil.message(sender, "&cНе удалось зарегистрироваться.")
                    EAuth.instance.eLogger.info(res.toString())
                }
            } else {
                ChatUtil.message(sender, "&cПользователь с таким ником уже зарегистрирован. Используй /login <пароль>")
                return@launch
            }
        }
        return true
    }
}