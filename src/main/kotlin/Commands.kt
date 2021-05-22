package ml.zhou2008

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.GlobalEventChannel.subscribeOnce
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import java.sql.Timestamp
import java.text.SimpleDateFormat

class Commands {
    companion object {
        fun register() {
            CommandTest.register()
            CommandSay.register()
            CommandTTS.register()
            CommandGNMSMC.register()
            CommandAtall.register()
            CommandQuery.register()
        }

        fun unregister() {
            CommandTest.unregister()
            CommandSay.unregister()
            CommandTTS.unregister()
            CommandGNMSMC.unregister()
            CommandAtall.unregister()
            CommandQuery.unregister()
        }
    }
}

object CommandTest : SimpleCommand(
    MiraiQQBOT, "test",
    description = "测试"
) {
    @Handler
    suspend fun MemberCommandSender.handle(qq: Long, name: String, text: String, time: Int) {
        val builder = ForwardMessageBuilder(group)
        if (time==-1) {
            builder.add(qq, name, PlainText(text))
        } else {
            builder.add(qq, name, PlainText(text), time)
        }
        sendMessage(builder.build())
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
        val builder = MessageChainBuilder()
        for (member in group.members) {
            builder.add(At(member))
        }
        sendMessage(builder.build())
    }
}

object CommandQuery : SimpleCommand(
    MiraiQQBOT, "query",
    description = "查询某人在此群的信息"
) {
    @Handler
    suspend fun MemberCommandSender.handle(target: NormalMember) {
        val format = SimpleDateFormat("yyyy/MM/dd E HH:mm:ss z")
        val lastSpeakTime = format.format(Timestamp(target.lastSpeakTimestamp.toLong()*1000))
        val joinTime = format.format(Timestamp(target.joinTimestamp.toLong()*1000))
        sendMessage(buildMessageChain {
            +PlainText("QQ: ${target.id}(${target.nick})\n")
            +PlainText("群卡片名称: ${target.nameCard}\n")
            +PlainText("最后一次发言时间: $lastSpeakTime\n")
            +PlainText("入群时间: $joinTime")
        })
    }
}