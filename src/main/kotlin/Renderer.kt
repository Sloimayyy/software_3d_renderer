package com.sloimay

import com.sloimay.helpers.FloatBuffer2d
import com.sloimay.helpers.pointIn2dTri
import com.sloimay.helpers.toInt
import com.sloimay.helpers.triInterpWeights
import com.sloimay.smath.Utils.Companion.remap
import com.sloimay.smath.clamp
import com.sloimay.smath.lerp
import com.sloimay.smath.matrices.Mat4
import com.sloimay.smath.vectors.Vec2
import com.sloimay.smath.vectors.Vec3
import com.sloimay.smath.vectors.Vec4
import com.sloimay.smath.vectors.swizzles.xy
import com.sloimay.triangles.*
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.tan

class Renderer(val window: Window) {


    val zBuffer = FloatBuffer2d(window.res.x, window.res.y) { x, y -> Float.POSITIVE_INFINITY }


    fun renderWorldTriangle(camera: Camera, worldTri: WorldTriangle) {

        // Perspective projection matrix (View to Perspective before perspective divide)
        val viewToClip = generateViewToClipMatrix(camera)

        // # Clip the triangles
        var clippedTriangles = mutableListOf(projectToClipSpace(worldTri, camera, viewToClip))

        clippedTriangles = clipTriangles(clippedTriangles)


        // # Convert clipped triangles into screen triangles
        val screenTrisToRender = clippedTrisToScreenTris(clippedTriangles)

        // # Render triangles
        rasterizeScreenTris(screenTrisToRender)

    }

    private fun clipTriangles(clippedTriangles: MutableList<ClippedTriangle>): MutableList<ClippedTriangle> {
        var clippedTriangles1 = clippedTriangles

        for (plane in ClippingPlane.entries) {
            val nextClippedTriangles = mutableListOf<ClippedTriangle>()

            for (clippedTri in clippedTriangles1) {

                val insideArray = clippedTri.points
                    .map { isPointInsideClippingPlane(it.pos, plane) }
                    .toBooleanArray()

                val insideCount = insideArray.sumOf { it.toInt() }

                when (insideCount) {
                    3 -> { // All vertices are inside the view frustum
                        nextClippedTriangles.add(clippedTri)
                    }

                    0 -> { // No vertex is inside the view frustum

                    }

                    1 -> { // One vertex is inside, two are outside
                        // Find which vertex is inside
                        val insideIndex = insideArray.indexOf(true)
                        val outside1 = (insideIndex + 1) % 3
                        val outside2 = (insideIndex + 2) % 3

                        val insidePoint = clippedTri.points[insideIndex]
                        val outside1Point = clippedTri.points[outside1]
                        val outside2Point = clippedTri.points[outside2]

                        val newVert1 = insidePoint
                        val newVert2 = interpolateCtPoints(
                            insidePoint,
                            outside1Point,
                            getT(insidePoint, outside1Point, plane)
                        )
                        val newVert3 = interpolateCtPoints(
                            insidePoint,
                            outside2Point,
                            getT(insidePoint, outside2Point, plane)
                        )

                        println("==============")
                        println(newVert1)
                        println(newVert2)
                        println(newVert3)

                        nextClippedTriangles.add(
                            ClippedTriangle(
                                arrayOf(newVert1, newVert2, newVert3),
                                clippedTri.texture
                            )
                        )
                    }

                    2 -> { // Two vertices inside, one outside
                        // Find which vertex is outside
                        val outsideIndex = insideArray.indexOf(false)
                        val inside1 = (outsideIndex + 1) % 3
                        val inside2 = (outsideIndex + 2) % 3

                        val outsidePoint = clippedTri.points[outsideIndex]
                        val inside1Point = clippedTri.points[inside1]
                        val inside2Point = clippedTri.points[inside2]

                        // Get the two new vertices at the intersection of the plane
                        val newVertex1 = interpolateCtPoints(
                            inside1Point, outsidePoint, getT(inside1Point, outsidePoint, plane)
                        )
                        val newVertex2 = interpolateCtPoints(
                            inside2Point, outsidePoint, getT(inside2Point, outsidePoint, plane)
                        )

                        // We get a quad after clipping, split into two triangles
                        nextClippedTriangles.add(
                            ClippedTriangle(
                                arrayOf(inside1Point, newVertex1, newVertex2),
                                clippedTri.texture,
                            )
                        )
                        nextClippedTriangles.add(
                            ClippedTriangle(
                                arrayOf(inside1Point, newVertex2, inside2Point),
                                clippedTri.texture,
                            )
                        )
                    }

                    else -> {}
                }

            }

            clippedTriangles1 = nextClippedTriangles
        }
        return clippedTriangles1
    }

