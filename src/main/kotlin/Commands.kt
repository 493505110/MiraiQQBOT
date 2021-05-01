package ml.zhou2008

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.data.Voice

class Commands {
    companion object {
        fun register() {
            CommandTest.register()
            CommandTTS.register()
        }

        fun unregister() {
            CommandTest.unregister()
            CommandTTS.unregister()
        }
    }
}

object CommandTest : SimpleCommand(
    MiraiQQBOT, "test",
    description = "测试指令"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("Test")
    }
}

object CommandTTS : SimpleCommand(
    MiraiQQBOT, "tts",
    description = "文本转语音(TextToSpeak)"
) {
    @Handler
    suspend fun CommandSender.handle(text: String) {
        TODO("LAZY")
    }
}