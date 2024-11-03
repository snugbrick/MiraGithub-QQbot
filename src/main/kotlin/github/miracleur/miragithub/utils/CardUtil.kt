package github.miracleur.miragithub.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

class CardUtil {
    /**
     * 返回卡片
     */
    @Throws(Exception::class)
    suspend fun getOldCard(message: String, html: String, avatar: String, name: String, time: String, event: Contact): Message {

        val toExternalResource = ImageUtil.Companion.getImage(avatar).toByteArray().toExternalResource()
        val imageId: String = toExternalResource.uploadAsImage(event).imageId
        withContext(Dispatchers.IO) {
            toExternalResource.close()
        }

        return Image(imageId).plus(name).plus("\n")
            .plus("时间：$time").plus("\n")
            .plus("介绍：$message").plus("\n")
            .plus("网址：$html").plus("\n")
    }

    @Throws(Exception::class)
    suspend fun getNewCard(shaMsg: String, message: String, projects: String, avatar: String, name: String, time: String, event: Contact): Image {
        val avatarResource: ExternalResource = ImageUtil.Companion.getImage(avatar).toByteArray().toExternalResource()
        //val avatarImageId: String = avatarResource.uploadAsImage(event).imageId
        withContext(Dispatchers.IO) {
            avatarResource.close()
        }

        val cardWidth = 1200
        val cardHeight = 700
        val cardImage = BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_RGB)
        val g: Graphics2D = cardImage.createGraphics()

        g.color = Color.WHITE
        g.fillRect(0, 0, cardWidth, cardHeight)

        g.font = Font("Microsoft YaHei", Font.PLAIN, 36)
        g.color = Color.BLACK

        val avatarInputStream = ByteArrayInputStream(ImageUtil.Companion.getImage(avatar).toByteArray())
        val avatarBufferedImage = ImageIO.read(avatarInputStream)
        g.drawImage(avatarBufferedImage, 800, 100, 300, 300, null)

        // 下载并绘制 GitHub logo
        val githubLogoUrl = URL("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png")
        val githubLogo = ImageIO.read(githubLogoUrl)
        g.drawImage(githubLogo, 1000, 500, 100, 100, null)

        val code = ImageIO.read(this::class.java.classLoader.getResource("code.png"))
        val clock = ImageIO.read(this::class.java.classLoader.getResource("clock.png"))
        val chat = ImageIO.read(this::class.java.classLoader.getResource("chat.png"))
        val key = ImageIO.read(this::class.java.classLoader.getResource("key.png"))
        g.drawImage(code, 30, 120, 40, 40, null)
        g.drawImage(clock, 30, 170, 40, 40, null)
        g.drawImage(chat, 30, 220, 40, 40, null)
        g.drawImage(key, 30, 270, 40, 40, null)

        g.drawString("$name commit code for $projects", 100, 120)
        g.drawString("time: $time", 100, 170)
        g.drawString("message: $message", 100, 220)
        g.drawString("sha: $shaMsg", 100, 270)

        // 在底部绘制颜色带 (205, 255, 247)
        g.color = Color(205, 255, 247)
        val bandHeight = cardHeight / 7
        g.fillRect(0, cardHeight - bandHeight, cardWidth, bandHeight)

        g.dispose() // 释放

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(cardImage, "jpg", outputStream)

        val cardResource: ExternalResource = outputStream.toByteArray().toExternalResource()
        val imageId: String = cardResource.uploadAsImage(event).imageId
        withContext(Dispatchers.IO) {
            cardResource.close()
        }

        return Image(imageId)
    }
}