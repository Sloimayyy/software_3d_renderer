package com.sloimay

import com.sloimay.smath.Utils.Companion.remap
import com.sloimay.smath.clamp
import com.sloimay.smath.floor
import com.sloimay.smath.vectors.*
import com.sloimay.worldentities.CubeEntity
import java.awt.Color
import java.awt.event.KeyEvent
import kotlin.concurrent.thread
import kotlin.math.*
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

fun main() {

    val window = Window("yippieeee", IVec2(800* 16 / 9, 800))
    val targetFps = 60.0
    val targetFrameTime = 1.0 / targetFps
    val targetFrameTimeMillis = targetFrameTime * 1000.0

    val renderer = Renderer(window)

    val aspect = window.res.x.toFloat() / window.res.y.toFloat()
    val near = 0.01f
    val far = 100.0f
    val fovY = remap(70f, 0f, 180f, 0f, PI.toFloat())

    val playerSpeed = 1f
    val playerSpinSpeed = 2f

    val mainThread = thread(start = false) {

        val timeSource = TimeSource.Monotonic
        var lastFrameTimestamp = timeSource.markNow()

        var lastFpsCheckTimestamp = timeSource.markNow()
        var fpsCheckFrameCount = 0
        val player = Player(
            pos = Vec3(-4f, 18f, -4f),
            yaw = PI.toFloat() - PI.toFloat() / 4f,
            pitch = PI.toFloat() / 5f,
            camera = Camera(
                fovY = fovY,
                aspect = aspect,
                far = far,
                near = near,
            )
        )
        val world = World()


        world.blocks[0, 0, 2] = Block(ANDESITE_TEXTURE, true, "andesite")
        world.blocks[0, 0, 3] = Block(ANDESITE_TEXTURE, true, "andesite")
        world.blocks[0, 0, 4] = Block(ANDESITE_TEXTURE, true, "andesite")
        world.blocks[0, 1, 3] = Block(ANDESITE_TEXTURE, true, "andesite")
        world.blocks[0, 2, 3] = Block(ANDESITE_TEXTURE, true, "andesite")

        /*
        for (x in 0 until world.worldSize.x) for (z in 0 until world.worldSize.z) {
            val y = (sin(sqrt(x.toFloat().pow(2f) + z.toFloat().pow(2f)) * 0.4f) * 3f + 5f).floor().toInt()
            world.blocks[x, y, z] = Block(GRASS_BLOCK_TEXTURE, true, "grass_block")
            for (bY in 0 until y) {
                if (abs(y - bY) > 2) {
                    world.blocks[x, bY, z] = Block(STONE_TEXTURE, true, "stone")
                } else {
                    world.blocks[x, bY, z] = Block(DIRT_TEXTURE, true, "dirt")
                }
            }
        }
         */


        val cubeEntity = CubeEntity(Vec3(0f, 0f, -2f), Quat.IDENTITY)

        //world.entities.add(cubeEntity)

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
            //cubeEntity.rotation = Quat.fromAxisAngle(Vec3.Y, window.frameCount.toFloat() / 60f)



            // # Rendering
            for (x in 0 until window.res.x) for (y in 0 until window.res.y) {
                window.setRgb(x, y, Color.BLACK)
            }

            for (worldTriangle in world.getTriangles()) {
                renderer.renderWorldTriangle(player.camera, worldTriangle)
            }


            // # End frame logic
            window.flip()
            renderer.resetZBuffer()
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