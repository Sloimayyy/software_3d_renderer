package com.sloimay

import com.sloimay.helpers.deepCopyBufferedImg
import com.sloimay.smath.clamp
import com.sloimay.smath.floor
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class Texture(texturePath: String) {

    val img: BufferedImage

    init {
        val bufImg = ImageIO.read( object {}.javaClass.getResourceAsStream(texturePath) )
        img = deepCopyBufferedImg(bufImg)
    }

    fun pollPixel(u: Float, v: Float): Color {
        val x = (u.clamp(0f, 1f) * img.width).floor().toInt().clamp(0, img.width - 1)
        val y = ((1f - v).clamp(0f, 1f) * img.height).floor().toInt().clamp(0, img.height - 1)
        return Color(img.getRGB(x, y))
    }

}