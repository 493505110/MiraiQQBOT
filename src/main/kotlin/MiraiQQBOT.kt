package ml.zhou2008

import ml.zhou2008.Utils.Companion.botGetREP
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel.subscribeAlways
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import java.util.*

object MiraiQQBOT : KotlinPlugin(
    JvmPluginDescription(
        id = "ml.zhou2008.MiraiQQBOT",
        name = "QQBOT",
        version = "0.3.0",
    ) {
        author("zhou2008")
    }
) {
    override fun onEnable() {
        Commands.register()
        Config.reload()
        Data.reload()

        val timer = Timer()
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH) + 1
        calendar.set(year, month, day, 0, 0, 0)
        timer.schedule(
            Task(),
            calendar.time,
            60*60*24*1000
        )

        if (Config.APPID.isEmpty() || Config.USERID.isEmpty()) {
            logger.warning { "APPID or USERID no set" }
        }

        subscribeAlways<GroupMessageEvent> {
            val msg = message.content
            if (msg.startsWith("-")) {
                subject.sendMessage(botGetREP(msg.removePrefix("-")))
            } else if (msg == "签到" || msg == "qd") {
                if (Data.qdED.contains(sender.id)) {
                    subject.sendMessage(At(sender) + "\n你已经签到过了")
                } else {
                    Data.qdCount += 1
                    val nowCoin = Data.coin[sender.id]
                    val gotCoin: Int
                    if (Data.qdCount == 1) {
                        val coin = (30..40).random()
                        if (nowCoin != null) {
                            Data.coin[sender.id] = nowCoin + coin
                        } else {
                            Data.coin[sender.id] = coin
                        }
                        gotCoin = coin
                    } else {
                        val coin = (10..20).random()
                        if (nowCoin != null) {
                            Data.coin[sender.id] = nowCoin + coin
                        } else {
                            Data.coin[sender.id] = coin
                        }
                        gotCoin = coin
                    }
                    if (Data.qdED.isEmpty()) {
                        Data.qdED=LongArray(1)
                        Data.qdED[0] = sender.id
                    } else {
                        val newARRAY = LongArray(Data.qdED.size + 1)
                        for (i in Data.qdED.indices) {
                            newARRAY[i] = Data.qdED[i]
                        }
                        newARRAY[Data.qdED.size] = sender.id
                        Data.qdED = newARRAY
                    }

                    subject.sendMessage(buildMessageChain {
                        +At(sender)
                        +PlainText("\n")
                        +PlainText("签到成功\n")
                        +PlainText("你是今天第${Data.qdCount}位签到的\n")
                        +PlainText("你获得了 $gotCoin Coin\n")
                        +PlainText("你现在有 ${Data.coin[sender.id]} Coin")
                    })
                }
            }
        }

        subscribeAlways<NewFriendRequestEvent> {
            if (Config.AUTO_ACCEPT) {
                accept()
            }
        }
        subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            if (Config.AUTO_ACCEPT) {
                accept()
            }
        }

        logger.info { "Plugin loaded" }
    }

    override fun onDisable() {
        Commands.unregister()
        logger.info { "Plugin unloaded" }
    }
}

class Task : TimerTask() {
    override fun run() {
        Data.qdCount = 0
        Data.qdED = LongArray(0)
    }
}