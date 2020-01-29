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
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class OpenableCornerPositionCalculatorTest {
   private lateinit var xyPlane_NormalTowardsZPositive: Plane

   @Before
   fun setUp(){
      val antiClockwisePointsAsSeenFromZPositive =
              listOf(Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = Plane.planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive)
   }
   /**
    * An example with two faces, each of them have free adjacent edges and are liable to be classified as
    * "openable corners"
    */
   @Test
   fun simpleExample_Calculated() {
      val bottomLeft = Vertex(0.0, 0.0, 0.0)
      val bottomRight = Vertex(1.0, 0.0, 0.0)
      val topRight = Vertex(1.0, 1.0, 0.0)
      val nearTopRight = Vertex(0.9, 0.9, 0.0)
      val topLeft = Vertex(0.0, 1.0, 0.0)
      val face1 = Face(listOf(bottomLeft, bottomRight, topRight, topLeft))
      val face2 = Face(listOf(nearTopRight, bottomRight, bottomLeft, topLeft))
      val bundle = Bundle(xyPlane_NormalTowardsZPositive,
              setOf(face1, face2),
              mapOf(face1 to setOf(face2)) )
      val figure = Figure(bundle)
      val calculator = OpenableCornerPositionCalculator(figure, bundle)
      val openCorners = calculator.calculateOpenCorners()
      assertThat(openCorners.size, `is`(2))
   }
}