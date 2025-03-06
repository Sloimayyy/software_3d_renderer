package com.sloimay

import com.sloimay.smath.matrices.Mat4
import com.sloimay.smath.vectors.Quat
import com.sloimay.smath.vectors.Vec3

class Player(pos: Vec3, yaw: Float, pitch: Float, var camera: Camera) {

    var pos: Vec3 = pos
        set(value) {
            this.camera.pos = value
            field = value
        }
    var yaw: Float = yaw
        set(value) {
            this.camera.yaw = value
            field = value
        }
    var pitch: Float = pitch
        set(value) {
            this.camera.pitch = value
            field = value
        }


    private fun computeRotQuat(): Quat {
        var out = Quat.IDENTITY
        out = out.mult(Quat.fromAxisAngle(Vec3.Y, -yaw))
        out = out.mult(Quat.fromAxisAngle(Vec3.X, -pitch))
        return out.normalize()
    }

    fun computeForwardVec(): Vec3 {
        return (-Vec3.Z).quatMul(this.computeRotQuat())
    }

    fun computeUpVec(): Vec3 {
        return Vec3.Y
    }

    fun computeRightVec(): Vec3 {
        return computeForwardVec().cross(computeUpVec())
    }



}
