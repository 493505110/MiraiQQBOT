package ml.zhou2008

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel.subscribeAlways
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
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
    private fun botGetREP(spoken: String): String {
        val url = "https://api.ownthink.com/bot?appid=${Config.APPID}&userid=${Config.USERID}&spoken=${URLEncoder.encode(spoken, Charsets.UTF_8)}"
        val jsonStr = URL(url).readText()
        val jsonObj = JSONObject.parseObject(jsonStr)
        return jsonObj.getJSONObject("data").getJSONObject("info").getString("text")
    }

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
