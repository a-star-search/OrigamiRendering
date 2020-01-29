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

package com.whitebeluga.origami.rendering.layeredrendering

import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.rendering.OneSidedPolygon

/**
 * Layer rendering is similar to the flat rendering.
 *
 * Admittedly this algorithm is designed to create polygons that can be correctly displayed in the piece of shit
 * library babylon js which is part of the web application module. However, layer rendering has some important
 * advantages over a completely flat rendering and I believe it might even be preferable to be used by
 * applications using rendering libraries that don't have the shortcomings of babylon js, although perhaps,
 * some adjustments are needed.
 *
 * In short: keep in mind that this is a useful algorithm for flat rendering but the polygons that could be
 * produced for other libraries, could look slightly different (and better) so I could need a variation of this
 * algorithm in the future that won't be too different to this one. Basically the main difference would be the ability
 * to draw concave faces if needed
 *
 * In this rendering, a face may increase the width of the rendered bundle even if said face is hidden.
 * This is intentional. It keeps the rendering flat enough but allows a more realistic look.
 *
 * About babylon:
 * ---
 *
 * In the fucking piece of shit babylon we have the following shortcomings:
 * - It's simply not possible to draw a concave polygon! (it is only possible in the XZ plane).
 *  A completely flat rendering requires concave polygons. Luckily in origami, starting from a square paper,
 *  only convex faces are ever produced. As long as we display the whole face, it can be rendered by babylon.
 *
 *  For that reason this algorithm returns whole faces (two polygons looking at opposite sides per face)
 *
 * - It is not possible to correctly draw two faces facing opposite sides together, since the borders of one
 *  show through to the other.
 *
 * - It is not feasible to break concave polygons into convex ones: not only it will show unwanted borders,
 * but the texture of the polygon will point to different directions.
 *
 * Advantage of a layered rendering:
 * ---
 *
 * A layered rendering is a compromise rendering with a two barreled purpose:
 *
 * It allows to visualize the figure in barely three dimensional way, which is a great help for the
 * folder.
 *
 * It is almost flat, which means that it's easy select folding points in a bundle using a slightly elongated
 * GUI element (such as a short and thin cylinder) and make it obvious that the fold may comprise several layers
 * that in a truly three dimensional view could be too far apart.
 *
 * Lastly, it is almost a necessary rendering. A truly three dimensional rendering is notoriously difficult to
 * achieve (apart from a few simple case that allows us to open up the figure in specific ways)
 *
 */
internal open class FigureLayerRenderer(private val figure: Figure, protected val layerSeparation: Double) {
   fun layeredRendering(): LayeredRendering = makePolygons()
   private fun makePolygons(): LayeredRendering {
      val polygons: MutableSet<OneSidedPolygon> = mutableSetOf()
      val originalFaceToPolygons: MutableMap<Face, Set<OneSidedPolygon>> = mutableMapOf()
      for(bundle in figure.bundles)
         addPolygonsOfBundle(bundle, polygons, originalFaceToPolygons)
      return LayeredRendering(polygons, originalFaceToPolygons)
   }
   private fun addPolygonsOfBundle(bundle: Bundle, polygons: MutableSet<OneSidedPolygon>,
                                   originalFaceToPolygons: MutableMap<Face, Set<OneSidedPolygon>>) {
      val layeredRendering = makePolygonsOfBundle(bundle)
      val bundlePolygons = layeredRendering.polygons
      polygons.addAll(bundlePolygons)
      //can just add all the entries, since there are no clashes between bundles
      originalFaceToPolygons.putAll(layeredRendering.originalFaceToPolygons)
   }
   protected open fun makePolygonsOfBundle(bundle: Bundle): LayeredRendering {
      val bundleRenderer = BundleLayeredRenderer(bundle, layerSeparation)
      return bundleRenderer.layeredRendering()
   }
}
