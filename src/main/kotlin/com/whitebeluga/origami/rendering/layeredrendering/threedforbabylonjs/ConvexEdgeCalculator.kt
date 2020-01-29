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
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Edge
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import com.whitebeluga.origami.loosen.Constants.CORNER_OPEN_UP_ANGLE

/**
 * Which edges of which faces ought to be convex?
 *
 * Candidates for convex edges are faces that are fully visible in their bundle (not totally or partially covered)
 * that is, any top or bottom face of a bundle.
 *
 * Of those faces, if there is an edge that is free (not connected to another face) and the edge is surrounded
 * by edges that are not free (connected to other faces), then the edge will be made convex.
 *
 * Theoretically it could find any number of such points for a face. In practice, the fucking shit of babylon js
 * makes it difficult to display any given deformed polygon. It's only trivial when there is a single
 * point that is not coplanar with the rest. So we find a single point even in the event that the face has more than
 * one.
 *
 * Luckily in origami, normally such faces have only one such "convexable" edge.
 */
internal class ConvexEdgeCalculator(private val figure: Figure, private val bundle: Bundle) {
   /**
    * It returns a map such that for every vertex key, if the list of points associated
    * are inserted in the vertices list of the face, it  will make the face convex.
    *
    * The face has to be part of the key, since a vertex can belong to different faces
    *
    * In fact for simplicty the values are just pairs of vertex and point
    */
   fun calculateConvexEdges(): Map<Face, Pair<Vertex, Point>> {
      val topFaces = bundle.topFaces()
      val convexEdgePointsOfTopFaces = calculateConvexEdges(topFaces, Bundle.Side.TOP)
      val bottomFaces = bundle.bottomFaces()
      val convexEdgePointsOfBottomFaces = calculateConvexEdges(bottomFaces, Bundle.Side.BOTTOM)
      return convexEdgePointsOfTopFaces + convexEdgePointsOfBottomFaces
   }
   private fun calculateConvexEdges(faces: Set<Face>, side: Bundle.Side): Map<Face, Pair<Vertex, Point>> {
      val result = mutableMapOf<Face, Pair<Vertex, Point>>()
      faces.forEach {face ->
         val pair = calculateConvexEdges(face, side)
         if(pair != null)
            result[face] = pair
      }
      return result
   }
   private fun calculateConvexEdges(face: Face, side: Bundle.Side): Pair<Vertex, Point>? {
      val convexableEdge = findConvexableEdges(face).firstOrNull() ?: return null //we take just the first
      return calculateConvexEdge(face, convexableEdge, side)
   }
   private fun findConvexableEdges(face: Face): Set<Edge> {
      val edges = face.edgeList
      val edgeListTwice  = edges + edges
      val result = mutableSetOf<Edge>()
      val minIndex = 1
      val maxIndex = edgeListTwice.size - 3
      for((index, _) in edgeListTwice.withIndex()) {
         val validIndex = index in minIndex..maxIndex
         if(!validIndex)
            continue
         val corner = isAConvexableEdge(edgeListTwice, index)
         if(corner != null)
            result.add(corner)
      }
      return result
   }
   private fun isAConvexableEdge(edges: List<Edge>, index: Int): Edge? {
      val edge = edges[index]
      val isAFreeEdge = figure.isAFreeEdge(edge)
      if (isAFreeEdge) {
         val previousEdge = edges[index - 1]
         val nextEdge = edges[index + 1]
         val areTwoAdjacentConnectedEdges = figure.isAConnectingEdge(previousEdge) && figure.isAConnectingEdge(nextEdge)
         if(areTwoAdjacentConnectedEdges)
            return edge
      }
      return null
   }
   /**
    * Instead of a pair of vertex and list  of points, we return a single point (the simplest and most crude way
    * the create a convex vertex: with just three points.
    *
    * The point returned is, of course, the middle point.
    *
    * The vertex returned (first value of the pair) is the first vertex of the edge that appears in the vertex list
    * of the face.
    */
   private fun calculateConvexEdge(face: Face, edge: Edge, side: Bundle.Side): Pair<Vertex, Point> {
      val vertices = face.vertices
      val edgeVertices = edge.vertices
      val firstVertex = edgeVertices.minBy { vertices.indexOf(it) }!!
      val secondVertex = if(edgeVertices.first() == firstVertex) edgeVertices[1] else edgeVertices.first()
      val midPoint = Point.midPoint(firstVertex, secondVertex)
      val openingDistance = firstVertex.distance(secondVertex) * CORNER_OPEN_UP_ANGLE / 4.0
      val normal = if(side == Bundle.Side.TOP) bundle.upwards else bundle.downwards
      val vector = normal.withLength(openingDistance)
      return Pair(firstVertex, midPoint.translate(vector))
   }
}