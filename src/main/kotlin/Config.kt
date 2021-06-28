package ml.zhou2008

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("setting") {
    var APPID: String by value()
    var USERID: String by value("miraiqqbot")
    var AUTO_ACCEPT: Boolean by value(false)
    var WHITELISTS: MutableList<Long> by value()
    var BLACKLISTS: MutableList<Long> by value()
    var GETCOINENABLED: Boolean by value(true)
    var UNKNOWNARG: String by value("参数错误,请使用\"help\"来获取帮助")
    var N: Int by value(25)
    var GCW: Float by value(0.3F)
    var GCL: Float by value(0.1F)
}