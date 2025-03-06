package com.sloimay

import com.sloimay.smath.matrices.Mat4
import com.sloimay.smath.vectors.Quat
import com.sloimay.smath.vectors.Vec3

class Camera(
    var pos: Vec3 = Vec3.ZERO,
    var yaw: Float = 0f,
    var pitch: Float = 0f,

    var fovY: Float,
    var aspect: Float,
    var far: Float,
    var near: Float,
) {

    private fun computeRotQuat(): Quat {
        var out = Quat.IDENTITY
        out = out.mult(Quat.fromAxisAngle(Vec3.Y, -yaw))
        out = out.mult(Quat.fromAxisAngle(Vec3.X, -pitch))
        return out.normalize()
    }

    fun worldToViewMatrix(): Mat4 {
        val translation = Mat4.fromTranslation(pos)
        val rot = Mat4.fromQuat(this.computeRotQuat())
        val transformation = translation.mul(rot)
        return transformation.inverse()
    }

}