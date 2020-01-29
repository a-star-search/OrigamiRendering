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

import com.moduleforge.libraries.geometry.Geometry.epsilonEquals
import com.moduleforge.libraries.geometry._3d.ColorCombination
import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Plane.planeFromOrderedPoints
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.awt.Color.BLACK
import java.awt.Color.WHITE

class FigureLayerRendererTest {
   private val separation = 1.0
   private val halfSeparation = 0.5
   private lateinit var xyPlane_NormalTowardsZPositive: Plane
   private lateinit var monoChromaticSquare: Face
   private lateinit var whiteFrontBlackBackSquare_FrontTowardsZPos: Face
   private lateinit var whiteFrontBlackBackSquare_FrontTowardsZNeg: Face

   @Before
   fun setUp(){
      val antiClockwisePointsAsSeenFromZPositive = listOf(Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive)
      val bottomLeft = Vertex(0.0, 0.0, 0.0)
      val bottomRight = Vertex(1.0, 0.0, 0.0)
      val topRight = Vertex(1.0, 1.0, 0.0)
      val topLeft = Vertex(0.0, 1.0, 0.0)
      val anticlockwiseFromZPos = listOf(bottomLeft, bottomRight, topRight, topLeft)
      monoChromaticSquare = Face(anticlockwiseFromZPos, ColorCombination(WHITE, WHITE))
      /*
      if vertices are perceived as anticlockwise from z pos, then the front of the polygon faces z pos
       */
      whiteFrontBlackBackSquare_FrontTowardsZPos = Face(anticlockwiseFromZPos, ColorCombination(WHITE, BLACK))
      val clockwiseFromZPos = listOf(topLeft, topRight, bottomRight, bottomLeft)
      whiteFrontBlackBackSquare_FrontTowardsZNeg = Face(clockwiseFromZPos, ColorCombination(WHITE, BLACK))
   }

   @Test
   fun testLayerRendering_WhenSingleFaceFigure_Calculated() {
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, monoChromaticSquare)
      val figure = Figure(bundle)
      val renderer = FigureLayerRenderer(figure, separation)
      val rendered = renderer.layeredRendering()
      val polygons = rendered.polygons
      assertThat(polygons.size, `is`(2)) //two polygons in total (one face has two sides)
      val faceAtZNeg = polygons.first { it.boundary.first().z < 0 }
      faceAtZNeg.boundary.forEach {
         assertTrue(epsilonEquals(it.z, -halfSeparation)) // all z coordinates are at half separation
      }
      val faceAtZPos = polygons.first { it.boundary.first().z > 0 }
      faceAtZPos.boundary.forEach {
         assertTrue(epsilonEquals(it.z, halfSeparation)) // all z coordinates are at half separation
      }
   }
   /**
    * Similar figure but in this case we ensure that the front and back (identified by the colors) are
    * shifted to the correct direction.
    */
   @Test
   fun testLayerRendering_WhenSingleFaceAndBiChromaticFigureAndFrontFaceTowardsZPos_Calculated() {
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, whiteFrontBlackBackSquare_FrontTowardsZPos)
      val figure = Figure(bundle)
      val renderer = FigureLayerRenderer(figure, separation)
      val rendered = renderer.layeredRendering()
      val polygons = rendered.polygons

      val frontFace = polygons.first { it.boundary.first().z > 0 } //the one at z pos
      assertThat(frontFace.color, `is`(WHITE))

      val backFace = polygons.first { it.boundary.first().z < 0 } //the one at z neg
      assertThat(backFace.color, `is`(BLACK))
   }
   /**
    * Similar test but using a polygon whose front faces z neg.
    *
    * This is to ensure that the algorithm works for any configuration of bundle and polygon direction.
    */
   @Test
   fun testLayerRendering_WhenSingleFaceAndBiChromaticFigureAndFrontFaceTowardsZNeg_Calculated() {
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, whiteFrontBlackBackSquare_FrontTowardsZNeg)
      val figure = Figure(bundle)
      val renderer = FigureLayerRenderer(figure, separation)
      val rendered = renderer.layeredRendering()
      val polygons = rendered.polygons

      val frontFace = polygons.first { it.boundary.first().z < 0 } //the one at z neg
      assertThat(frontFace.color, `is`(WHITE))

      val backFace = polygons.first { it.boundary.first().z > 0 } //the one at z pos
      assertThat(backFace.color, `is`(BLACK))
   }
   /**
    * Probably the name is self explanatory but here is a more detailed description: two faces of the same
    * dimensions against each other.
    *

    * Four polygons (two sides per face) need to be rendered: the outer ones of the opposing faces.
    *
    * Remember that in the case of bottom and top faces both sides are rendered because these faces have a higher chance
    * of being deformed and opened and the inner faces get exposed.
    */
   @Test
   fun testLayerRendering_WhenTwoOpposingFacesExactlyMatch_FourPolygonsAreRendered() {
      val bottomLeft = Vertex(0.0, 0.0, 0.0)
      val bottomRight = Vertex(1.0, 0.0, 0.0)
      val topRight = Vertex(1.0, 1.0, 0.0)
      val topLeft = Vertex(0.0, 1.0, 0.0)
      val pointsA = listOf(bottomLeft, bottomRight, topRight, topLeft)
      val faceA = Face(pointsA)
      val pointsB = listOf(bottomLeft, bottomRight, Vertex(topRight), Vertex(topLeft))
      val faceB = Face(pointsB)
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, setOf(faceA, faceB), mapOf(faceA to setOf(faceB)) )
      val figure = Figure(bundle)
      val renderer = FigureLayerRenderer(figure, separation)
      val renderedFigure = renderer.layeredRendering()
      val polygons = renderedFigure.polygons
      assertThat(polygons.size, `is`(4))
      //maybe I should test that these two faces are the correct ones, but meh, takes too much work
   }
   /**
    * Similar to the previous test but also including another face in the middle of the two and completely hidden by
    * them on both sides.
    *
    * Four polygons (two sides per face) need to be rendered: the outer ones of the opposing faces.
    *
    * Remember that in the case of bottom and top faces both sides are rendered because these faces have a higher chance
    * of being deformed and opened and the inner faces get exposed.
    *
    * In this case there is one other difference, the faces are further separated because in our algorithm a face
    * increases the width of the rendered bundle even if said face is hidden. This is intentional.
    */
   @Test
   fun testLayerRendering_WhenTwoOpposingFacesExactlyMatch_AndAnotherFaceIsHiddenOnBothSides_FourPolygonsAreRendered() {
      val bottomLeft = Vertex(0.0, 0.0, 0.0)
      val bottomRight = Vertex(1.0, 0.0, 0.0)
      val topRight = Vertex(1.0, 1.0, 0.0)
      val topLeft = Vertex(0.0, 1.0, 0.0)
      val pointsA = listOf(bottomLeft, bottomRight, topRight, topLeft)
      val faceA = Face(pointsA)
      val pointsB = listOf(bottomLeft, bottomRight, Vertex(topRight), Vertex(topLeft))
      val faceB = Face(pointsB)
      val hidden = Face(listOf(topRight, topLeft, Vertex(0.5, 0.5, 0.0)))
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, setOf(faceA, faceB, hidden),
              mapOf(faceA to setOf(faceB, hidden), hidden to setOf(faceB)) )
      val figure = Figure(bundle)
      val renderer = FigureLayerRenderer(figure, separation)
      val renderedFigure = renderer.layeredRendering()
      val polygons = renderedFigure.polygons
      assertThat(polygons.size, `is`(4)) // again, only two faces need to be rendered
   }
}