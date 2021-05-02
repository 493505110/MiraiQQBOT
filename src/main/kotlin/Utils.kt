package ml.zhou2008

import com.alibaba.fastjson.JSONObject
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsVoice
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder
import java.util.*


class Utils {
    companion object {
        private fun transferTo(`in`: InputStream): Long {
            Objects.requireNonNull(System.out, "out")
            var transferred: Long = 0
            val buffer = ByteArray(2048)
            var read: Int
            while (`in`.read(buffer, 0, 2048).also { read = it } >= 0) {
                System.out.write(buffer, 0, read)
                transferred += read.toLong()
            }
            return transferred
        }

        private fun exeCmd(commandStr: String) {
            try {
                val p = Runtime.getRuntime().exec(commandStr)
                transferTo(p.inputStream)
                p.waitFor()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        fun botGetREP(spoken: String): String {
            val url = "https://api.ownthink.com/bot?appid=${Config.APPID}&userid=${Config.USERID}&spoken=${URLEncoder.encode(spoken, Charsets.UTF_8)}"
            val jsonStr = URL(url).readText()
            val jsonObj = JSONObject.parseObject(jsonStr)
            return jsonObj.getJSONObject("data").getJSONObject("info").getString("text")
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        suspend fun tts(text: String, group: Group) {
            val ffmpeg = File("ffmpeg.exe")
            val silkEncoder = File("silk_v3_encoder.exe")
            val ttsURL = "https://fanyi.baidu.com/gettts?lan=zh&spd=5&text=${URLEncoder.encode(text, Charsets.UTF_8)}"
            val stream = URL(ttsURL).openStream()

            if (ffmpeg.exists() && silkEncoder.exists()) {
                val mp3 = File("mirai_${System.currentTimeMillis()}.mp3")
                val pcm = File("mirai_${System.currentTimeMillis()}.pcm")
                val silk = File("mirai_${System.currentTimeMillis()}.silk")

                mp3.writeBytes(stream.readAllBytes())

                try {
                    exeCmd("${ffmpeg.absolutePath} -i ${mp3.absolutePath} -f s16le -ar 24000 -ac 1 -acodec pcm_s16le -y ${pcm.absolutePath}")
                    exeCmd("${silkEncoder.absolutePath} ${pcm.absolutePath} ${silk.absolutePath} -Fs_API 24000 -tencent -quiet")
                } catch (e: IOException) {
                    group.sendMessage(e.toString())
                }
                val er = silk.toExternalResource()
                group.sendMessage(er.uploadAsVoice(group))
                mp3.delete()
                pcm.delete()
                stream.close()
                er.close()
                silk.delete()
            } else {
                group.sendMessage("ffmpeg或silk编码器丢失")
            }
        }
    }
}