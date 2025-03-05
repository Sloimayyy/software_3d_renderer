package com.sloimay

import com.sloimay.helpers.pointIn2dTri
import com.sloimay.helpers.triInterpWeights
import com.sloimay.smath.Utils
import com.sloimay.smath.Utils.Companion.remap
import com.sloimay.smath.clamp
import com.sloimay.smath.matrices.Mat4
import com.sloimay.smath.vectors.*
import com.sloimay.smath.vectors.swizzles.xy
import com.sloimay.worldentities.CubeEntity
import java.awt.Color
import java.awt.event.KeyEvent
import kotlin.concurrent.thread
import kotlin.math.*
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

fun main() {

    val window = Window("yippieeee", IVec2(800, 800))
    val targetFps = 60.0
    val targetFrameTime = 1.0 / targetFps
    val targetFrameTimeMillis = targetFrameTime * 1000.0

    val aspect = window.res.x.toFloat() / window.res.y.toFloat()
    val near = 0.1f
    val far = 100.0f
    val fovY = remap(90f, 0f, 180f, 0f, PI.toFloat())

    val playerSpeed = 1f
    val playerSpinSpeed = 2f

    val mainThread = thread(start = false) {

        val timeSource = TimeSource.Monotonic
        var lastFrameTimestamp = timeSource.markNow()

        var lastFpsCheckTimestamp = timeSource.markNow()
        var fpsCheckFrameCount = 0
        val player = Player(
            pos = Vec3(0f, 0f, 0f),
            yaw = 0f,
            pitch = 0f,
        )
        val world = World()



        val cubeEntity = CubeEntity(Vec3(0f, 0f, -2f), Quat.IDENTITY)

        world.entities.add(cubeEntity)

        /*world.triangles.add(
            WorldTriangle(
                arrayOf(
                    WtPoint(Vec3(0f, 0f, -1f), WtPointData(0f, 0f)),
                    WtPoint(Vec3(1f, 0f, -1f), WtPointData(1f, 0f)),
                    WtPoint(Vec3(1f, 1f, -1f), WtPointData(1f, 1f)),
                ),
                cubeTexture,
            )
        )*/

        while (true) {

            // # Start frame logic
            if (lastFrameTimestamp.elapsedNow().toDouble(DurationUnit.SECONDS) < targetFrameTime) {
                continue
            }
            lastFrameTimestamp = timeSource.markNow()
            window.tickKeys()
            // # ============


            val dt = targetFrameTime.toFloat()

            // # Player movement
            val rightVec = player.computeRightVec()
            val upVec = player.computeUpVec()
            val forwardVec = player.computeForwardVec()

            val forwardDir = if (window.isPressed(KeyEvent.VK_Z)) 1 else if (window.isPressed(KeyEvent.VK_S)) -1 else 0
            val rightDir = if (window.isPressed(KeyEvent.VK_D)) 1 else if (window.isPressed(KeyEvent.VK_Q)) -1 else 0
            val upDir = if (window.isPressed(KeyEvent.VK_SPACE)) 1 else if (window.isPressed(KeyEvent.VK_SHIFT)) -1 else 0

            player.pos += forwardVec * playerSpeed * forwardDir.toFloat() * dt
            player.pos += rightVec * playerSpeed * rightDir.toFloat() * dt
            player.pos += upVec * playerSpeed * upDir.toFloat() * dt

            //println(player.pos)

            // # Player rotation
            val yawDir = if (window.isPressed(KeyEvent.VK_RIGHT)) 1 else if (window.isPressed(KeyEvent.VK_LEFT)) -1 else 0
            val pitchDir = if (window.isPressed(KeyEvent.VK_DOWN)) 1 else if (window.isPressed(KeyEvent.VK_UP)) -1 else 0

            player.yaw += playerSpinSpeed * yawDir * dt
            player.yaw = (player.yaw + PI.toFloat()).mod(PI.toFloat() * 2) - PI.toFloat()

            player.pitch += playerSpinSpeed * pitchDir * dt
            player.pitch = player.pitch.clamp(-PI.toFloat() / 2, PI.toFloat() / 2)

            // # Cube movement
            cubeEntity.rotation = Quat.fromAxisAngle(Vec3.Y, window.frameCount.toFloat() / 60f)



            // # Rendering
            for (x in 0 until window.res.x) for (y in 0 until window.res.y) {
                window.setRgb(x, y, Color.BLACK)
            }

            val screenTrisToRender = mutableListOf<RenderTriangle>()

            for (worldTriangle in world.getTriangles()) {

                val screenPoints = mutableListOf<StPoint>()

                // # Project and clip triangle
                for (triPoint in worldTriangle.points) {
                    println("WorldPoint: ${triPoint.pos}")
                    val pointInViewSpace = player.worldToViewMatrix().timesVec4(triPoint.pos.extend(1f))
                    println("ViewPoint: ${pointInViewSpace}")

                    // Perspective projection matrix
                    val viewToClip = Mat4(
                        Vec4(1f / (tan(fovY / 2) * aspect), 0f, 0f, 0f),
                        Vec4(0f, 1f / tan(fovY / 2), 0f, 0f),
                        Vec4(0f, 0f, -(far + near) / (far - near), -1f),
                        Vec4(0f, 0f, -(2 * far * near) / (far - near), 0f)
                    )

                    val pointInClipSpace = viewToClip.timesVec4(pointInViewSpace)

                    val pointInNdc = (pointInClipSpace / pointInClipSpace.w).xy

                    val pointScreenX = remap(pointInNdc.x, -1f, 1f, 0f, window.res.x.toFloat())
                    val pointScreenY = remap(pointInNdc.y, -1f, 1f, window.res.y.toFloat(), 0f)

                    val screenPoint = Vec2(pointScreenX, pointScreenY)

                    /*window.drawCircle(
                        pointScreenX.roundToInt(),
                        pointScreenY.roundToInt(),
                        10f,
                        IVec3(0, 0, 0).rgbToCol()
                    )*/

                    val w = pointInViewSpace.w
                    screenPoints.add(
                        StPoint(screenPoint, StPointData(
                            pointInViewSpace.w,
                            triPoint.data.u,
                            triPoint.data.v,
                            triPoint.data.u / w,
                            triPoint.data.v / w,
                            1f / w,
                        ))
                    )
                }

                val renderTri = RenderTriangle(
                    arrayOf(screenPoints[0], screenPoints[1], screenPoints[2]),
                    worldTriangle.texture,
                )

                screenTrisToRender.add(renderTri)
            }

            // # Render triangles
            for (renderTri in screenTrisToRender) {
                val (p1, p2, p3) = renderTri.points

                val triPointVec3Array = arrayOf(
                    p1.pos.extend(p1.data.depth),
                    p2.pos.extend(p2.data.depth),
                    p3.pos.extend(p3.data.depth)
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

                    window.setRgb(sx, sy, pixCol)
                }
            }



            // # End frame logic
            window.render()
            // Fps checking logic
            fpsCheckFrameCount += 1
            if (lastFpsCheckTimestamp.elapsedNow().toDouble(DurationUnit.SECONDS) >= 1.0) {
                lastFpsCheckTimestamp = timeSource.markNow()

                val computedFps = fpsCheckFrameCount.toDouble()
                window.frame.title = computedFps.toString()

                fpsCheckFrameCount = 0
            }
            // # ===============
        }
    }

    mainThread.start()



}