    private fun generateViewToClipMatrix(camera: Camera): Mat4 {
        val viewToClip = Mat4(
            Vec4(1f / (tan(camera.fovY / 2) * camera.aspect), 0f, 0f, 0f),
            Vec4(0f, 1f / tan(camera.fovY / 2), 0f, 0f),
            Vec4(0f, 0f, -(camera.far + camera.near) / (camera.far - camera.near), -1f),
            Vec4(0f, 0f, -(2 * camera.far * camera.near) / (camera.far - camera.near), 0f)
        )
        return viewToClip
    }


    private fun rasterizeScreenTris(screenTrisToRender: List<RenderTriangle>) {
        for (renderTri in screenTrisToRender) {

            val (p1, p2, p3) = renderTri.points


            /*val lines = arrayOf(p1.pos to p2.pos, p2.pos to p3.pos, p3.pos to p1.pos)
            for (line in lines) {
                window.drawLine(line.first.floor().asIVec2(), line.second.floor().asIVec2(), 4f, Color.PINK)
            }

            continue*/

            val triPointVec3Array = arrayOf(
                p1.pos.extend(0f),
                p2.pos.extend(0f),
                p3.pos.extend(0f),
            )

            val minX = min(min(p1.pos.x.roundToInt(), p2.pos.x.roundToInt()), p3.pos.x.roundToInt())
                .clamp(0, window.frame.width)
            val maxX = max(max(p1.pos.x.roundToInt(), p2.pos.x.roundToInt()), p3.pos.x.roundToInt())
                .clamp(0, window.frame.width)

            val minY = min(min(p1.pos.y.roundToInt(), p2.pos.y.roundToInt()), p3.pos.y.roundToInt())
                .clamp(0, window.frame.height)
            val maxY = max(max(p1.pos.y.roundToInt(), p2.pos.y.roundToInt()), p3.pos.y.roundToInt())
                .clamp(0, window.frame.height)

            for (sx in minX..maxX) for (sy in minY..maxY) {
                val screenPoint = Vec2(sx.toFloat() + 0.5f, sy.toFloat() + 0.5f)
                if (!pointIn2dTri(screenPoint, p1.pos, p2.pos, p3.pos)) continue

                // 2d triangle interpolation (using 3d math lmao)
                val weights = triInterpWeights(
                    Vec3(screenPoint.x, screenPoint.y, 0f),
                    triPointVec3Array
                )

                val uOverW = (
                        weights[0] * p1.data.uOverW +
                                weights[1] * p2.data.uOverW +
                                weights[2] * p3.data.uOverW
                        )
                val vOverW = (
                        weights[0] * p1.data.vOverW +
                                weights[1] * p2.data.vOverW +
                                weights[2] * p3.data.vOverW
                        )
                val oneOverW = (
                        weights[0] * p1.data.oneOverW +
                                weights[1] * p2.data.oneOverW +
                                weights[2] * p3.data.oneOverW
                        )

                val u = uOverW / oneOverW
                val v = vOverW / oneOverW

                val pixCol = renderTri.texture.pollPixel(u, v)

                if (pixCoordsOnScreen(sx, sy)) {
                    val screenDepth = zBuffer[sx, sy]
                    val pixelDepth = 1f / oneOverW
                    if (pixelDepth < screenDepth) {
                        window.setRgb(sx, sy, pixCol)
                        //window.setRgb(sx, sy, Color(u.clamp(0f, 1f), v.clamp(0f, 1f), 0f))
                        zBuffer[sx, sy] = pixelDepth
                    }
                }
            }
        }
    }

