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
    suspend fun getNewCard(message: String, html: String, avatar: String, name: String, time: String, event: Contact): Image {
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

        g.font = Font("Arial", Font.PLAIN, 32)
        g.color = Color.BLACK

        val avatarInputStream = ByteArrayInputStream(ImageUtil.Companion.getImage(avatar).toByteArray())
        val avatarBufferedImage = ImageIO.read(avatarInputStream)
        g.drawImage(avatarBufferedImage, 800, 100, 400, 400, null)

        // 下载并绘制 GitHub logo
        val githubLogoUrl = URL("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png")
        val githubLogo = ImageIO.read(githubLogoUrl)
        g.drawImage(githubLogo, cardWidth - 120, cardHeight - 120, 100, 100, null)

        g.drawString(name, 50, 100)
        g.drawString("时间：$time", 50, 170)
        g.drawString("介绍：$message", 50, 220)
        g.drawString("网址：$html", 50, 270)

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