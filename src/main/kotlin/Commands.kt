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
import kotlin.math.roundToInt

class Commands {
    companion object {
        fun register() {
            CommandTest.register()
            CommandSay.register()
            CommandTTS.register()
            CommandGNMSMC.register()
            CommandAtall.register()
            CommandQuery.register()
            CommandCointop.register()
            CommandQD.register()
            CommandGetCoin.register()
        }

        fun unregister() {
            CommandTest.unregister()
            CommandSay.unregister()
            CommandTTS.unregister()
            CommandGNMSMC.unregister()
            CommandAtall.unregister()
            CommandQuery.unregister()
            CommandCointop.unregister()
            CommandQD.unregister()
            CommandGetCoin.unregister()
        }
    }
}

object CommandTest : SimpleCommand(
    MiraiQQBOT, "test", "测试",
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
    MiraiQQBOT, "say", "发送",
    description = "发送消息(发送)"
) {
    @Handler
    suspend fun CommandSender.handle(text: String) {
        sendMessage(text.deserializeMiraiCode())
    }
}

object CommandTTS : SimpleCommand(
    MiraiQQBOT, "tts", "文本转语音",
    description = "文本转语音"
) {
    @Handler
    suspend fun MemberCommandSender.handle(text: String) {
        Utils.tts(text, group)
    }
}

object CommandGNMSMC : SimpleCommand(
    MiraiQQBOT, "gnmsmc", "下一条消息码",
    description = "获取下一条消息的Mirai码(下一条消息码)"
) {
    @Handler
    suspend fun UserCommandSender.handle() {
        subscribeOnce<MessageEvent> {
            sendMessage(message.serializeToMiraiCode())
        }
    }
}

object CommandAtall : SimpleCommand(
    MiraiQQBOT, "atall", "At所有人",
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
    MiraiQQBOT, "query", "查询",
    description = "查询某人在此群的信息(查询)"
) {
    @Handler
    suspend fun MemberCommandSender.handle(target: NormalMember) {
        val format = SimpleDateFormat("yyyy/MM/dd E HH:mm:ss z")
        val lastSpeakTime = format.format(Timestamp(target.lastSpeakTimestamp.toLong()*1000))
        val joinTime = format.format(Timestamp(target.joinTimestamp.toLong()*1000))
        var coin = Data.coin[target.id]
        if (coin == null) {
            coin = 0
        }
        sendMessage(buildMessageChain {
            +PlainText("QQ: ${target.id}(${target.nick})\n")
            +PlainText("群卡片名称: ${target.nameCard}\n")
            +PlainText("Coin: $coin\n")
            +PlainText("最后一次发言时间: $lastSpeakTime\n")
            +PlainText("入群时间: $joinTime")
        })
    }
}

object CommandCointop : SimpleCommand(
    MiraiQQBOT, "cointop", "金币排行",
    description = "Coin 排行榜(金币排行)"
) {
    @Handler
    suspend fun CommandSender.handler() {
        val sortedmap = Data.coin.entries.sortedByDescending { it.value }
            .associateBy ({ it.key }, { it.value } )

        val builder = MessageChainBuilder()
        val it = sortedmap.iterator()
        builder.add(PlainText("Coin 排行榜\n"))
        var allCoin = 0
        var index = 1
        while (it.hasNext()) {
            val key = it.next().key
            val value = Data.coin[key]
            builder.add(PlainText("$index.") + At(key) + PlainText("($key): $value\n"))
            index++
            if (value != null) {
                allCoin += value
            }
        }
        builder.add(PlainText("Coin 总和: $allCoin"))
        sendMessage(builder.build())
    }
}

object CommandQD : SimpleCommand(
    MiraiQQBOT, "qd", "签到",
    description = "签到"
) {
    @Handler
    suspend fun MemberCommandSender.handle() {
        if (Data.qdED.contains(user.id)) {
            subject.sendMessage(At(user) + "\n你已经签到过了")
        } else {
            Data.qdCount += 1
            val nowCoin = Data.coin[user.id]
            val gotCoin: Int
            if (Data.qdCount == 1) {
                val coin = (30..40).random()
                if (nowCoin != null) {
                    Data.coin[user.id] = nowCoin + coin
                } else {
                    Data.coin[user.id] = coin
                }
                gotCoin = coin
            } else {
                val coin = (10..20).random()
                if (nowCoin != null) {
                    Data.coin[user.id] = nowCoin + coin
                } else {
                    Data.coin[user.id] = coin
                }
                gotCoin = coin
            }

            if (Data.qdED.isEmpty()) {
                Data.qdED = LongArray(1)
                Data.qdED[0] = user.id
            } else {
                val newARRAY = LongArray(Data.qdED.size + 1)
                for (i in Data.qdED.indices) {
                    newARRAY[i] = Data.qdED[i]
                }
                newARRAY[Data.qdED.size] = user.id
                Data.qdED = newARRAY
            }

            val userAllQD = Data.allQD[user.id]
            if (userAllQD != null) {
                Data.allQD[user.id] = userAllQD + 1
            } else {
                Data.allQD[user.id] = 1
            }

            subject.sendMessage(buildMessageChain {
                +At(user)
                +PlainText("\n")
                +PlainText("签到成功\n")
                +PlainText("你是今天第${Data.qdCount}位签到的\n")
                +PlainText("你已累计签到${Data.allQD[user.id]}天\n")
                +PlainText("你获得了 $gotCoin Coin\n")
                +PlainText("你现在有 ${Data.coin[user.id]} Coin")
            })
        }
    }
}

object CommandGetCoin : SimpleCommand(
    MiraiQQBOT, "getcoin", "抢劫",
    description = "抢劫 40%成功 成功则获得对方的40% 失败扣除自己的20%"
) {
    @Handler
    suspend fun MemberCommandSender.handle(target: NormalMember) {
        if (target != user) {
            val tof = (1..10).random() <= 4
            val targetCoin = Data.coin[target.id]
            val selfCoin = Data.coin[target.id]
            val lostCoin: Int
            if (selfCoin != null) {
                lostCoin = (selfCoin * 0.2).roundToInt()
            } else {
                sendMessage("你至少需要1 Coin来进行这个操作")
                return
            }
            if (targetCoin != null) {
                val getCoin = (targetCoin * 0.4).roundToInt()
                if (tof) {
                    Data.coin[user.id] = selfCoin + getCoin
                    Data.coin[target.id] = targetCoin - getCoin
                    sendMessage(At(user.id) + "成功,你获得了$getCoin(${Data.coin[user.id]}) Coin,目标还剩${Data.coin[target.id]} Coin")
                } else {
                    Data.coin[user.id] = selfCoin - lostCoin
                    sendMessage(At(user.id) + "失败,你丢失了$lostCoin Coin,你还剩${Data.coin[user.id]} Coin")
                }
            } else {
                sendMessage("你的目标没有任何的Coin")
            }
        } else {
            sendMessage("您搁着卡bug呢?")
        }
    }
}