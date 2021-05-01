package ml.zhou2008

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("setting") {
    val ConfigList = arrayOf(
        "ITPK_APIKEY",
        "ITPK_APISECRET"
    )

    var ITPK_APIKEY: String by value()
    var ITPK_APISECRET: String by value()
}