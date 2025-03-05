package com.sloimay

import com.sloimay.smath.matrices.Mat4
import com.sloimay.smath.vectors.Quat
import com.sloimay.smath.vectors.Vec3

class Player(var pos: Vec3, var yaw: Float, var pitch: Float) {


    fun worldToViewMatrix(): Mat4 {
        val translation = Mat4.fromTranslation(pos)
        val rot = Mat4.fromQuat(this.computeRotQuat())
        val transformation = translation.mul(rot)
        return transformation.inverse()
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


    private fun computeRotQuat(): Quat {
        var out = Quat.IDENTITY
        out = out.mult(Quat.fromAxisAngle(Vec3.Y, -this.yaw))
        out = out.mult(Quat.fromAxisAngle(Vec3.X, -this.pitch))
        return out.normalize()
    }
}
