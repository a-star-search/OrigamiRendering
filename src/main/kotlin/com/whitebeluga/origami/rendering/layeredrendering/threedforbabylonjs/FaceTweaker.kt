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

import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import com.whitebeluga.origami.rendering.OneSidedPolygon
import com.whitebeluga.origami.rendering.layeredrendering.SideToRender

/**
 * Tweaks in a face are one or more vertices whose position has been slightly shifted to give the figure a more realistic
 * 3D look.
 *
 * Examples of tweaks are corners pulled outwards or connected edges whose free ends are slightly pulled apart and so on.
 *
 * In general, the result of these vertex replacements is a deformed face (ie not flat). That is important depending
 * on the choice of technology used in the GUI. Not very many libraries can straight out render deformed faces.
 *
 * A second kind of tweak is including one or more vertices between two vertices. This allows to give the face a
 * convex shape.
 *
 * The one sided polygons output of this algorithm never include the creases.
 * 3-d rendered figures are created without creases in this module.
 *
 * IMPORTANT
 * Limitations of this class:
 *
 * In theory it should be possible to have any number of tweaks for three dimensionality in any given face.
 *
 * In practice most graphical libraries will have some trouble displaying deformed faces.
 *
 * In particular the fucking piece of shit babylonjs whose developers surely suffer from retardation, makes it very
 * difficult (although, to be fair, not impossible) to render any arbitrary deformed face.
 *
 * What it is easier in babylon though, is displaying a face where, at most, one point is not co-planar.
 * The way the vertices are taken to make a face with babylon in the current GUI module (The way it works now in the
 * GUI module is using two paths where the first path starts with the first half of the vertices) as long as the
 * non-coplanar vertex is the second on the list returned, then the face will be correctly rendered.
 *
 * In short, this class returns a face where, AT MOST, one of the vertices will not be coplanar.
 *
 * The question arises as if will it be a little unnatural looking and inconsistent to pull only one vertex, when
 * multiple vertices ought to be pulled.
 *
 * I think it is better than the alternative: no vertices being pulled. I also count on the number of vertices to be
 * pulled for any face to be none or one in most cases.
 */
object FaceTweaker {
   fun makePolygon(face: Face,
                   sideToRender: SideToRender,
                   vertexToTweakedPosition: Map<Vertex, Point>,
                   /**
                    * we make a convex edge with single point in the middle (a list of points would be smoother)
                    */
                   vertexToNextVertexInConvexEdge: Pair<Vertex, Point>?,
                   bundlePlane: Plane): OneSidedPolygon {
      val polygonAndBundleFaceTheSameDirection = face.polygonPlane.approximatelyFacingTheSameWay(bundlePlane)
      val openCorners = makeVerticesWithOpenCorners(face.vertices, vertexToTweakedPosition)
      val andConvexEdges = if(vertexToNextVertexInConvexEdge == null)
            openCorners
         else
            makeVerticesOfFaceWithConvexEdge(openCorners, vertexToNextVertexInConvexEdge)
      //true if front, false if back
      val isTheFrontFace =
              (polygonAndBundleFaceTheSameDirection && sideToRender == SideToRender.TOP) ||
              (!polygonAndBundleFaceTheSameDirection && sideToRender == SideToRender.BOTTOM)
      val verticesOfSideOfPolygon = if(isTheFrontFace)
            andConvexEdges
         else {
            //keep the second as the second also in the reversed list, this is for correct display of convex edges in babylon
            val reversed = andConvexEdges.reversed()
            //the second in the four-edged triangle, will be, presumably, the third when reversed
            //so drop the first and append it
            reversed.drop(1) + reversed.first()
         }
      val colorOfSideOfPolygon = if(isTheFrontFace) face.frontColor else face.backColor
      return OneSidedPolygon(verticesOfSideOfPolygon, colorOfSideOfPolygon)
   }
   private fun makeVerticesWithOpenCorners(vertices: List<Vertex>, vertexToTweakedPosition: Map<Vertex, Point>): List<Vertex> =
      if (vertexToTweakedPosition.isEmpty()) {
         vertices
      } else {
         //take an arbitrary one, the first is good.
         val first = vertexToTweakedPosition.entries.firstOrNull { vertices.contains(it.key) }
         if(first == null)
            vertices
         else
            makeVerticesWithOneOpenCorner(vertices, first.key, first.value)
      }
   private fun makeVerticesWithOneOpenCorner(vertices: List<Vertex>, vertexToBeReplaced: Vertex, newPosition: Point): List<Vertex> {
      val index = vertices.indexOf(vertexToBeReplaced)
      val newVertex = Vertex(newPosition)
      val newVertices = vertices.map { if(it == vertexToBeReplaced) newVertex else it }
      //now reorganize the vertices to ensure the replaced is in the second position
      if(index == 1)
         return newVertices
      if(index == 0)
         return listOf(newVertices.last()) + newVertices.dropLast(1)
      val newTail = vertices.take(index - 1)
      return vertices.drop(index - 1) + newTail
   }
   private fun makeVerticesOfFaceWithConvexEdge(vertices: List<Vertex>, vertexToNextPositionInConvexEdge: Pair<Vertex, Point>):
           List<Vertex> {
      val index = vertices.indexOf(vertexToNextPositionInConvexEdge.first)
      val found = index >= 0
      return if(found)
            makeVerticesOfFaceWithOneConvexEdge(vertices, index, vertexToNextPositionInConvexEdge.second)
         else
            vertices
   }
   /**
    * TThe newly inserted vertex will be the second element on the list returned.
    */
   private fun makeVerticesOfFaceWithOneConvexEdge(vertices: List<Vertex>, indexOfVertexThatPrecedesTheVertexToInsert: Int,
                                                   positionOfVertexToInsert: Point):
           List<Vertex> {
      val vertexToInsert = Vertex(positionOfVertexToInsert)
      val newHead = listOf(vertices[indexOfVertexThatPrecedesTheVertexToInsert], vertexToInsert)
      return newHead + when (indexOfVertexThatPrecedesTheVertexToInsert) {
         0 -> vertices.drop(1)
         vertices.lastIndex -> vertices.dropLast(1)
         else -> vertices.drop(indexOfVertexThatPrecedesTheVertexToInsert + 1) + vertices.take(indexOfVertexThatPrecedesTheVertexToInsert)
      }
   }
}