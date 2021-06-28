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
import net.mamoe.mirai.event.whileSelectMessages
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
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
                    Data.qdED.clear()
                    Data.ItemCount = mutableMapOf(
                        Pair("每日礼包", 1)
                    )
                }
            },
            calendar.time,
            86400000L
        )

        if (Config.APPID.isEmpty() || Config.USERID.isEmpty()) {
            logger.warning { "APPID or USERID no set" }
        }

        var enabled = true

        subscribeAlways<GroupMessageEvent> {
            if (message.content == "admin console") {
                if (Config.WHITELISTS.contains(sender.id)) {
                    subject.sendMessage("Welcome to admin console.")
                    whileSelectMessages {
                        "exit" { subject.sendMessage("Exiting..."); false }
                        default {
                            val caa = it.split(" ")
                            when {
                                caa[0] == "achelp" -> { subject.sendMessage("""
                                    disable/enable [getcoin]
                                    addto/removein black/white list
                                    set n/gcw/gcl
                                """.trimIndent()) }
                                caa[0] == "disable" -> { enabled=false; subject.sendMessage("OK") }
                                caa[0] == "enable" -> { enabled=true; subject.sendMessage("OK") }
                                caa[0] == "addtoblacklist" -> {
                                    if (caa.size == 2) {
                                        val user = caa[1].toLongOrNull()
                                        if (user != null) {
                                            Config.BLACKLISTS.add(user)
                                            subject.sendMessage("OK")
                                        } else {
                                            subject.sendMessage("Invalid params.")
                                        }
                                    } else {
                                        subject.sendMessage("Invalid params.")
                                    }
                                }
                                caa[0] == "removeinblacklist" -> {
                                    if (caa.size == 2) {
                                        val user = caa[1].toLongOrNull()
                                        if (user != null) {
                                            Config.BLACKLISTS.remove(user)
                                            subject.sendMessage("OK")
                                        } else {
                                            subject.sendMessage("Invalid params.")
                                        }
                                    } else {
                                        subject.sendMessage("Invalid params.")
                                    }
                                }
                                caa[0] == "addtowhitelist" -> {
                                    if (caa.size == 2) {
                                        val user = caa[1].toLongOrNull()
                                        if (user != null) {
                                            Config.WHITELISTS.add(user)
                                            subject.sendMessage("OK")
                                        } else {
                                            subject.sendMessage("Invalid params.")
                                        }
                                    } else {
                                        subject.sendMessage("Invalid params.")
                                    }
                                }
                                caa[0] == "removeinwhitelist" -> {
                                    if (caa.size == 2) {
                                        val user = caa[1].toLongOrNull()
                                        if (user != null) {
                                            Config.WHITELISTS.remove(user)
                                            subject.sendMessage("OK")
                                        } else {
                                            subject.sendMessage("Invalid params.")
                                        }
                                    } else {
                                        subject.sendMessage("Invalid params.")
                                    }
                                }
                                caa[0] == "setn" -> {
                                    if (caa.size == 2) {
                                        val user = caa[1].toIntOrNull()
                                        if (user != null) {
                                            Config.N = user
                                            subject.sendMessage("OK")
                                        } else {
                                            subject.sendMessage("Invalid params.")
                                        }
                                    } else {
                                        subject.sendMessage("Invalid params.")
                                    }
                                }
                                caa[0] == "setgcw" -> {
                                    if (caa.size == 2) {
                                        val user = caa[1].toFloatOrNull()
                                        if (user != null) {
                                            Config.GCW = user
                                            subject.sendMessage("OK")
                                        } else {
                                            subject.sendMessage("Invalid params.")
                                        }
                                    } else {
                                        subject.sendMessage("Invalid params.")
                                    }
                                }
                                caa[0] == "setgcl" -> {
                                    if (caa.size == 2) {
                                        val user = caa[1].toFloatOrNull()
                                        if (user != null) {
                                            Config.GCL = user
                                            subject.sendMessage("OK")
                                        } else {
                                            subject.sendMessage("Invalid params.")
                                        }
                                    } else {
                                        subject.sendMessage("Invalid params.")
                                    }
                                }
                                caa[0] == "disablegetcoin" -> { Config.GETCOINENABLED=false; subject.sendMessage("OK") }
                                caa[0] == "enablegetcoin" -> { Config.GETCOINENABLED=true; subject.sendMessage("OK") }
								else -> { subject.sendMessage("Unknown command.") }
                            }
                            true
                        }
                    }
                } else {
                    subject.sendMessage("Access denied.")
                }
            }
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        subscribeAlways<GroupMessageEvent> {
            if (!Config.BLACKLISTS.contains(sender.id) && enabled) {
                val msg = message.content.split(" ")
                when {
                    msg[0].startsWith("-") -> {
                        subject.sendMessage(botGetREP(msg[0].removePrefix("-")))
                    }
                    msg[0] == "help" -> {
                        subject.sendMessage(buildMessageChain {
                            +"指令列表:\n"
                            +"  qd 签到\n"
                            +"  cointop Coin排行榜\n"
                            +"  query <target> 查询某人在这个群的信息\n"
                            +"  getavatar <qq> 获取目标头像\n"
                            +"  music <name> 点歌\n"
                            +"  getcoin <target> 抢劫\n"
                            +"  baike <name> 百科\n"
                            +"  inv 查看物品栏\n"
                            +"  use <name> 使用物品\n"
                            +"  desc <name> 查看物品描述\n"
                            +"  allitem 所有物品\n"
                            +"  shop 商店\n"
                            +"  mycoin 金币\n"
                            +"  admin console 管理员控制台"
                        })
                    }
                    msg[0] == "帮助" -> {
                        subject.sendMessage(buildMessageChain {
                            +"指令列表:\n"
                            +"  签到\n"
                            +"  金币排行\n"
                            +"  查询 <目标>\n"
                            +"  获取头像 <qq>\n"
                            +"  点歌 <歌名>\n"
                            +"  抢劫 <目标>\n"
                            +"  百科 <条目名>\n"
                            +"  物品栏\n"
                            +"  使用 <物品名>\n"
                            +"  描述 <物品名>\n"
                            +"  所有物品\n"
                            +"  商店\n"
                            +"  金币"
                        })
                    }
                    msg[0] == "mycoin" || msg[0] == "金币" -> {
                        subject.sendMessage("Coin: ${Data.coin[sender.id] ?: 0}")
                    }
                    msg[0] == "music" || msg[0] == "点歌" -> {
                        if (msg.size == 2) {
                            Utils.music(msg[1], subject)
                        } else {
                            subject.sendMessage(Config.UNKNOWNARG)
                        }
                    }
                    msg[0] == "getavatar" || msg[0] == "获取头像" -> {
                        if (msg.size == 2) {
                            val er =
                                URL("http://q1.qlogo.cn/g?b=qq&nk=${msg[1]}&s=640").openStream().toExternalResource()
                            subject.sendMessage(subject.uploadImage(er))
                            er.close()
                        } else {
                            subject.sendMessage(Config.UNKNOWNARG)
                        }
                    }
                    msg[0] == "desc" || msg[0] == "描述" -> {
                        if (msg.size == 2) {
                            val desc = Data.ItemDesc[msg[1]]
                            if (desc != null) {
                                subject.sendMessage(desc)
                            } else {
                                subject.sendMessage("这个物品不存在")
                            }
                        } else {
                            subject.sendMessage(Config.UNKNOWNARG)
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
                            subject.sendMessage(Config.UNKNOWNARG)
                        }
                    }
                    msg[0] == "allitem" || msg[0] == "所有物品" -> {
                        val builder = MessageChainBuilder()
                        for (item in Data.ItemDesc) {
                            builder.add("${item.key}\n")
                        }
                        subject.sendMessage(builder.build())
                    }
                    msg[0] == "inv" || msg[0] == "inventory" || msg[0] == "物品栏" -> {
                        val inv = Data.Inventory[sender.id]
                        if (inv != null) {
                            val builder = StringBuilder()
                            for (item in inv) {
                                builder.append("${item.key}: ${item.value}\n")
                            }
                            builder.delete(builder.length - 2, builder.length)
                            if (builder.isEmpty()) {
                                builder.append("你没有任何物品")
                            }
                            subject.sendMessage(builder.toString())
                        } else {
                            subject.sendMessage("你没有任何物品")
                        }
                    }
                    msg[0] == "use" || msg[0] == "使用" -> {
                        if (msg.size >= 2) {
                            val itemName = msg[1]
                            val itemCount = Data.Inventory[sender.id]?.get(itemName)
                            if (itemCount != null) {
                                var count = 1
                                if (msg.size == 3) {
                                    val count2 = msg[2].toIntOrNull()
                                    if (count2 != null) {
                                        count = count2
                                    } else {
                                        subject.sendMessage("请确保你输入的是整数")
                                        return@subscribeAlways
                                    }
                                }
                                if (Data.ITEM.contains(itemName)) {
                                    subject.sendMessage("该物品无法使用")
                                    return@subscribeAlways
                                }
                                if (count > itemCount) {
                                    subject.sendMessage("你没有这么多的$itemName,你只有" + itemCount + "个")
                                    return@subscribeAlways
                                }
                                for (i in (1..count)) {
                                    when (itemName) {
                                        "每日礼包" -> {
                                            Data.coin[sender.id] = (Data.coin[sender.id] ?: 0) + 10
                                            subject.sendMessage("Coin +10")
                                        }
                                    }
                                }
                                Data.Inventory[sender.id]?.set(itemName, itemCount-count)
                                if (itemCount-count == 0) {
                                    Data.Inventory[sender.id]?.remove(itemName)
                                }
                            } else {
                                subject.sendMessage("你没有这个物品")
                            }
                        } else {
                            subject.sendMessage(Config.UNKNOWNARG)
                        }
                    }
                    msg[0] == "test" -> {
                        Data.coin[sender.id] = (Data.coin[sender.id] ?: 0) + 10
                    }
                    msg[0] == "shop" || msg[0] == "商店" -> {
                        val builder = MessageChainBuilder()
                        builder.add("商店有以下物品出售:\n")
                        for (item in Data.ItemDesc) {
                            builder.add("${item.key}(${Data.ItemCount[item.key]}): ${Data.ItemPrice[item.key]} Coin\n")
                        }
                        builder.add("使用\"q\"来退出,输入\"物品名 数量\"来购买.")
                        subject.sendMessage(builder.build())
                        whileSelectMessages {
                            "q" { subject.sendMessage("已退出"); false }
                            "退出" { subject.sendMessage("已退出"); false }
                            default {
                                val nameANDcount = it.split(" ")
                                val itemName = nameANDcount[0]
                                val count = nameANDcount[1].toIntOrNull() ?: 1
                                if (Data.ItemDesc.containsKey(itemName)) {
                                    val selfCoin = Data.coin[sender.id] ?: 0
                                    val itemPrice = Data.ItemPrice[itemName] ?: 0
                                    val itemCount = Data.ItemCount[itemName] ?: 0
                                    if (itemPrice < selfCoin) {
                                        if (itemCount > 0) {
                                            if (count <= itemCount) {
                                                if (Data.Inventory[sender.id] != null) {
                                                    Data.Inventory[sender.id]?.set(
                                                        itemName, (Data.Inventory[sender.id]?.get(itemName)
                                                            ?: 0) + 1
                                                    )
                                                } else {
                                                    Data.Inventory[sender.id] = mutableMapOf(Pair(itemName,1))
                                                }
                                                Data.ItemCount[itemName] = itemCount - count
                                                Data.coin[sender.id] = selfCoin - itemPrice
                                                subject.sendMessage("成功,现在你有${Data.Inventory[sender.id]?.get(itemName)}个$itemName")
                                            } else {
                                                subject.sendMessage("商店里没有这么多的$itemName,只有" + itemCount + "个")
                                            }
                                        } else {
                                            subject.sendMessage("这个物品已经卖光了")
                                        }
                                    } else {
                                        subject.sendMessage("你没有足够的Coin,你只有$selfCoin Coin")
                                    }
                                } else {
                                    subject.sendMessage("没有这个物品")
                                }
                                true
                            }
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
                                    +"QQ: $qq($nick)\n"
                                    +"群名片: $nameCard\n"
                                    +"Coin: $coin\n"
                                    +"最后一次发言时间: $lastSpeakTime\n"
                                    +"入群时间: $joinTime"
                                })
                            }
                        } else {
                            subject.sendMessage(Config.UNKNOWNARG)
                        }
                    }
                    msg[0] == "cointop" || msg[0] == "金币排行" -> {
                        val sortedmap = Data.coin.entries.sortedByDescending { it.value }
                            .associateBy({ it.key }, { it.value })

                        val builder = MessageChainBuilder()
                        val iterator = sortedmap.iterator()
                        builder.add("Coin 排行榜\n")
                        var allCoin = 0
                        var index = 1
                        while (iterator.hasNext()) {
                            val key = iterator.next().key
                            val value = Data.coin[key]
                            builder.add("$index.[mirai:at:$key]($key): $value\n".deserializeMiraiCode())
                            index++
                            if (value != null) {
                                allCoin += value
                            }
                        }
                        builder.add("Coin 总和: $allCoin")
                        subject.sendMessage(builder.build())
                    }
                    msg[0].startsWith("getcoin", true) || msg[0].startsWith("抢劫") -> {
                        if (Config.GETCOINENABLED) {
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
                                        var tof = (1..100).random() <= Config.N
                                        if (Config.WHITELISTS.contains(sender.id)) {
                                            tof = true
                                        }
                                        val targetCoin = Data.coin[target.id]
                                        val selfCoin = Data.coin[sender.id]
                                        val lostCoin: Int
                                        if (selfCoin != null) {
                                            lostCoin = (selfCoin * Config.GCL).roundToInt()
                                        } else {
                                            subject.sendMessage("你至少需要1 Coin来进行这个操作")
                                            return@subscribeAlways
                                        }
                                        if (targetCoin != null) {
                                            val getCoin = (targetCoin * Config.GCW).roundToInt()
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
                                                subject.sendMessage("已取消")
                                            }
                                        } else {
                                            subject.sendMessage("你的目标没有任何的Coin")
                                        }
                                    } else {
                                        subject.sendMessage("?")
                                    }
                                }
                            } else {
                                subject.sendMessage(Config.UNKNOWNARG)
                            }
                        } else {
                            subject.sendMessage("暂时停用")
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

                            Data.qdED.add(sender.id)

                            val userAllQD = Data.allQD[sender.id]
                            if (userAllQD != null) {
                                Data.allQD[sender.id] = userAllQD + 1
                            } else {
                                Data.allQD[sender.id] = 1
                            }

                            subject.sendMessage(buildMessageChain {
                                +At(sender)
                                +"\n"
                                +"签到成功\n"
                                +"你是今天第${Data.qdCount}位签到的\n"
                                +"你已累计签到${Data.allQD[sender.id]}天\n"
                                +"你获得了 $gotCoin Coin\n"
                                +"你现在有 ${Data.coin[sender.id]} Coin"
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