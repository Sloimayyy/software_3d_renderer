package com.sloimay

import com.sloimay.smath.vectors.Vec2
import com.sloimay.smath.vectors.Vec3

data class StPointData(
    val depth: Float,
    val u: Float,
    val v: Float,
    val uOverW: Float,
    val vOverW: Float,
    val oneOverW: Float
)
data class StPoint(val pos: Vec2, val data: StPointData)

class RenderTriangle(
    val points: Array<StPoint>,
    val texture: Texture,
) {

    init {
        if (points.size != 3) {
            throw Exception()
        }
    }

}