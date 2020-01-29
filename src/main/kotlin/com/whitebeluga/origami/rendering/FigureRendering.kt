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

import com.moduleforge.libraries.geometry._3d.Vector
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.loosen.FigureLoosener.loosen
import com.whitebeluga.origami.rendering.layeredrendering.FigureLayerRenderer
import com.whitebeluga.origami.rendering.layeredrendering.LayeredRendering
import com.whitebeluga.origami.rendering.layeredrendering.threedforbabylonjs.FigureThreeDimensionalLayerRenderer

/**
 * Given a 'Figure' object, which comprises a set of faces of what would be an ideal figure, to create an object
 * that can be visualized on a graphical interface, we need to do some transformations.
 *
 * A JSDisplayableFigure, whether flat, layered or 3D, is composed of a set of one sided faces of a particular color.
 * A one sided polygon is a polygon that only can be seen from one of the sides.
 *
 * Flat rendering:
 * Let's say that we have made some regular, linear folds on a sheet of paper. In theory, all the bundles of paper
 * occupy the same plane and therefore we can render the visible part of each face. The resulting mosaic is what
 * we call a flat rendering.
 *
 * At the moment the flat rendering is not being used because what I'm using in the web application can't handle
 * displaying these concave polygons (plus other problems).
 *
 * Layered rendering:
 * Similar to the flat rendering but displaying the whole face. As the name implies, the faces are rendered in layers.
 * There are some advantages to the layered rendering: the layers make it looks a slightly three dimensional while still
 * allowing the same precision in defining folds.
 *
 * 3D rendering:
 * The 3D rendering is a little more complicated, as we have to 'open up' the figure to make it look realistic.
 *
 */
object FigureRendering {
   /**
    * In the layered rendering
    * the boundaries are slightly shifted from the plane of the flat figure bundles for display, since in some GUIs
    * it can cause
    * trouble when they share the exact same space, even if they can be represented with only one side,
    * for example the borders can cause trouble and show through to the other side.
    *
    * There are also some positive reasons to prefer a layered rendering. It is not just a compromise for a
    * deficient GUI library.
    */
   @JvmStatic fun layeredRendering(figure: Figure, pushLayersApart: Double): JSDisplayableFigure {
      val renderer = FigureLayerRenderer(figure, pushLayersApart)
      return layeredRendering(renderer)
   }
   private fun layeredRendering(renderer: FigureLayerRenderer): JSDisplayableFigure {
      val polygons = renderer.layeredRendering()
      return makeDisplayableFigure(polygons)
   }
   private fun makeDisplayableFigure(layeredRendering: LayeredRendering): JSDisplayableFigure {
      val polygons = layeredRendering.polygons
      return JSDisplayableFigure(polygons, layeredRendering.originalFaceToPolygons)
   }
   fun push(pol: OneSidedPolygon, direction: Vector, pushDistance: Double, layerSeparation: Double): OneSidedPolygon {
      val pushingVector = Vector(direction).normalize().scale(pushDistance)
      val pushedPoints = pol.boundary.map { it.translate(pushingVector) }
      val creasePushingVector = Vector(direction).normalize().scale(pushDistance + layerSeparation)
      val pushedCreases = pol.creases.map { creasePoints ->
            creasePoints.map { it.translate(creasePushingVector) }
         }.toSet()
      return OneSidedPolygon(pushedPoints, pol.color, pushedCreases)
   }
   /**
    * This method tries to open up the figure and make it look three dimensional.
    *
    * It relies partially on layered rendering for those parts that cannot be properly opened.
    *
    * Therefore, for the layer rendering a separation between layers is needed.
    *
    * Admittedly this is a requirement that has crept up from the web application library used, where a minimum
    * separation is needed for the polygons to be correctly displayed.
    *
    * However there are also a few good reasons to prefer a layered rendering over a flat one and there is barely any
    * downside.
    *
    * After a fold, a figure might be centered, so that its center (however you want to define it) remains the same.
    *
    * In the 3D rendering a centering could also be part of the algorithm. However the figure is only slightly opened
    * and centering it wouldn't improve the way it looks to the GUI user, so the result rendering is returned uncentered.
    */
	@JvmStatic fun threeDRendering(figure: Figure, pushLayersApart: Double): JSDisplayableFigure {
      val loosened = loosen(figure)
      val renderer = FigureThreeDimensionalLayerRenderer(loosened, pushLayersApart)
      return layeredRendering(renderer)
	}
}
