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
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import com.whitebeluga.origami.rendering.OneSidedPolygon
import com.whitebeluga.origami.rendering.layeredrendering.BundleLayeredRenderer
import com.whitebeluga.origami.rendering.layeredrendering.SideToRender

/**
 * This is a variation of the layered rendering, where in addition to layers, a series of modifications to the faces
 * are done for an enhanced 3D aspect.
 *
 * Most, if not all, of these modifications involve deforming the faces -making them non-planar-.
 *
 * As explained elsewhere in the project
 * this is very relevant to the graphical library used in the GUI.
 * In general we cannot expect such library to render
 * correctly a deformed face. At least not without some further processing, such as triangulating.
 *
 * The libarary we are currently using, babylon js, can straight out render non-planar faces. So it's fair to say
 * that this choice of library has "leaked" to the polygon objects produced in this module.
 *
 * It is also fair to say that non-planar faces are the best choice to achieve a realistic 3D look, and in case that
 * some graphical library cannot render them, they could just triangulate them first.
 *
 * Some deformations that are done to the faces are corner opening or "pulling" and edge convexing.
 *
 * What does it mean that a corner can be opened?
 *
 * We have the following heuristic to try to achieve a more realistic 3-D look:
 *
 * For faces that have no other faces on top of it outwards, that is, either bottom or top faces of the bundle, if they
 * also have exactly
 * two free adjacent edges, that is two edges that are not connected to any other faces, and these two edges are also
 * adjacent between them and adjacent to a non-free edge each, then those corners (typically zero or one, per face,
 * but there could be more). are pulled outwards.
 *
 * It is important to note that the faces from which corners have been pulled are DEFORMED. The opened corner is not
 * on the same plane as the rest of the vertices.
 *
 * We choose to create such a deformed polygon because it works on the current front end application. Bear in mind
 * that if you decide to use a different rendering library, a different result, such as turning the opened corner
 * into a triangle might be preferable.
 *
 * It's certainly unrealistic to expect any given library to display deformed faces.
 *
 * Take the case of the piece of shit babylon js. It is supposed to be a 3D rendenring library and it cannot fucking
 * display a fucking concave polygon.
 */
class BundleThreeDimensionalLayeredRenderer(figure: Figure, bundle: Bundle, layerSeparation: Double):
        BundleLayeredRenderer(bundle, layerSeparation) {
   private val vertexToOpenCorner: Map<Vertex, Point>
   /**
   In an edge the vertices are returned as a list for convenience, but two edges with the same ends
   are equal to each other, irrespective of their order.

   Therefore it's not convenient to map an edge with new points would make a convex curve of that
   edge, since we would have to calculate the direction of these inner points.

   Instead, we map a vertex with a list of new positions that go directly behind them to create a
   convex edge and that goes in the direction of the points of the face, which is far more convenient.

    The face has to be included as part of the key since a vertex can belong to two faces.

    For simplicity the value is a pair of vertex, point instead of the more precise
    map of vertex to list of points.
    */
   private val vertexToNextVerticesInConvexEdge: Map<Face, Pair<Vertex, Point>>

   init {
      val openableCornerCalculator = OpenableCornerPositionCalculator(figure, bundle)
      vertexToOpenCorner = openableCornerCalculator.calculateOpenCorners()
      val convexEdgeCalculator = ConvexEdgeCalculator(figure, bundle)
      vertexToNextVerticesInConvexEdge = convexEdgeCalculator.calculateConvexEdges()
   }
   override fun makePolygon(face: Face, sideToRender: SideToRender): OneSidedPolygon =
      FaceTweaker.makePolygon(face, sideToRender, vertexToOpenCorner,
              vertexToNextVerticesInConvexEdge[face],
              bundle.plane)
}