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

import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.Double.Companion.MIN_VALUE

class BundleLayerRendererTest {
   private val separation = 1.0
   private val halfSeparation = 0.5
   private lateinit var xyPlane_NormalTowardsZPositive: Plane
   private lateinit var square: Face
   private lateinit var squareBundle: Bundle

   @Before
   fun setUp(){
      val antiClockwisePointsAsSeenFromZPositive = listOf(Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = Plane.planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive)
      val bottomLeft = Vertex(0.0, 0.0, 0.0)
      val bottomRight = Vertex(1.0, 0.0, 0.0)
      val topRight = Vertex(1.0, 1.0, 0.0)
      val topLeft = Vertex(0.0, 1.0, 0.0)
      val anticlockwiseFromZPos = listOf(bottomLeft, bottomRight, topRight, topLeft)
      square = Face(anticlockwiseFromZPos)
      squareBundle = Bundle(xyPlane_NormalTowardsZPositive, square)
   }
   @Test
   fun calculateLayerDistance_WhenOneLayer_ShouldReturnListOfZero(){
      //it doesn't matter with what bundle we create the renderer, only separation matters
      val renderer = BundleLayeredRenderer(squareBundle, separation)
      val distances = renderer.calculateDistances(1)
      assertThat(distances.size, `is`(1))
      val distance = distances.first()
      assertEquals(0.0, distance, MIN_VALUE)
   }
   @Test
   fun whenRenderingBundleOfOneFace_LayeredRenderingShouldReturnAMapOfTheFaceToTwoPolygons() {
      val renderer = BundleLayeredRenderer(squareBundle, separation)
      val rendered = renderer.layeredRendering()
      val map = rendered.originalFaceToPolygons
      assertThat(map.keys, `is`(setOf(square)))
      val polygons = map.values.first()
      assertThat(polygons.size, `is`(2))
   }
   @Test
   fun calculateLayerDistance_WhenTwoLayers_Calculated(){
      //it doesn't matter with what bundle we create the renderer, only separation matters
      val renderer = BundleLayeredRenderer(squareBundle, separation)
      val distances = renderer.calculateDistances(2)
      assertThat(distances.size, `is`(2))
      val first = distances.first()
      assertEquals(-halfSeparation, first, MIN_VALUE)
      val second = distances[1]
      assertEquals(halfSeparation, second, MIN_VALUE)
   }
   @Test
   fun calculateLayerDistance_WhenThreeLayers_Calculated(){
      //it doesn't matter with what bundle we create the renderer, only separation matters
      val renderer = BundleLayeredRenderer(squareBundle, separation)
      val distances = renderer.calculateDistances(3)
      assertThat(distances.size, `is`(3))
      val first = distances.first()
      assertEquals(-separation, first, MIN_VALUE)
      val second = distances[1]
      assertEquals(0.0, second, MIN_VALUE)
      val third = distances[2]
      assertEquals(separation, third, MIN_VALUE)
   }
   @Test
   fun calculateLayerDistance_WhenFourLayers_Calculated(){
      //it doesn't matter with what bundle we create the renderer, only separation matters
      val renderer = BundleLayeredRenderer(squareBundle, separation)
      val distances = renderer.calculateDistances(4)
      assertThat(distances.size, `is`(4))
      val first = distances.first()
      assertEquals(-separation - halfSeparation, first, MIN_VALUE)
      val second = distances[1]
      assertEquals(-halfSeparation, second, MIN_VALUE)
      val third = distances[2]
      assertEquals(halfSeparation, third, MIN_VALUE)
      val fourth = distances[3]
      assertEquals(separation + halfSeparation, fourth, MIN_VALUE)
   }
}