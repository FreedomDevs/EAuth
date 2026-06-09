package dev.elysium.eauth

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import dev.elysium.eapi.EAPIPaper
import dev.elysium.eapi.lib.core.ApiException
import dev.elysium.eapi.lib.v2.auth.endpoints.AltLoginEndpoint
import dev.elysium.eapi.lib.v2.auth.endpoints.AltRegisterEndpoint
import dev.elysium.eapi.lib.v2.auth.endpoints.CheckRefreshTokenEndpoint
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


@Suppress("UnstableApiUsage")
class JoinDialogListener : Listener {
    private val awaitingResponse: MutableMap<UUID, CompletableFuture<Pair<String?, Boolean>>> =
        ConcurrentHashMap<UUID, CompletableFuture<Pair<String?, Boolean>>>()

    @EventHandler
    fun onPlayerJoin(event: AsyncPlayerConnectionConfigureEvent) {
        val connection: PlayerConfigurationConnection = event.connection
        val uuid = connection.profile.id!!

        var dialog: Dialog?
        val isRegister = uuid.version() == 8 && uuid.toString().startsWith("454c5953-4955")

        val cookie = connection.retrieveCookie(NamespacedKey("eauth", "plugin_refresh_token")).join()

        if (cookie != null && cookie.isNotEmpty() && !isRegister) {
            val refreshToken = Base64.getEncoder().encode(cookie).contentToString()

            try {
                runBlocking {
                    EAPIPaper.instance.api.v2.auth.checkRefreshToken(
                        CheckRefreshTokenEndpoint.Req(
                            refresh_token = refreshToken
                        )
                    )
                }
                return
            } catch (ex: ApiException) {
                if (ex.status == 401) {
                    connection.storeCookie(NamespacedKey("eauth", "plugin_refresh_token"), ByteArray(0))
                } else {
                    connection.disconnect(
                        Component.text(
                            "Неизвестная ошибка при проверке cookie файла",
                            NamedTextColor.RED
                        )
                    )
                }
            }
        }


        if (isRegister) {
            dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG)
                .get(Key.key("eauth:register")) ?: throw RuntimeException("Диалога register нет")
        } else {
            dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG)
                .get(Key.key("eauth:login")) ?: throw RuntimeException("Диалога login нет")
        }

        // Construct a new completable future without a task.
        val response = CompletableFuture<Pair<String?, Boolean>>()

        // Complete the future if nothing has been done after one minute.
        response.completeOnTimeout(Pair(null, true), 1, TimeUnit.MINUTES)

        // Put it into our map.
        awaitingResponse[uuid] = response

        val audience: Audience = connection.audience

        // Show the connecting player the dialog.
        audience.showDialog(dialog)

        // Wait until the future is complete. This step is necessary in order to keep the player in the configuration phase.
        val result = response.join()

        if (result.first == null) {
            audience.closeDialog()
            connection.disconnect(Component.text("Вы не ввели пароль", NamedTextColor.RED))
            return
        }

        if (!result.second) {
            audience.closeDialog()
            connection.disconnect(Component.text("Пароли не совпадают", NamedTextColor.RED))
            return
        }

        // We clean the map to avoid unnecessary entry buildup.
        awaitingResponse.remove(uuid)

        val password = result.first!!

        if (isRegister) {
            try {
                val registerResponse = runBlocking {
                    EAPIPaper.instance.api.v2.auth.altRegister(
                        AltRegisterEndpoint.Req(
                            login = connection.profile.name!!,
                            password = password
                        )
                    )
                }

                connection.storeCookie(
                    NamespacedKey("eauth", "plugin_refresh_token"),
                    Base64.getDecoder().decode(registerResponse.refresh_token)
                )

                val virtualHost = connection.virtualHost
                if (virtualHost == null) {
                    connection.disconnect(Component.text("Не удалось переподключится после регистрации, перезайдите сами"))
                    return
                }

                connection.transfer(virtualHost.hostString, virtualHost.port)
            } catch (ex: ApiException) {
                connection.disconnect(Component.text("Неизвестная ошибка", NamedTextColor.RED))
            }

            return
        }


        try {
            val loginResponse = runBlocking {
                EAPIPaper.instance.api.v2.auth.altLogin(
                    AltLoginEndpoint.Req(
                        login = connection.profile.name!!,
                        password = password
                    )
                )
            }

            connection.storeCookie(
                NamespacedKey("eauth", "plugin_refresh_token"),
                Base64.getDecoder().decode(loginResponse.refresh_token)
            )
        } catch (ex: ApiException) {
            if (ex.status == 401) {
                connection.disconnect(Component.text("Пароль неверный", NamedTextColor.RED))
                return
            }

            connection.disconnect(Component.text("Неизвестная ошибка", NamedTextColor.RED))
        }
    }

    @EventHandler
    fun onHandleDialog(event: PlayerCustomClickEvent) {
        val configurationConnection = event.commonConnection as? PlayerConfigurationConnection ?: return

        val uuid: UUID = configurationConnection.profile.id ?: return
        val key = event.identifier
        val view = event.dialogResponseView ?: return
        val password = view.getText("password") ?: return
        if (key == Key.key("eauth:user_input/confirm")) {
            if (uuid.version() == 8 && uuid.toString().startsWith("454c5953-4955")) {
                val passwordConfirmation = view.getText("password_confirmation") ?: return
                setConnectionJoinResult(uuid, Pair(password, password == passwordConfirmation))
            } else {
                setConnectionJoinResult(uuid, Pair(password, true))
            }
        }
    }

    /**
     * An event handler for cleanup the map to avoid unnecessary entry buildup.
     */
    @EventHandler
    fun onConnectionClose(event: PlayerConnectionCloseEvent) {
        awaitingResponse.remove(event.playerUniqueId)
    }

    /**
     * Simple utility method for setting a connection's dialog response result.
     */
    private fun setConnectionJoinResult(uniqueId: UUID, value: Pair<String?, Boolean>) {
        awaitingResponse[uniqueId]?.complete(value) ?: return
    }
}