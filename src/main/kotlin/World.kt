package com.sloimay

import com.sloimay.triangles.WorldTriangle
import com.sloimay.worldentities.WorldEntity

class World() {

    val entities = mutableListOf<WorldEntity>()

    fun getTriangles(): List<WorldTriangle> {
        val tris = mutableListOf<WorldTriangle>()
        for (ent in entities) tris.addAll(ent.getTriangles())
        return tris
    }

}