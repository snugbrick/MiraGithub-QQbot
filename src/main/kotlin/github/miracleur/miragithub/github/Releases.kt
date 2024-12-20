package github.miracleur.miragithub.github

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import github.miracleur.miragithub.GithubTask
import github.miracleur.miragithub.GithubTask.Companion.releases
import github.miracleur.miragithub.GithubTask.Companion.token
import github.miracleur.miragithub.utils.CardUtil
import github.miracleur.miragithub.utils.HttpUtil
import github.miracleur.miragithub.entity.Release
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ContactUtils.getFriendOrGroup
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import java.lang.String
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*


class Releases {
    val logger: MiraiLogger = MiraiLogger.Factory.create(Releases::class, "Bot")
    private val headers = Headers.Builder().add("Accept","application/vnd.github.v3+json").add("Authorization","token $token")
    private val requestBody: RequestBody? = null

    @OptIn(ConsoleExperimentalApi::class)
    suspend fun checkReleaseUpdate(
        projects: Any?,
    ){
        val bots = Bot.instances
        var time: kotlin.String? = null
        try{
            if (!RateLimits().isResidue()){
                headers.removeAll("Authorization")
            }
            val data =HttpUtil.request(
                method = HttpUtil.Companion.Method.GET,
                uri = "https://api.github.com/repos/$projects/releases",
                body = requestBody,
                headers = headers.build(),
                logger = logger
            )

            if (null == data || JSONArray.parseArray(data).size < 1){
                return
            }

            val release = JSONObject.parseObject(JSONArray.parseArray(data)[0].toString(),Release::class.java)

            if (null == releases[projects.toString()]?.nodeId){
                releases[projects.toString()] = release
                return
            }

            if (releases[projects.toString()]!!.nodeId == release.nodeId){
                return
            }
            releases[projects.toString()] = release

            if (null != release.publishedAt){
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
                val myDate: Date = dateFormat.parse(release.publishedAt.toString().replace("Z", "+0000"))
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                time = sdf.format(Date(String.valueOf(myDate.time).toLong()))

            }

            for (e in GithubTask.groups) {
                for (bot in bots){
                    bot.getGroup(e.toString().toLong())?.sendMessage(
                        CardUtil().getOldCard(
                            message = release.name.toString(),
                            html = release.htmlUrl.toString(),
                            avatar = release.author!!.avatarUrl.toString(),
                            time = time.toString(),
                            name = release.assets!![0]!!.uploader!!.login.toString()+ "为${projects.toString()}更新了版本",
                            event = bot.getFriendOrGroup(e.toString().toLong())
                        )
                    )
                }
            }

            for (u in GithubTask.users) {
                for (bot in bots){
                    bot.getStranger(u.toString().toLong())?.sendMessage(
                        CardUtil().getOldCard(
                            message = release.name.toString(),
                            html = release.htmlUrl.toString(),
                            avatar = release.author!!.avatarUrl.toString(),
                            time = time.toString(),
                            name = release.assets!![0]!!.uploader!!.login.toString() + "为${projects.toString()}更新了版本",
                            event = bot.getFriendOrGroup(u.toString().toLong())
                        )
                    )
                }
            }
        }catch (e: SocketTimeoutException){
            logger.warning("请求超时")
            return
        }catch (e: ConnectException){
            GithubTask.logger.warning("无法连接到api.github.com")
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }




    }

}