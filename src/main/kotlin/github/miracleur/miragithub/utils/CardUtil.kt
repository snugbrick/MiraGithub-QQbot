package github.miracleur.miragithub.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO
import kotlin.random.Random

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
    suspend fun getNewCard(html: String, message: String, projects: String, avatar: String, name: String, time: String, event: Contact): Image {
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

        g.font = Font("Microsoft YaHei", Font.PLAIN, 32)
        g.color = Color.BLACK

        //头像
        val avatarInputStream = ByteArrayInputStream(ImageUtil.Companion.getImage(avatar).toByteArray())
        val avatarBufferedImage = ImageIO.read(avatarInputStream)
        g.drawImage(avatarBufferedImage, 1000, 0, 200, 200, null)

        // 在底部绘制颜色带 (205, 255, 247)
        g.color = Color(205, 255, 247)
        val bandHeight = cardHeight / 7
        g.fillRect(0, cardHeight - bandHeight, cardWidth, bandHeight)
        g.color = Color.GRAY

        // 下载并绘制 GitHub logo
        val githubLogoUrl = URL("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png")
        val githubLogo = ImageIO.read(githubLogoUrl)
        g.drawImage(githubLogo, 1050, 600, 100, 100, null)

        val x = 30
        val y = 120
        val code = ImageIO.read(this::class.java.classLoader.getResource("code.png"))
        val clock = ImageIO.read(this::class.java.classLoader.getResource("clock.png"))
        val chat = ImageIO.read(this::class.java.classLoader.getResource("chat.png"))
        val key = ImageIO.read(this::class.java.classLoader.getResource("key.png"))
        g.drawImage(code, x, y, 40, 40, null)
        g.drawImage(clock, x, y + 50, 40, 40, null)
        g.drawImage(chat, x, y + 100, 40, 40, null)
        g.drawImage(key, x, y + 150, 40, 40, null)

        g.color = Color.BLACK
        g.drawString("检测到 $name 为 ${projects.split("/")[1]} 提交了代码", x + 70, y + 32)
        g.drawString(time, x + 70, y + 50 + 32)
        g.drawString(message, x + 70, y + 100 + 32)
        g.drawString(html, x + 70, y + 150 + 32)

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

    @Throws(Exception::class)
    suspend fun getCard(html: String, message: String, projects: String, avatar: String, name: String, time: String, event: Contact): Image {
        val avatarResource: ExternalResource = ImageUtil.Companion.getImage(avatar).toByteArray().toExternalResource()
        val theImageId: String = avatarResource.uploadAsImage(event).imageId
        withContext(Dispatchers.IO) {
            avatarResource.close()
        }

        val cardWidth = 1200
        val cardHeight = 600
        val cardImage = BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_RGB)
        val g: Graphics2D = cardImage.createGraphics()

        //背景
        val randomNumber = Random.nextInt(0, 52)
        var backgroundImage = ImageIO.read(this::class.java.classLoader.getResource("backg/$randomNumber.png"))
        if(backgroundImage == null)
            backgroundImage = ImageIO.read(this::class.java.classLoader.getResource("backg/$randomNumber.jpg"))
        g.drawImage(backgroundImage, 0, 0, 1200, 600, null)

        // 创建高斯模糊操作
        val radius = 15
        val kernelSize = radius * 2 + 1
        val kernel = FloatArray(kernelSize * kernelSize) { 1f / (kernelSize * kernelSize) } // 均匀模糊
        val convolveOp = ConvolveOp(Kernel(kernelSize, kernelSize, kernel), ConvolveOp.EDGE_NO_OP, null)

        // 应用模糊效果
        val blurredImage = convolveOp.filter(backgroundImage, null)
        g.drawImage(blurredImage, 0, 0, 1200, 600, null)

        //头像
        val avatarInputStream = ByteArrayInputStream(ImageUtil.Companion.getImage(avatar).toByteArray())
        val avatarBufferedImage = ImageIO.read(avatarInputStream)
        drawRoundedImageWithOpacity(g, avatarBufferedImage, cardWidth / 20.0, 140.0, 200, 200, 60, 1.0F)

        //框框
        val boxLength = 370.0
        val boxHigh = 60
        drawBox(g, cardWidth / 20.0, boxLength, 200, boxHigh, 60, 0.5F)
        drawBox(g, 320.0, 60.0, 1159 - 320, 480, 60, 0.5F)

        //icon
        val x = 320 + 40
        val y = 60 + 40
        val code = ImageIO.read(this::class.java.classLoader.getResource("code.png"))
        val clock = ImageIO.read(this::class.java.classLoader.getResource("clock.png"))
        val chat = ImageIO.read(this::class.java.classLoader.getResource("chat.png"))
        val key = ImageIO.read(this::class.java.classLoader.getResource("key.png"))
        g.drawImage(code, x, y, 40, 40, null)
        g.drawImage(clock, x, y + 50, 40, 40, null)
        g.drawImage(chat, x, y + 100, 40, 40, null)
        g.drawImage(key, x, y + 150, 40, 40, null)

        //字
        g.font = Font("Microsoft YaHei", Font.PLAIN, 32)
        g.color = Color.BLACK
        if (name.length < 2)
            g.drawString(name, cardWidth / 20 + 68, boxLength.toInt() + (boxHigh / 2) * (5 / 4))
        else if (name.length > 2 && name.length < 4)
            g.drawString(name, cardWidth / 20 + 36, boxLength.toInt() + (boxHigh / 2) * (5 / 4))
        else
            g.drawString(name, cardWidth / 20 + 20, boxLength.toInt() + (boxHigh / 2) * (5 / 4))


        g.drawString("检测到 $name 为 ${projects.split("/")[1]} 提交了代码", x + 70, y + 32)
        g.drawString(time, x + 70, y + 50 + 32)
        g.drawString(message, x + 70, y + 100 + 32)
        g.drawString(html.asSequence().take(7).joinToString(""), x + 70, y + 150 + 32)


        // 下载并绘制 GitHub logo
//        val githubLogoUrl = URL("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png")
//        val githubLogo = ImageIO.read(githubLogoUrl)
//        g.drawImage(githubLogo, 1000, 500, 100, 100, null)
        g.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(cardImage, "jpg", outputStream)

        val cardResource: ExternalResource = outputStream.toByteArray().toExternalResource()
        val imageId: String = cardResource.uploadAsImage(event).imageId
        withContext(Dispatchers.IO) {
            cardResource.close()
        }

        return Image(imageId)
    }

    fun drawRoundedImageWithOpacity(g: Graphics2D, image: BufferedImage, x: Double, y: Double, width: Int, height: Int, cornerRadius: Int, opacity: Float) {
        val roundedRectangle = RoundRectangle2D.Double(x, y, width.toDouble(), height.toDouble(), cornerRadius.toDouble(), cornerRadius.toDouble())
        g.clip(roundedRectangle)

        val composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
        g.composite = composite

        g.drawImage(image, x.toInt(), y.toInt(), width, height, null)

        g.clip = null
    }

    fun drawBox(g: Graphics2D, x: Double, y: Double, width: Int, height: Int, cornerRadius: Int, opacity: Float) {
        val roundedRectangle = RoundRectangle2D.Double(x, y, width.toDouble(), height.toDouble(), cornerRadius.toDouble(), cornerRadius.toDouble())

        val composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
        g.composite = composite

        g.color = Color.WHITE
        g.fill(roundedRectangle)

        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
    }
}