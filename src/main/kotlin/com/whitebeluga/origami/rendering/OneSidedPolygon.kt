/*
 *    This file is part of "Origami".
 *
 *     Origami is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Origami is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Origami.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.whitebeluga.origami.rendering

import com.moduleforge.libraries.geometry._3d.Point
import com.moduleforge.libraries.geometry._3d.Polygon
import com.whitebeluga.origami.figure.component.Crease
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.loosen.DeformableFace
import java.awt.Color

/**
 The order of the boundary of the polygon determines the direction of the front face
 Dummy class used for the GUI module, only the point list and the color are important, no operations.

 Creases should only belong to planar -ie not deformed- polygons
 */
class OneSidedPolygon(val boundary: List<Point>, val color: Color,
                      /**
                       * The creases are a set of pair of points. Here represented as lists with the purposes of
                       * providing simple data structures that will be later processed by the front end.
                       */
                      val creases: Set<List<Point>> = emptySet()) {
   //fun reversed() = OneSidedPolygon(boundary.reversed(), color)
   companion object {
      /**
       * The first of the pair is the front, the second is the back.
       *
       * For a deformable face there can't be creases.
       */
      fun from(face: DeformableFace): Pair<OneSidedPolygon, OneSidedPolygon> {
         val frontFace = OneSidedPolygon(face.vertices, face.frontColor)
         val backFace = OneSidedPolygon(face.vertices.asReversed(), face.backColor)
         return Pair(frontFace, backFace)
      }
      fun makeFrontFaceWithCreasesFromFace(face: Face): OneSidedPolygon = makeFrontFaceFrom(face, face.creases)
      fun makeBackFaceWithCreasesFromFace(face: Face): OneSidedPolygon = makeBackFaceFrom(face, face.creases)
      fun makeFrontFaceNoCreasesFromFace(face: Face): OneSidedPolygon = makeFrontFaceFrom(face)
      fun makeBackFaceNoCreasesFromFace(face: Face): OneSidedPolygon = makeBackFaceFrom(face)
      internal fun makeFrontFaceFrom(p: CreasedPolygon): OneSidedPolygon =
              makeFrontFaceFrom(p, p.creases)
      fun makeFrontFaceFrom(p: Polygon, creases: Set<Crease> = emptySet()): OneSidedPolygon {
         val creasePoints = creases.map { it.pointAndVertexList }.toSet()
         return OneSidedPolygon(p.vertices, p.frontColor, creasePoints)
      }
      internal fun makeBackFaceFrom(p: CreasedPolygon): OneSidedPolygon =
         makeBackFaceFrom(p, p.creases)
      fun makeBackFaceFrom(p: Polygon, creases: Set<Crease> = emptySet()): OneSidedPolygon {
         val creasePoints = creases.map { it.pointAndVertexList }.toSet()
         return OneSidedPolygon(p.vertices.asReversed(), p.backColor, creasePoints)
      }
   }
}
