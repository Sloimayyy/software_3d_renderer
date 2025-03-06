package com.sloimay.triangles

import com.sloimay.Texture
import com.sloimay.smath.vectors.Vec2

data class RtPointData(
    val depth: Float,
    val uOverW: Float,
    val vOverW: Float,
    val oneOverW: Float
)
data class RtPoint(val pos: Vec2, val data: RtPointData)

class RenderTriangle(
    val points: Array<RtPoint>,
    val texture: Texture,
) {

    init {
        if (points.size != 3) {
            throw Exception()
        }
    }

}