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

import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.rendering.layeredrendering.FigureLayerRenderer
import com.whitebeluga.origami.rendering.layeredrendering.LayeredRendering

/**
 * It doesn't only create the layers, it also slightly opens up corners (see definition of corner)
 * for a more realistic 3D look.
 *
 * This might seem like it should belong to the Loosen project.
 *
 * In fact the loosening algorithms are devoted to create a version of the figure where the bundles are opened
 * into new bundles (think of articulated bundles or the slightly opened flap in the fish base for example).
 *
 * The output of the loosening algorithms is a valid figure.
 *
 * In this case slightly opening up corners of faces by deforming them is a tweak that produces a set of deformed faces.
 * This is a final step, there is no more adjustments, as manipulating a set of faces lacking the structure information
 * that a Figure object has is practically impossible.
 *
 */
internal class FigureThreeDimensionalLayerRenderer(private val figure: Figure, layerSeparation: Double):
        FigureLayerRenderer(figure, layerSeparation) {
   override fun makePolygonsOfBundle(bundle: Bundle): LayeredRendering {
      val bundleRenderer = BundleThreeDimensionalLayeredRenderer(figure, bundle, layerSeparation)
      return bundleRenderer.layeredRendering()
   }
}