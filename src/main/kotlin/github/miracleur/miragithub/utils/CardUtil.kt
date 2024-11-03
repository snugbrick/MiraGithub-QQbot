package github.miracleur.miragithub.utils

import github.miracleur.miragithub.GithubTask.Companion.logger
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
        logger.info("准备生成图片")
        val avatarResource: ExternalResource = ImageUtil.Companion.getImage(avatar).toByteArray().toExternalResource()
        //val avatarImageId: String = avatarResource.uploadAsImage(event).imageId
        withContext(Dispatchers.IO) {
            avatarResource.close()
        }
        logger.info("已获得头像")

        val cardWidth = 1200
        val cardHeight = 600
        val cardImage = BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_RGB)
        val g: Graphics2D = cardImage.createGraphics()
        logger.info("已构建图片")

        g.color = Color.WHITE
        g.fillRect(0, 0, cardWidth, cardHeight)

        g.font = Font("Segoe UI", Font.PLAIN, 24)
        g.color = Color.BLACK

        val avatarInputStream = ByteArrayInputStream(ImageUtil.Companion.getImage(avatar).toByteArray())
        val avatarBufferedImage = ImageIO.read(avatarInputStream)
        g.drawImage(avatarBufferedImage, 1000, 200, 100, 100, null)
        logger.info("已绘制头像")

        g.drawString(name, 150, 100)
        g.drawString("时间：$time", 150, 150)
        g.drawString("介绍：$message", 150, 200)
        g.drawString("网址：$html", 150, 250)
        g.dispose() // 释放

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(cardImage, "jpg", outputStream)

        val cardResource: ExternalResource = outputStream.toByteArray().toExternalResource()
        val imageId: String = cardResource.uploadAsImage(event).imageId
        withContext(Dispatchers.IO) {
            cardResource.close()
        }

        logger.info("Card uploaded: $imageId,$name,$time,$message")

        return Image(imageId)
    }
}