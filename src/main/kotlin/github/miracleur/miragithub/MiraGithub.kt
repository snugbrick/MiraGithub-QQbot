package github.miracleur.miragithub


import github.miracleur.miragithub.command.Github
import github.miracleur.miragithub.initial.Configurations.Companion.init
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info

object MiraGithub : KotlinPlugin(
    JvmPluginDescription(id = "github.miracleur.miragithub", name = "MiraGithub", version = "0.1.0") {
        author("MiracleUR")
        info(
            """实时监控github仓库更新，并推送消息到QQ群""".trimIndent()
        )
        info(
            """在研究新功能喵 
                       --圣迹""".trimIndent()
        )
    }
) {
    override fun onEnable() {
        logger.info { "MiraGithub loaded" }
        CommandManager.registerCommand(Github(), true)
        globalEventChannel().subscribeGroupMessages {
            "/github rate_limit" reply "this command is not available now"
            "?" reply "¿"
            "¿" reply "?"
        }
    }

    /**
     * 初始化配置文件 以及每次开启时读取配置文件
     */
    override fun PluginComponentStorage.onLoad() {
        init()
    }
}
