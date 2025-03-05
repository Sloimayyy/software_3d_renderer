package com.sloimay

import com.sloimay.smath.Utils
import com.sloimay.smath.vectors.IVec2
import com.sloimay.smath.vectors.IVec3
import com.sloimay.smath.vectors.Vec2
import com.sloimay.smath.vectors.Vec3
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Stroke
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JFrame
import kotlin.math.PI
import kotlin.math.roundToInt

typealias KeyCode = Int

class Window(
    val title: String = "Title",
    val res: IVec2
) {
    val frame: JFrame
    private lateinit var bufferedImg: BufferedImage
    private lateinit var imgGraphics: Graphics2D

    private val instantKeyPressedMap: ConcurrentHashMap<KeyCode, Boolean>
    private val tickedKeyPressedMap: ConcurrentHashMap<KeyCode, Boolean>

    var frameCount = 0; private set

    init {
        frame = JFrame(title)
        frame.bounds = Rectangle(0, 0, res.x, res.y)
        frame.isResizable = false
        frame.isVisible = true

        this.instantKeyPressedMap = ConcurrentHashMap()
        this.tickedKeyPressedMap = ConcurrentHashMap()

        frame.addKeyListener(object : KeyListener {
            fun setKeyPressedInMap(keyCode: Int, pressed: Boolean) {
                instantKeyPressedMap[keyCode] = pressed
            }

            override fun keyPressed(e: KeyEvent?) {
                if (e == null) return
                setKeyPressedInMap(e.keyCode, true)
            }

            override fun keyReleased(e: KeyEvent?) {
                if (e == null) return
                setKeyPressedInMap(e.keyCode, false)
            }

            override fun keyTyped(e: KeyEvent?) { }
        })

        resetBufferedImg()
    }

    fun tickKeys() {
        for ((k, v) in this.instantKeyPressedMap) {
            this.tickedKeyPressedMap[k] = v
        }
    }

    fun isPressed(keyCode: Int): Boolean {
        return this.tickedKeyPressedMap[keyCode] ?: false
    }



    fun setRgb(x: Int, y: Int, col: Color) {
        if (x < bufferedImg.width && x >= 0 && y >= 0 && y < bufferedImg.height) {
            bufferedImg.setRGB(x, y, col.rgb)
        }
    }

    fun drawCircle(x: Int, y: Int, r: Float, col: Color) {
        val pointCount = 64
        val points = mutableListOf<Vec2>()
        for (i in 0 until pointCount) {
            val angle = Utils.remap(i.toFloat(), 0f, pointCount.toFloat(), 0f, PI.toFloat() * 2)
            points.add((Vec2.fromAngle(angle) * r) + Vec2(x.toFloat(), y.toFloat()))
        }
        val poly = Polygon(
            points.map { it.x.roundToInt() }.toIntArray(),
            points.map { it.y.roundToInt() }.toIntArray(),
            pointCount,
        )


        imgGraphics.color = col
        imgGraphics.stroke = BasicStroke(2f)
        imgGraphics.drawPolygon(poly)
    }

    fun render() {
        frame.graphics.drawImage(bufferedImg, 0, 0, null)
        resetBufferedImg()
        frameCount += 1
    }

    private fun resetBufferedImg() {
        bufferedImg = BufferedImage(res.x, res.y, BufferedImage.TYPE_INT_RGB)
        imgGraphics = bufferedImg.createGraphics()
    }

}