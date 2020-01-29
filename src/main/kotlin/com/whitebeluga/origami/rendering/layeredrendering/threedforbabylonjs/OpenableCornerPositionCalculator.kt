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

/*
 * Copyright (c) 2018.  This file is subject to the terms and conditions defined in file 'LICENSE.txt', which is part of this source code package.
 */

package com.whitebeluga.origami.rendering.layeredrendering.threedforbabylonjs

import com.moduleforge.libraries.geometry._3d.Point
import com.moduleforge.libraries.geometry._3d.Vector
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Bundle.Side.BOTTOM
import com.whitebeluga.origami.figure.Bundle.Side.TOP
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Edge
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import com.whitebeluga.origami.loosen.Constants.CORNER_OPEN_UP_ANGLE

internal class OpenableCornerPositionCalculator(private val figure: Figure, private val bundle: Bundle) {
   fun calculateOpenCorners(): Map<Vertex, Point> {
      val topFaces = bundle.topFaces()
      val topOpenCorners = calculateOpenCorners(topFaces, TOP)
      val bottomFaces = bundle.bottomFaces()
      val bottomOpenCorners = calculateOpenCorners(bottomFaces, BOTTOM)
      return topOpenCorners + bottomOpenCorners
   }
   private fun calculateOpenCorners(faces: Set<Face>, side: Bundle.Side): Map<Vertex, Point> {
      val result = mutableMapOf<Vertex, Point>()
      faces.forEach {
         val map = calculateOpenCorners(it, side)
         result.putAll(map)
      }
      return result
   }
   private fun calculateOpenCorners(face: Face, side: Bundle.Side): Map<Vertex, Point> {
      /*
      No point in opening up a triangle.
      Besides a single triangle that is articulated on a single edge will already be opened up.
       */
      if(face.isTriangle())
         return emptyMap()
      val openableCorners = findOpenableCorners(face)
      val normal = if(side == TOP) bundle.upwards else bundle.downwards
      val vertexToVector: Map<Vertex, Vector> =
              openableCorners.map {
                 it to normal.withLength(calculateOpeningDistance(face, it))
              }.toMap()
      return openableCorners.map {
         it to it.translate(vertexToVector[it])
      }.toMap()
   }
   private fun findOpenableCorners(face: Face): Set<Vertex> {
      val edges = face.edgeList
      val edgeListTwice  = edges + edges
      val result = mutableSetOf<Vertex>()
      val minIndex = 1
      val maxIndex = edgeListTwice.size - 3
      for((index, edge) in edgeListTwice.withIndex()) {
         val validIndex = index in minIndex..maxIndex
         if(!validIndex)
            continue
         val corner = isAnOpenableCorner(edgeListTwice, index)
         if(corner != null)
            result.add(corner)
      }
      return result
   }
   private fun isAnOpenableCorner(edges: List<Edge>, index: Int): Vertex? {
      val edge = edges[index]
      val nextEdge = edges[index + 1]
      val areTwoAdjacentFreeEdges = figure.isAFreeEdge(edge) && figure.isAFreeEdge(nextEdge)
      if (areTwoAdjacentFreeEdges) {
         val previous = edges[index - 1]
         val nextOfNext = edges[index + 2]
         val areSurroundedByConnectedEdges = figure.isAConnectingEdge(previous) && figure.isAConnectingEdge(nextOfNext)
         if (areSurroundedByConnectedEdges) {
            val commonVertex = edge.vertices.intersect(nextEdge.vertices).first()
            return commonVertex
         }
      }
      return null
   }
   private fun calculateOpeningDistance(face: Face, vertex: Vertex): Double {
      val index = face.vertices.indexOf(vertex)
      val vertexCount = face.vertices.size
      val halfVertexCount = vertexCount / 2
      val indexOfOppositeVertex = (index + halfVertexCount) % vertexCount
      val distanceBetweenVertexAndOpposite = face.vertices[index].distance(face.vertices[indexOfOppositeVertex])
      //for small angles, the angle value is similar to the sin
      //https://en.wikipedia.org/wiki/Small-angle_approximation
      return distanceBetweenVertexAndOpposite * CORNER_OPEN_UP_ANGLE
   }
}