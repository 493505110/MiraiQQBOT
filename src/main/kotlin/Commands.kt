package ml.zhou2008

import io.github.mzdluo123.silk4j.AudioUtils
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsVoice
import java.net.URL
import java.net.URLEncoder

class Commands {
    companion object {
        fun register() {
            CommandTest.register()
            CommandConfig.register()
            CommandSay.register()
            CommandTTS.register()
        }

        fun unregister() {
            CommandTest.unregister()
            CommandConfig.unregister()
            CommandSay.unregister()
            CommandTTS.unregister()
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
    @Description("列出配置项")
    suspend fun CommandSender.list() {
        var messages = "配置项列表:\n"
        for (config in Config.ConfigList) {
            messages += "   $config\n"
        }
        sendMessage(messages)
    }

    @SubCommand
    @Description("获取指定配置项")
    suspend fun CommandSender.get(key: String) {
        sendMessage(
            when (key) {
                "ITPK_APIKEY" -> "ITPK_APIKEY: ${Config.ITPK_APIKEY}"
                "ITPK_APISECRET" -> "ITPK_APISECRET: ${Config.ITPK_APISECRET}"
                "MAX_COUNT" -> "ITPK_APISECRET: ${Config.MAX_COUNT}"
                else -> "名为 \"$key\" 的配置项不存在"
            }
        )
    }

    @SubCommand
    @Description("设置配置项")
    suspend fun CommandSender.set(key: String, data: String) {
        when (key) {
            "ITPK_APIKEY" -> { Config.ITPK_APIKEY=data; sendMessage("OK") }
            "ITPK_APISECRET" -> { Config.ITPK_APISECRET=data; sendMessage("OK") }
            "MAX_COUNT" -> { Config.MAX_COUNT=data.toInt(); sendMessage("OK") }
            else -> { sendMessage("名为 \"$key\" 的配置项不存在") }
        }
    }
}

object CommandSay : SimpleCommand(
    MiraiQQBOT, "say",
    description = "发送消息"
) {
    @Handler
    suspend fun CommandSender.handle(text: String, count: Int) {
        val maxCount = Config.MAX_COUNT
        if (count < maxCount+1) {
            for (i in 1..count) {
                sendMessage(text.deserializeMiraiCode())
            }
        } else {
            sendMessage("次数达到上限($maxCount)")
        }
    }
}

object CommandTTS : SimpleCommand(
    MiraiQQBOT, "tts",
    description = "!!!-BUG-!!!文本转语音(只能在群执行)"
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    @Handler
    suspend fun MemberCommandSender.handle(text: String) {
        val ttsURL = "https://fanyi.baidu.com/gettts?lan=zh&spd=5&text=${URLEncoder.encode(text, Charsets.UTF_8)}"
        val stream = URL(ttsURL).openStream()
        val silk = AudioUtils.mp3ToSilk(stream)
        val er = silk.toExternalResource()
        subject.sendMessage(er.uploadAsVoice(subject))
        er.close()
    }
}