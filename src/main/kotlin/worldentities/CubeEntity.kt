package com.sloimay.worldentities

import com.sloimay.CUBE_TEXTURE
import com.sloimay.WorldTriangle
import com.sloimay.WtPoint
import com.sloimay.WtPointData
import com.sloimay.smath.vectors.Quat
import com.sloimay.smath.vectors.Vec3

class CubeEntity(pos: Vec3, rotation: Quat) : WorldEntity(pos, rotation) {

    // Model space coordinates
    val tris = run {
        val vertices = mutableListOf<Vec3>()

        for (i in -1..1 step 2) for (j in -1..1 step 2) for (k in -1..1 step 2) {
            val x = i.toFloat()
            val y = j.toFloat()
            val z = k.toFloat()
            vertices.add(Vec3(x, y, z) / 2f)
        }


        listOf(
            WorldTriangle(
                arrayOf(
                    WtPoint(vertices[0], WtPointData(0f, 0f)),
                    WtPoint(vertices[1], WtPointData(1f, 0f)),
                    WtPoint(vertices[2], WtPointData(0f, 1f)),
                ),
                CUBE_TEXTURE,
            ),
            WorldTriangle(
                arrayOf(
                    WtPoint(vertices[1], WtPointData(0f, 0f)),
                    WtPoint(vertices[5], WtPointData(1f, 0f)),
                    WtPoint(vertices[3], WtPointData(0f, 1f)),
                ),
                CUBE_TEXTURE,
            ),
            WorldTriangle(
                arrayOf(
                    WtPoint(vertices[5], WtPointData(0f, 0f)),
                    WtPoint(vertices[4], WtPointData(1f, 0f)),
                    WtPoint(vertices[7], WtPointData(0f, 1f)),
                ),
                CUBE_TEXTURE,
            )
            ,
            WorldTriangle(
                arrayOf(
                    WtPoint(vertices[4], WtPointData(0f, 0f)),
                    WtPoint(vertices[0], WtPointData(1f, 0f)),
                    WtPoint(vertices[6], WtPointData(0f, 1f)),
                ),
                CUBE_TEXTURE,
            )

        )
    }

    override fun getTriangles(): List<WorldTriangle> {

        val movePoint = fun(p: Vec3): Vec3 {
            var pMoved = p
            pMoved = pMoved.quatMul(this.rotation)
            pMoved += pos
            return pMoved
        }

        val worldTris = tris.map { modelTri ->
            val (p1, p2, p3) = modelTri.points

            WorldTriangle(
                arrayOf(
                    WtPoint(movePoint(p1.pos), p1.data),
                    WtPoint(movePoint(p2.pos), p2.data),
                    WtPoint(movePoint(p3.pos), p3.data),
                ),
                modelTri.texture,
            )
        }

        return worldTris
    }
}