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

import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.rendering.layeredrendering.SideToRender.NONE

/**
 * A layer is a set of faces in a bundle that do not overlap.
 *
 * The side to render is with respect to the bundle (not the polygon). This is an arbitrary criterion.
 */
class Layer (val faceToSideToRender: Map<Face, SideToRender>) {
   val faces: Set<Face> = faceToSideToRender.keys
   init {
      //if a face is not visible at all it shouldn't be included
      assert(faceToSideToRender.values.none { it == NONE })
   }
   companion object {
      fun emptyLayer(): Layer = Layer(emptyMap())
   }
}