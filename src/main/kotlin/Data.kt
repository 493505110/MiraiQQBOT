package ml.zhou2008

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object Data : AutoSavePluginData("data") {
    var coin: MutableMap<Long, Int> by value()
    var qdCount: Int by value(0)
    var qdED: MutableList<Long> by value()
    var allQD: MutableMap<Long, Int> by value()
    var Inventory: MutableMap<Long, MutableMap<String, Int>> by value()
    var ITEM: Array<String> by value(arrayOf())
    var ItemDesc: MutableMap<String, String> by value(mutableMapOf(
        Pair("每日礼包", "10 Coin!")
    ))
    var ItemPrice: MutableMap<String, Int> by value(mutableMapOf(
        Pair("每日礼包", 0)
    ))
    var ItemCount: MutableMap<String, Int> by value(mutableMapOf(
        Pair("每日礼包", 1)
    ))
}