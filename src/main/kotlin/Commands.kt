package ml.zhou2008

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand

class Commands {
    companion object {
        fun register() {
            CommandTest.register()
            CommandConfig.register()
        }

        fun unregister() {
            CommandTest.unregister()
            CommandConfig.unregister()
        }
    }
}

object CommandTest : SimpleCommand(
    MiraiQQBOT, "test",
    description = "测试"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("Hello world!")
    }
}

object CommandConfig : CompositeCommand(
    MiraiQQBOT, "config",
    description = "配置"
) {
    @SubCommand
    suspend fun CommandSender.list() {
        var messages = "配置项列表:\n"
        for (config in Config.ConfigList) {
            messages += "   $config\n"
        }
        sendMessage(messages)
    }

    @SubCommand
    suspend fun CommandSender.get(key: String) {
        sendMessage(
            when (key) {
                "ITPK_APIKEY" -> "ITPK_APIKEY: ${Config.ITPK_APIKEY}"
                "ITPK_APISECRET" -> "ITPK_APISECRET: ${Config.ITPK_APISECRET}"
                else -> "名为 \"$key\" 的配置项不存在"
            }
        )
    }

    @SubCommand
    suspend fun CommandSender.set(key: String, data: String) {
        when (key) {
            "ITPK_APIKEY" -> { Config.ITPK_APIKEY=data; sendMessage("OK") }
            "ITPK_APISECRET" -> { Config.ITPK_APISECRET=data; sendMessage("OK") }
            else -> { sendMessage("名为 \"$key\" 的配置项不存在") }
        }
    }
}