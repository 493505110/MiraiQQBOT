package ml.zhou2008

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel.subscribeAlways
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import java.net.URL
import java.net.URLEncoder

object MiraiQQBOT : KotlinPlugin(
    JvmPluginDescription(
        id = "ml.zhou2008.MiraiQQBOT",
        name = "QQBOT",
        version = "0.1.0",
    ) {
        author("zhou2008")
    }
) {
    private fun botITPKgetREP(question: String): String {
        val url = "http://i.itpk.cn/api.php?question=${URLEncoder.encode(question, Charsets.UTF_8)}&api_key=${Config.ITPK_APIKEY}&api_secret=${Config.ITPK_APISECRET}"
        return URL(url).readText().drop(1)
    }

    override fun onEnable() {
        Commands.register()
        Config.reload()

        if (Config.ITPK_APIKEY.isEmpty() || Config.ITPK_APISECRET.isEmpty()) {
            logger.warning { "ITPK_APIKEY or ITPK_APISECRET no set" }
        }

        subscribeAlways<GroupMessageEvent> {
            val msg = message.content
            if (msg.startsWith("-")) {
                subject.sendMessage(botITPKgetREP(msg.removePrefix("-")))
            }
        }

        subscribeAlways<NewFriendRequestEvent> { accept() }
        subscribeAlways<BotInvitedJoinGroupRequestEvent> { accept() }

        logger.info { "Plugin loaded" }
    }

    override fun onDisable() {
        Commands.unregister()
        logger.info { "Plugin unloaded" }
    }
}
