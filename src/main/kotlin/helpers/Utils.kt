package com.sloimay.helpers

import com.sloimay.smath.abs
import com.sloimay.smath.vectors.IVec3
import com.sloimay.smath.vectors.Vec2
import com.sloimay.smath.vectors.Vec3
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage


fun IVec3.rgbToCol(): Color {
    val clamped = this.clamp(IVec3.ZERO, IVec3.ONE * 255)
    return Color(clamped.x, clamped.y, clamped.z)
}

fun Vec3.srgbToCol(): Color {
    val clamped = this.clamp(Vec3.ZERO, Vec3.ONE)
    return clamped.times(255f).round().asIVec3().rgbToCol()
}

fun Boolean.toInt() = if (this) 1 else 0


fun deepCopyBufferedImg(bufImg: BufferedImage): BufferedImage {
    val cm = bufImg.colorModel
    val iapm = cm.isAlphaPremultiplied
    val raster = bufImg.copyData(null)
    return BufferedImage(cm, raster, iapm, null)
}


fun sameSide(p1: Vec2, p2: Vec2, a: Vec2, b: Vec2): Boolean {
    val bSubA = b - a
    val cProd1 = bSubA.cross(p1 - a)
    val cProd2 = bSubA.cross(p2 - a)
    return cProd1.dot(cProd2) >= 0f
}

fun pointIn2dTri(p: Vec2, a: Vec2, b: Vec2, c: Vec2): Boolean {
    if (sameSide(p, a, b, c) && sameSide(p, b, a, c) && sameSide(p, c, a, b)) {
        return true
    } else {
        return false
    }
}

fun triInterpWeights(p: Vec3, tri: Array<Vec3>): Array<Float> {
    val f0 = tri[0] - p
    val f1 = tri[1] - p
    val f2 = tri[2] - p

    val triArea = (tri[0] - tri[1]).cross(tri[0] - tri[2]).length()
    val w0 = f1.cross(f2).length() / triArea
    val w1 = f2.cross(f0).length() / triArea
    val w2 = f0.cross(f1).length() / triArea

    return arrayOf(w0, w1, w2)
}




class FloatBuffer2d(val width: Int, val height: Int, init: (Int, Int) -> Float = { x, y -> 0f }) {

    val buffer = FloatArray(width * height)

    init {
        for (y in 0 until height) for (x in 0 until width) {
            buffer[y * width + x] = init(x, y)
        }
    }

    operator fun set(x: Int, y: Int, v: Float) {
        buffer[y * width + x] = v
    }
    operator fun get(x: Int, y: Int): Float {
        return buffer[y * width + x]
    }

    fun fill(el: Float) {
        buffer.fill(el)
    }

}
