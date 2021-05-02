package ml.zhou2008

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode

class Commands {
    companion object {
        fun register() {
            CommandTest.register()
            CommandSay.register()
            CommandTTS.register()
        }

        fun unregister() {
            CommandTest.unregister()
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
    description = "文本转语音(只能在群执行)"
) {
    @Handler
    suspend fun MemberCommandSender.handle(text: String) {
        Utils.tts(text, group)
    }
}