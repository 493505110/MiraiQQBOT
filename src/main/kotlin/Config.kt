package ml.zhou2008

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("setting") {
    var APPID: String by value()
    var USERID: String by value("miraiqqbot")
    var AUTO_ACCEPT: Boolean by value(false)
}