package ml.zhou2008

import ml.zhou2008.Utils.Companion.botGetREP
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel.subscribeAlways
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning

object MiraiQQBOT : KotlinPlugin(
    JvmPluginDescription(
        id = "ml.zhou2008.MiraiQQBOT",
        name = "QQBOT",
        version = "0.1.0",
    ) {
        author("zhou2008")
    }
) {
    override fun onEnable() {
        Commands.register()
        Config.reload()

        if (Config.APPID.isEmpty() || Config.USERID.isEmpty()) {
            logger.warning { "APPID or USERID no set" }
        }

        subscribeAlways<GroupMessageEvent> {
            val msg = message.content
            if (msg.startsWith("-")) {
                subject.sendMessage(botGetREP(msg.removePrefix("-")))
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
