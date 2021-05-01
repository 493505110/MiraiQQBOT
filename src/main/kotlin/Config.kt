package ml.zhou2008

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("setting") {
    val ITPK_APIKEY: String by value()
    val ITPK_APISECRET: String by value()
}