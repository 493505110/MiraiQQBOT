package ml.zhou2008

import com.alibaba.fastjson.JSONObject
import ml.zhou2008.Utils.Companion.botGetREP
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.GlobalEventChannel.subscribeAlways
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import java.net.URL
import java.net.URLEncoder
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object MiraiQQBOT : KotlinPlugin(
    JvmPluginDescription(
        id = "ml.zhou2008.MiraiQQBOT",
        name = "QQBOT",
        version = "0.5.0",
    ) {
        author("zhou2008")
    }
) {
    override fun onEnable() {
        Config.reload()
        Data.reload()

        val timer = Timer()
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH) + 1
        calendar.set(year, month, day, 0, 0, 0)
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    Data.qdCount = 0
                    Data.qdED = LongArray(0)
                }
            },
            calendar.time,
            86400000L
        )

        if (Config.APPID.isEmpty() || Config.USERID.isEmpty()) {
            logger.warning { "APPID or USERID no set" }
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        subscribeAlways<GroupMessageEvent> {
            if (!Config.BLACKLISTS.contains(sender.id)) {
                val msg = message.content.split(" ")
                when {
                    msg[0].startsWith("-") -> {
                        subject.sendMessage(botGetREP(msg[0].removePrefix("-")))
                    }
                    msg[0] == "help" || msg[0] == "帮助" -> {
                        subject.sendMessage(buildMessageChain {
                            +PlainText("指令列表:\n")
                            +PlainText("  qd 签到\n")
                            +PlainText("  cointop Coin排行榜\n")
                            +PlainText("  query <target> 查询某人在这个群的信息\n")
                            +PlainText("  getavatar <qq> 获取目标头像\n")
                            +PlainText("  music <name> 点歌\n")
                            +PlainText("  getcoin <target> 抢劫\n")
                            +PlainText("  baike <name> 百科")
                        })
                    }
                    msg[0] == "music" || msg[0] == "点歌" -> {
                        if (msg.size == 2) {
                            Utils.music(msg[1], subject)
                        } else {
                            subject.sendMessage("参数错误,请使用\"help\"来获取帮助")
                        }
                    }
                    msg[0] == "getavatar" || msg[0] == "获取头像" -> {
                        if (msg.size == 2) {
                            val er =
                                URL("http://q1.qlogo.cn/g?b=qq&nk=${msg[1]}&s=640").openStream().toExternalResource()
                            subject.sendMessage(subject.uploadImage(er))
                            er.close()
                        } else {
                            subject.sendMessage("参数错误,请使用\"help\"来获取帮助")
                        }
                    }
                    msg[0] == "baike" || msg[0] == "百科" -> {
                        if (msg.size == 2) {
                            val url = URL("https://api.muxiaoguo.cn/api/Baike?type=Baidu&word=${URLEncoder.encode(msg[1], Charsets.UTF_8)}")
                            val jsonObj = JSONObject.parseObject(url.readText())
                            val code = jsonObj.getIntValue("code")
                            if (code == 200) {
                                subject.sendMessage(jsonObj.getJSONObject("data").getString("content"))
                            } else {
                                subject.sendMessage("条目不存在")
                            }
                        } else {
                            subject.sendMessage("参数错误,请使用\"help\"来获取帮助")
                        }
                    }
                    msg[0].startsWith("query", true) || msg[0].startsWith("查询") -> {
                        val at: At? = message.findIsInstance<At>()
                        if (at != null) {
                            var target: NormalMember? = null
                            for (member in group.members) {
                                if (member.id == at.target) {
                                    target = member
                                    break
                                }
                            }
                            if (target != null) {
                                val format = SimpleDateFormat("yyyy/MM/dd E HH:mm:ss z")
                                val lastSpeakTime = format.format(Timestamp(target.lastSpeakTimestamp.toLong() * 1000))
                                val joinTime = format.format(Timestamp(target.joinTimestamp.toLong() * 1000))
                                val qq = target.id
                                val nick = target.nick
                                val nameCard = target.nameCard
                                var coin = Data.coin[target.id]
                                if (coin == null) {
                                    coin = 0
                                }
                                subject.sendMessage(buildMessageChain {
                                    +PlainText("QQ: $qq($nick)\n")
                                    +PlainText("群名片: $nameCard\n")
                                    +PlainText("Coin: $coin\n")
                                    +PlainText("最后一次发言时间: $lastSpeakTime\n")
                                    +PlainText("入群时间: $joinTime")
                                })
                            }
                        } else {
                            subject.sendMessage("参数错误,请使用\"help\"来获取帮助")
                        }
                    }
                    msg[0] == "cointop" || msg[0] == "金币排行" -> {
                        val sortedmap = Data.coin.entries.sortedByDescending { it.value }
                            .associateBy({ it.key }, { it.value })

                        val builder = MessageChainBuilder()
                        val iterator = sortedmap.iterator()
                        builder.add(PlainText("Coin 排行榜\n"))
                        var allCoin = 0
                        var index = 1
                        while (iterator.hasNext()) {
                            val key = iterator.next().key
                            val value = Data.coin[key]
                            builder.add(PlainText("$index.") + At(key) + PlainText("($key): $value\n"))
                            index++
                            if (value != null) {
                                allCoin += value
                            }
                        }
                        builder.add(PlainText("Coin 总和: $allCoin"))
                        subject.sendMessage(builder.build())
                    }
                    msg[0].startsWith("getcoin", true) || msg[0].startsWith("抢劫") -> {
                        val at: At? = message.findIsInstance<At>()
                        if (at != null) {
                            var target: NormalMember? = null
                            for (member in group.members) {
                                if (member.id == at.target) {
                                    target = member
                                    break
                                }
                            }
                            if (target != null) {
                                if (target != sender) {
                                    subject.sendMessage("确认吗?")
                                    val confirm: Boolean = selectMessages {
                                        startsWith("y") { true }
                                        startsWith("n") { false }
                                        startsWith("是") { true }
                                        startsWith("否") { false }
                                        startsWith("确认") { true }
                                        startsWith("取消") { false }
                                        timeout(10_000) { false }
                                    }
                                    if (confirm) {
                                        var tof = (1..10).random() <= 4
                                        if (Config.WHITELISTS.contains(target.id)) {
                                            tof = false
                                        }
                                        val targetCoin = Data.coin[target.id]
                                        val selfCoin = Data.coin[sender.id]
                                        val lostCoin: Int
                                        if (selfCoin != null) {
                                            lostCoin = (selfCoin * 0.1).roundToInt()
                                        } else {
                                            subject.sendMessage("你至少需要1 Coin来进行这个操作")
                                            return@subscribeAlways
                                        }
                                        if (targetCoin != null) {
                                            val getCoin = (targetCoin * 0.3).roundToInt()
                                            if (tof) {
                                                Data.coin[sender.id] = selfCoin + getCoin
                                                Data.coin[target.id] = targetCoin - getCoin
                                                subject.sendMessage(
                                                    At(sender) + "成功,你获得了$getCoin(${Data.coin[sender.id]}) Coin,目标还剩${Data.coin[target.id]} Coin"
                                                )
                                            } else {
                                                Data.coin[sender.id] = selfCoin - lostCoin
                                                subject.sendMessage(At(sender) + "失败,你丢失了$lostCoin Coin,你还剩${Data.coin[sender.id]} Coin")
                                            }
                                        } else {
                                            subject.sendMessage("你的目标没有任何的Coin")
                                        }
                                    } else {
                                        subject.sendMessage("已取消")
                                    }
                                } else {
                                    subject.sendMessage("?")
                                }
                            }
                        } else {
                            subject.sendMessage("参数错误,请使用\"help\"来获取帮助")
                        }
                    }
                    msg[0] == "qd" || msg[0] == "签到" -> {
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
                                Data.qdED = LongArray(1)
                                Data.qdED[0] = sender.id
                            } else {
                                val newARRAY = LongArray(Data.qdED.size + 1)
                                for (i in Data.qdED.indices) {
                                    newARRAY[i] = Data.qdED[i]
                                }
                                newARRAY[Data.qdED.size] = sender.id
                                Data.qdED = newARRAY
                            }

                            val userAllQD = Data.allQD[sender.id]
                            if (userAllQD != null) {
                                Data.allQD[sender.id] = userAllQD + 1
                            } else {
                                Data.allQD[sender.id] = 1
                            }

                            subject.sendMessage(buildMessageChain {
                                +At(sender)
                                +PlainText("\n")
                                +PlainText("签到成功\n")
                                +PlainText("你是今天第${Data.qdCount}位签到的\n")
                                +PlainText("你已累计签到${Data.allQD[sender.id]}天\n")
                                +PlainText("你获得了 $gotCoin Coin\n")
                                +PlainText("你现在有 ${Data.coin[sender.id]} Coin")
                            })
                        }
                    }
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
        logger.info { "Plugin unloaded" }
    }
}