package com.sloimay.worldentities

import com.sloimay.WorldTriangle
import com.sloimay.smath.vectors.Quat
import com.sloimay.smath.vectors.Vec3

abstract class WorldEntity(var pos: Vec3, var rotation: Quat) {
    abstract fun getTriangles(): List<WorldTriangle>
}