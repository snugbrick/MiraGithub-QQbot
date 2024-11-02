package github.miracleur.miragithub.initial

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import github.miracleur.miragithub.MiraGithub
import github.miracleur.miragithub.GithubTask
import github.miracleur.miragithub.GithubTask.Companion.admin
import github.miracleur.miragithub.GithubTask.Companion.branches
import github.miracleur.miragithub.GithubTask.Companion.groups
import github.miracleur.miragithub.GithubTask.Companion.num
import github.miracleur.miragithub.GithubTask.Companion.project
import github.miracleur.miragithub.GithubTask.Companion.sha
import github.miracleur.miragithub.GithubTask.Companion.taskMillisecond
import github.miracleur.miragithub.GithubTask.Companion.token
import github.miracleur.miragithub.GithubTask.Companion.users
import github.miracleur.miragithub.github.Branches
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.plugin.name

import net.mamoe.mirai.utils.MiraiLogger

import java.io.File
import java.io.InputStream

class Configurations {
    companion object {
        private val systemPath: String = System.getProperty("user.dir")
        private val fileDirectory: File =
            File(systemPath + File.separator + "config" + File.separator + "com.hcyacg.github-notice")
        private val file: File = File(fileDirectory.path + File.separator + "setting.json")
        private var projectJson: JSONObject = JSONObject.parseObject("{}")
        val path: String = Configurations::class.java.protectionDomain.codeSource.location.path
        var logger: MiraiLogger = MiraiLogger.Factory.create(this::class, "Bot")

        /**
         * 初始化插件各项配置
         */
        fun init() {
            /**
             * 不存在配置文件将自动创建
             */

            if (!fileDirectory.exists() || !file.exists()) {
                fileDirectory.mkdirs()
                file.createNewFile()
                val resourceAsStream: InputStream? =
                    Configurations::class.java.classLoader.getResourceAsStream("setting.json")
                resourceAsStream?.let { file.writeBytes(it.readAllBytes()) }
                logger.warning("初始化配置文件,请在config/com.hcyacg.github-notice/setting.json配置相关参数")
            } else {
                load()
            }
        }

        /**
         * 重载配置文件
         */
        fun overload() {
            load()
            num = 0
            logger.info("配置文件已重载")
        }

        /**
         * 加载配置文件
         */
        fun load() {
            runBlocking {
                projectJson = JSONObject.parseObject(file.readText())

                project = JSON.parseArray(projectJson.getString("project"))
                for (e in project) {
                    sha[e.toString()] = ""
                }


                groups = JSON.parseArray(projectJson.getString("group"))
                for ((index, e) in groups.withIndex()) {
                    groups[index] = e.toString()
                }
                admin = JSON.parseArray(projectJson.getString("admin"))
                for ((index, e) in admin.withIndex()) {
                    admin[index] = e.toString()
                }
                users = JSON.parseArray(projectJson.getString("users"))
                for ((index, e) in users.withIndex()) {
                    users[index] = e.toString()
                }

                token = projectJson.getString("token")
                taskMillisecond = projectJson.getLong("task-millisecond")



                branches = try {
                    Branches().getBranchesByRepo(project)
                } catch (e: Exception) {
                    Branches().getBranchesByRepo(project)
                }

                GithubTask.all = 0
                for (p in project) {
                    GithubTask.all += branches[p]!!.size
                }
                if (GithubTask.all == 0) {
                    logger.warning("[${MiraGithub.name}]加载配置完成,共【${GithubTask.all}】个分支,请检查是否配置完成,或检查网络问题")
                } else {
                    logger.info("[${MiraGithub.name}]加载配置完成,共【${GithubTask.all}】个分支")
                }
                logger.info("[${MiraGithub.name}]请不要开启插件后立即上传代码,插件需要一段时间获取所有项目的分支相关数据")
            }
        }
    }


}
