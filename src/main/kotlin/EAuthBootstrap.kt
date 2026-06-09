package dev.elysium.eauth

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.DialogKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

@Suppress("UnstableApiUsage")
class EAuthBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(
            RegistryEvents.DIALOG.compose().newHandler { event ->
                run {
                    event.registry().register(DialogKeys.create(Key.key("eauth:login"))) { builder ->
                        builder
                            .base(
                                DialogBase.builder(Component.text("Вход в аккаунт"))
                                    .body(listOf(DialogBody.plainMessage(Component.text("Введите пароль от своего аккаунта"))))
                                    .inputs(
                                        listOf(
                                            DialogInput.text("password", Component.text("Пароль"))
                                                .width(300)
                                                .build(),
                                        )
                                    )
                                    .build()
                            )
                            .type(
                                DialogType.confirmation(
                                    ActionButton.create(
                                        Component.text("Войти", TextColor.color(0xAEFFC1)),
                                        Component.text("Вход с заданным паролем."),
                                        100,
                                        DialogAction.customClick(Key.key("eauth:user_input/confirm"), null)
                                    ),
                                    ActionButton.create(
                                        Component.text("Отмена", TextColor.color(0xFFA0B1)),
                                        Component.text("Ливает с сервера."),
                                        100,
                                        null // If we set the action to null, it doesn't do anything and closes the dialog
                                    )
                                )
                            )
                    }
                    event.registry().register(DialogKeys.create(Key.key("eauth:register"))) { builder ->
                        builder
                            .base(
                                DialogBase.builder(Component.text("Регистрация нового аккаунта"))
                                    .body(listOf(DialogBody.plainMessage(Component.text("Введите пароль для регистрации своего аккаунта"))))
                                    .inputs(
                                        listOf(
                                            DialogInput.text("password", Component.text("Пароль"))
                                                .width(300)
                                                .build(),
                                            DialogInput.text("password_confirmation", Component.text("Повтор пароля"))
                                                .width(300)
                                                .build(),
                                        )
                                    )
                                    .build()
                            )
                            .type(
                                DialogType.confirmation(
                                    ActionButton.create(
                                        Component.text("Зарегистрироватся", TextColor.color(0xAEFFC1)),
                                        Component.text("Регистрация аккаунта с заданным паролем."),
                                        100,
                                        DialogAction.customClick(Key.key("eauth:user_input/confirm"), null)
                                    ),
                                    ActionButton.create(
                                        Component.text("Отмена", TextColor.color(0xFFA0B1)),
                                        Component.text("Ливает с сервера."),
                                        100,
                                        null
                                    )
                                )
                            )
                    }
                }
            }
        )
    }
}