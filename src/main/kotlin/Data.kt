package ml.zhou2008

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object Data : AutoSavePluginData("data") {
    var coin: MutableMap<Long, Int> by value()
    var qdCount: Int by value(0)
    var qdED: LongArray by value(LongArray(0))
    var allQD: MutableMap<Long, Int> by value()
}