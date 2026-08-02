package dev.elysium.eauth

import io.jsonwebtoken.Jwts
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.io.File
import java.security.interfaces.EdECPublicKey
import java.util.UUID

class onPreLoginListener: Listener {

    @EventHandler
    fun preLogin(event: AsyncPlayerPreLoginEvent) {
        val plugin = Main.instance

        val cookie = event.connection.retrieveCookie(NamespacedKey("eauth", "eauth-jwt")).join()
        if (cookie == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("JWTクッキーが見つかりません"));
            return;
        }

        val jwt = cookie.toString(Charsets.UTF_8);

        val file = File(plugin.dataFolder, "key.pem")
        if (!file.exists()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Internal server error: 公開鍵が見つかりません"))
            return
        }

        val pubKey: EdECPublicKey = KeyLoader.loadPublicKey(file)

        try {
            val claims = Jwts.parser().verifyWith(pubKey).build().parseSignedClaims(jwt)

            val body = claims.payload
            val uuid = body["uuid"] as String
            val serverName = body["serverName"] as String
            val userName = body["userName"]  as String

            if (userName != event.name) {
                plugin.logger.severe("Игрок пытается зайти под другим ником забань эту мудень")
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("ユーザー名がJWT内のものと一致しません。"))
                return
            }

            if (serverName.lowercase() != plugin.config.getString("serverName")?.lowercase()){
                plugin.logger.severe("Игрок подключается не по тому serverName чекни конфиг мудила")
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("誤ったサーバーにリダイレクトされました。管理者にお問い合わせください。"))
                return
            }

            val nProfile = Bukkit.createProfile(UUID.fromString(uuid), event.name)
            event.playerProfile = nProfile
        } catch (e: Exception) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("無効なJWT"));
            return
        }
    }
}