package com.sloimay.triangles

import com.sloimay.Texture
import com.sloimay.smath.vectors.Vec2
import com.sloimay.smath.vectors.Vec4

data class CtPointData(
    val depth: Float,
    val u: Float,
    val v: Float,
    val w: Float
)
// Homogeneous coords pos
data class CtPoint(val pos: Vec4, val data: CtPointData)

class ClippedTriangle(
    val points: Array<CtPoint>,
    val texture: Texture,
) {

    init {
        if (points.size != 3) {
            throw Exception()
        }
    }

}