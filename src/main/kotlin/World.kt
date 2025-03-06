package com.sloimay

import com.sloimay.helpers.Grid
import com.sloimay.smath.vectors.IVec3
import com.sloimay.smath.vectors.Vec3
import com.sloimay.triangles.WorldTriangle
import com.sloimay.triangles.WtPoint
import com.sloimay.triangles.WtPointData
import com.sloimay.worldentities.WorldEntity

class World() {

    val worldSize = IVec3(32, 16, 32)

    val blocks = Grid(worldSize) { x, y, z -> Block(ANDESITE_TEXTURE, false, "air") }

    val entities = mutableListOf<WorldEntity>()

    fun getTriangles(): List<WorldTriangle> {
        /*val tris = mutableListOf<WorldTriangle>()
        for (ent in entities) tris.addAll(ent.getTriangles())
        return tris*/

        val allTris = mutableListOf<WorldTriangle>()

        for (x in 0 until worldSize.x) for (y in 0 until worldSize.y) for (z in 0 until worldSize.z) {
            val block = blocks[x, y, z]

            val blockPos = Vec3(x.toFloat(), y.toFloat(), z.toFloat())

            if (block.name != "air") {

                val vertices = mutableListOf<Vec3>()
                for (i in -1..1 step 2) for (j in -1..1 step 2) for (k in -1..1 step 2) {
                    val lx = i.toFloat()
                    val ly = j.toFloat()
                    val lz = k.toFloat()
                    vertices.add(((Vec3(lx, ly, lz) / 2f) + 0.5f) + blockPos)
                }

                val mesh = listOf(
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[0], WtPointData(0f, 0f)),
                            WtPoint(vertices[1], WtPointData(1f, 0f)),
                            WtPoint(vertices[2], WtPointData(0f, 1f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[2], WtPointData(0f, 1f)),
                            WtPoint(vertices[3], WtPointData(1f, 1f)),
                            WtPoint(vertices[1], WtPointData(1f, 0f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[1], WtPointData(0f, 0f)),
                            WtPoint(vertices[5], WtPointData(1f, 0f)),
                            WtPoint(vertices[3], WtPointData(0f, 1f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[5], WtPointData(0f, 0f)),
                            WtPoint(vertices[4], WtPointData(1f, 0f)),
                            WtPoint(vertices[7], WtPointData(0f, 1f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[4], WtPointData(0f, 0f)),
                            WtPoint(vertices[0], WtPointData(1f, 0f)),
                            WtPoint(vertices[6], WtPointData(0f, 1f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[3], WtPointData(0f, 1f)),
                            WtPoint(vertices[7], WtPointData(1f, 1f)),
                            WtPoint(vertices[5], WtPointData(1f, 0f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[7], WtPointData(0f, 1f)),
                            WtPoint(vertices[6], WtPointData(1f, 1f)),
                            WtPoint(vertices[4], WtPointData(1f, 0f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[6], WtPointData(0f, 1f)),
                            WtPoint(vertices[2], WtPointData(1f, 1f)),
                            WtPoint(vertices[0], WtPointData(1f, 0f)),
                        ),
                        block.texture,
                    )

                    ,
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[3], WtPointData(0f, 1f)),
                            WtPoint(vertices[7], WtPointData(0f, 0f)),
                            WtPoint(vertices[2], WtPointData(1f, 1f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[6], WtPointData(1f, 0f)),
                            WtPoint(vertices[7], WtPointData(0f, 0f)),
                            WtPoint(vertices[2], WtPointData(1f, 1f)),
                        ),
                        block.texture,
                    )

                    ,
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[1], WtPointData(0f, 1f)),
                            WtPoint(vertices[5], WtPointData(0f, 0f)),
                            WtPoint(vertices[0], WtPointData(1f, 1f)),
                        ),
                        block.texture,
                    ),
                    WorldTriangle(
                        arrayOf(
                            WtPoint(vertices[4], WtPointData(1f, 0f)),
                            WtPoint(vertices[5], WtPointData(0f, 0f)),
                            WtPoint(vertices[0], WtPointData(1f, 1f)),
                        ),
                        block.texture,
                    )
                )

                allTris.addAll(mesh)
            }
        }

        return allTris

    }

}