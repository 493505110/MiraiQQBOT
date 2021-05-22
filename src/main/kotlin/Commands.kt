package ml.zhou2008

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.event.GlobalEventChannel.subscribeOnce
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder

class Commands {
    companion object {
        fun register() {
            CommandTest.register()
            CommandSay.register()
            CommandTTS.register()
            CommandGNMSMC.register()
            CommandAtall.register()
            //CommandQuery.register()
        }

        fun unregister() {
            CommandTest.unregister()
            CommandSay.unregister()
            CommandTTS.unregister()
            CommandGNMSMC.unregister()
            CommandAtall.unregister()
            //CommandQuery.unregister()
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
    suspend fun CommandSender.handle(text: String) {
        sendMessage(text.deserializeMiraiCode())
    }
}

object CommandTTS : SimpleCommand(
    MiraiQQBOT, "tts",
    description = "文本转语音"
) {
    @Handler
    suspend fun MemberCommandSender.handle(text: String) {
        Utils.tts(text, group)
    }
}

object CommandGNMSMC : SimpleCommand(
    MiraiQQBOT, "gnmsmc",
    description = "获取下一条消息的Mirai码"
) {
    @Handler
    suspend fun UserCommandSender.handle() {
        subscribeOnce<MessageEvent> {
            sendMessage(message.serializeToMiraiCode())
        }
    }
}

object CommandAtall : SimpleCommand(
    MiraiQQBOT, "atall",
    description = "At所有人"
) {
    @Handler
    suspend fun MemberCommandSender.handle() {
        val messageChainBuilder = MessageChainBuilder()
        for (member in group.members) {
            messageChainBuilder.add(At(member))
        }
        sendMessage(messageChainBuilder.build())
    }
}

//object CommandQuery : SimpleCommand(
//    MiraiQQBOT, "query",
//    description = "查询某人在此群的信息"
//) {
//    @Handler
//    suspend fun MemberCommandSender.handle(user: NormalMember) {
//        val format = SimpleDateFormat("yyyy/MM/dd E HH:mm:ss z")
//        val lastSpeakTime = format.format(user.lastSpeakTimestamp)
//        val joinTime = format.format(user.joinTimestamp)
//        sendMessage(buildMessageChain {
//            +PlainText("QQ: ${user.id}(${user.nick})\n")
//            +PlainText("群卡片名称: ${user.nameCard}\n")
//            +PlainText("最后一次发言时间: $lastSpeakTime\n")
//            +PlainText("入群时间: $joinTime")
//        })
//    }
//}