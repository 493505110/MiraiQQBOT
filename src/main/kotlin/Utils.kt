package ml.zhou2008

import com.alibaba.fastjson.JSONObject
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import java.net.URL
import java.net.URLEncoder

class Utils {
    companion object {
        fun botGetREP(spoken: String): String {
            val url = "https://api.ownthink.com/bot?appid=${Config.APPID}&userid=${Config.USERID}&spoken=${URLEncoder.encode(spoken, Charsets.UTF_8)}"
            val jsonStr = URL(url).readText()
            val jsonObj = JSONObject.parseObject(jsonStr)
            return jsonObj.getJSONObject("data").getJSONObject("info").getString("text")
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        suspend fun music(name: String, group: Group) {
            fun getRealURL(mid: String): String {
                val url = URL(
                    "https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22$mid\"%5D%2C\"songtype\"%3A%5B0%5D%2C\"uin\"%3A\"1443481947\"%2C\"loginflag\"%3A1%2C\"platform\"%3A\"20\"%7D%7D%2C\"comm\"%3A%7B\"uin\"%3A\"18585073516\"%2C\"format\"%3A\"json\"%2C\"ct\"%3A24%2C\"cv\"%3A0%7D%7D"
                )
                val jo = JSONObject.parseObject(url.readText()).getJSONObject("req_0")
                var playURL = jo.getJSONObject("data").getJSONArray("sip").getString(0)
                playURL += jo.getJSONObject("data").getJSONArray("midurlinfo").getJSONObject(0).getString("purl")
                return playURL
            }

            val url = URL(
                "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?aggr=1&flag_qc=0&n=3&format=json&w=${
                    URLEncoder.encode(
                        name,
                        Charsets.UTF_8
                    )
                }"
            )
            val ss =
                JSONObject.parseObject(url.readText()).getJSONObject("data").getJSONObject("song").getJSONArray("list")
            val song = ss.getJSONObject(0)
            val musicName = song.getString("songname")
            val musicID = song.getString("songmid")
            val musicURL = getRealURL(musicID)
            val jumpURL =
                "https://i.y.qq.com/v8/playsong.html?_wv=1&songid=${song.getString("songid")}&source=qqshare&ADTAG=qqshare"
            val picURL = "http://y.gtimg.cn/music/photo_new/T002R300x300M000${song.getString("albummid")}.jpg"
            val desc = try {
                val singers = song.getJSONArray("singer")
                val sgs = StringBuilder()
                for (i in 0 until singers.size) {
                    sgs.append(singers.getJSONObject(i).getString("name"))
                    sgs.append(";")
                }
                sgs.deleteCharAt(sgs.length - 1)
                sgs.toString()
            } catch (e: Exception) {
                song.getString("albumname")
            }
            if (musicURL.endsWith("/")) {
                group.sendMessage("暂不支持")
            } else {
                group.sendMessage(
                    MusicShare(
                        kind = MusicKind.QQMusic,
                        title = musicName,
                        summary = desc,
                        jumpUrl = jumpURL,
                        pictureUrl = picURL,
                        musicUrl = musicURL
                    )
                )
            }
        }
    }
}