    private fun clippedTrisToScreenTris(clippedTriangles: MutableList<ClippedTriangle>) =
        clippedTriangles.map { clippedTri ->
            val points = clippedTri.points.map { ctPoint ->
                val pointInClipSpace = ctPoint.pos
                val pointInNdc = (pointInClipSpace / pointInClipSpace.w).xy
                val pointScreenX = remap(pointInNdc.x, -1f, 1f, 0f, window.res.x.toFloat())
                val pointScreenY = remap(pointInNdc.y, -1f, 1f, window.res.y.toFloat(), 0f)
                val screenPoint = Vec2(pointScreenX, pointScreenY)
                val w = ctPoint.data.w
                RtPoint(
                    screenPoint,
                    RtPointData(
                        ctPoint.data.depth,
                        ctPoint.data.u / w,
                        ctPoint.data.v / w,
                        1f / w,
                    )
                )
            }
            RenderTriangle(
                points.toTypedArray(),
                clippedTri.texture,
            )
        }

    private fun projectToClipSpace(
        worldTri: WorldTriangle,
        camera: Camera,
        viewToClip: Mat4
    ): ClippedTriangle {
        val firstClipTrianglePoints = worldTri.points.map { triPoint ->
            //println("WorldPoint: ${triPoint.pos}")
            val pointInViewSpace = camera.worldToViewMatrix().timesVec4(triPoint.pos.extend(1f))
            //println("ViewPoint: ${pointInViewSpace}")

            val pointInClipSpace = viewToClip.timesVec4(pointInViewSpace)

            val w = pointInClipSpace.w
            CtPoint(
                pointInClipSpace,
                CtPointData(
                    pointInClipSpace.w,
                    triPoint.data.u,
                    triPoint.data.v,
                    w,
                )
            )
        }

        return ClippedTriangle(
            firstClipTrianglePoints.toTypedArray(),
            worldTri.texture,
        )
    }


    private fun getT(insidePoint: CtPoint, outsidePoint: CtPoint, plane: ClippingPlane): Float {
        /** TODO:
         *      try to make it branchless instead
         */
        val (inside, outside) = insidePoint.pos to outsidePoint.pos
        val t = when (plane) {
            ClippingPlane.LEFT -> { // Left plane (x = -w)
                (inside.x + inside.w) / ((inside.x + inside.w) - (outside.x + outside.w))
            }
            ClippingPlane.RIGHT -> { // Right plane (x = w)
                (inside.w - inside.x) / ((inside.w - inside.x) + (outside.x - outside.w))
            }
            ClippingPlane.BOTTOM -> { // Bottom plane (y = -w)
                (inside.y + inside.w) / ((inside.y + inside.w) - (outside.y + outside.w))
            }
            ClippingPlane.TOP -> { // Top plane (y = w)
                (inside.w - inside.y) / ((inside.w - inside.y) + (outside.y - outside.w))
            }
            ClippingPlane.NEAR -> { // Near plane (z = -w)
                (inside.z + inside.w) / ((inside.z + inside.w) - (outside.z + outside.w))
            }
            ClippingPlane.FAR -> { // Far plane (z = w)
                (inside.w - inside.z) / ((inside.w - inside.z) + (outside.z - outside.w))
            }
        }
        return  t
    }


    private fun isPointInsideClippingPlane(point: Vec4, plane: ClippingPlane, delta: Float = 0.001f): Boolean {
        return when (plane) {
            ClippingPlane.LEFT -> point.x + delta >= -point.w
            ClippingPlane.RIGHT -> point.x - delta <= point.w
            ClippingPlane.BOTTOM -> point.y + delta >= -point.w
            ClippingPlane.TOP -> point.y - delta <= point.w
            ClippingPlane.NEAR -> point.z + delta >= -point.w
            ClippingPlane.FAR -> point.z - delta <= point.w
        }
    }


    private fun interpolateCtPoints(a: CtPoint, b: CtPoint, t: Float): CtPoint {
        val newPos = a.pos.lerp(b.pos, t)
        val newData = CtPointData(
            depth = a.data.depth.lerp(b.data.depth, t),
            u = a.data.u.lerp(b.data.u, t),
            v = a.data.v.lerp(b.data.v, t),
            w = a.data.w.lerp(b.data.w, t),
        )
        return CtPoint(newPos, newData)
    }


    private fun pixCoordsOnScreen(x: Int, y: Int): Boolean {
        return x >= 0 && x < window.res.x && y >= 0 && y < window.res.y
    }


    fun resetZBuffer() {
        zBuffer.fill(Float.POSITIVE_INFINITY)
    }
}