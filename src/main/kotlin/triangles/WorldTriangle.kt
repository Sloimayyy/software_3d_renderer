package com.sloimay.triangles

import com.sloimay.Texture
import com.sloimay.smath.vectors.Vec3


data class WtPointData(val u: Float, val v: Float)
data class WtPoint(val pos: Vec3, val data: WtPointData)


class WorldTriangle(
    val points: Array<WtPoint>,
    val texture: Texture,
) {

    init {
        if (points.size != 3) {
            throw Exception()
        }
    }

}