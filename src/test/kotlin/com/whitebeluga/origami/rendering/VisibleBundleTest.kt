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

package com.whitebeluga.origami.rendering

import com.moduleforge.libraries.geometry._3d.LineSegment
import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Plane.planeFromOrderedPoints
import com.moduleforge.libraries.geometry._3d.Point
import com.moduleforge.libraries.geometry._3d.Polygon
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.core.Is.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.locationtech.jts.geom.Coordinate as JTSCoordinate
import org.locationtech.jts.geom.Polygon as JTSPolygon

/**
 * A fixture to test polygon visibility methods in Blunder
 */
class VisibleBundleTest {
   private lateinit var xyPlane_NormalTowardsZPositive: Plane
   private lateinit var xyPlane_NormalTowardsZNegative: Plane
   private lateinit var faceInXY: Face

   private lateinit var bundle: Bundle
   private lateinit var visibleBundle: VisibleBundle

   private lateinit var bundleWithTwoLayersNoOverlapping: Bundle
   private lateinit var bundleWithTwoLayersPartialOverlapping: Bundle

   private lateinit var topCoversBottom: Bundle
   private lateinit var bottomCoversTop: Bundle

   private lateinit var visibleBundleWithTwoLayersNoOverlapping: VisibleBundle
   private lateinit var visibleStackWithTwoLayersPartialOverlapping: VisibleBundle
   private lateinit var visibleTopCoversBottom: VisibleBundle
   private lateinit var visibleBottomCoversTop: VisibleBundle

   @Before
   fun setUp(){
      // anticlockwise boundary for observer on Z+
      val planePoints = listOf( Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = planeFromOrderedPoints(planePoints)
      xyPlane_NormalTowardsZNegative = planeFromOrderedPoints(planePoints.reversed())
      faceInXY = Face(Vertex(1.0, 1.0, 0.0), Vertex(2.0, 1.0, 0.0), Vertex(2.0, 2.0, 0.0))
      bundle = Bundle(xyPlane_NormalTowardsZPositive, faceInXY)
      visibleBundle = VisibleBundle(bundle)
      bundleWithTwoLayersNoOverlapping = Bundle(xyPlane_NormalTowardsZPositive,
         faces = setOf(
                 Face(Vertex(0.0, 0.0, 0.0), Vertex(1.0, 0.0, 0.0), Vertex(1.0, 1.0, 0.0)),
                 Face(Vertex(10.0, 10.0, 0.0), Vertex(20.0, 10.0, 0.0), Vertex(20.0, 20.0, 0.0))),
         facesToFacesAbove = emptyMap() )
      visibleBundleWithTwoLayersNoOverlapping = VisibleBundle(bundleWithTwoLayersNoOverlapping)


      val bottomFace = Face(Vertex(0.0, 0.0, 0.0), Vertex(2.0, 0.0, 0.0), Vertex(2.0, 2.0, 0.0))
      val topFace = Face(Vertex(1.0, 1.0, 0.0), Vertex(3.0, 1.0, 0.0), Vertex(3.0, 3.0, 0.0))
      bundleWithTwoLayersPartialOverlapping = Bundle(xyPlane_NormalTowardsZPositive,
              faces = setOf( bottomFace, topFace),
              facesToFacesAbove = mapOf(bottomFace to setOf(topFace)))
      visibleStackWithTwoLayersPartialOverlapping = VisibleBundle(bundleWithTwoLayersPartialOverlapping)

      val coveredBottomFace = Face(Vertex(0.0, 0.0, 0.0), Vertex(2.0, 0.0, 0.0), Vertex(2.0, 2.0, 0.0))
      val coveringTopFace = Face(Vertex(0.0, 0.0, 0.0), Vertex(3.0, 0.0, 0.0), Vertex(3.0, 3.0, 0.0))
      topCoversBottom = Bundle(xyPlane_NormalTowardsZPositive,
              faces = setOf( coveredBottomFace, coveringTopFace),
              facesToFacesAbove = mapOf(coveredBottomFace to setOf(coveringTopFace)))
      visibleTopCoversBottom = VisibleBundle(topCoversBottom)

      val coveredTopFace = Face(Vertex(0.0, 0.0, 0.0), Vertex(2.0, 0.0, 0.0), Vertex(2.0, 2.0, 0.0))
      val coveringBottomFace = Face(Vertex(0.0, 0.0, 0.0), Vertex(3.0, 0.0, 0.0), Vertex(3.0, 3.0, 0.0))
      bottomCoversTop = Bundle(xyPlane_NormalTowardsZPositive,
              faces = setOf( coveredTopFace, coveringBottomFace),
              facesToFacesAbove = mapOf(coveringBottomFace to setOf(coveredTopFace)))
      visibleBottomCoversTop = VisibleBundle(bottomCoversTop)
   }
   @Test
   fun bundleWithOneFace_FaceShouldRemainVisible(){
      val visibleFacesPerFace = 2
      val faceCount = 1
      val expectedVisibleFaces =  visibleFacesPerFace * faceCount
      assertThat(visibleBundle.visiblePolygons.size, `is`(expectedVisibleFaces))
      //same number of vertices
      assertEquals(visibleBundle.visiblePolygons.first().boundary.size, faceInXY.vertices.size)
   }
   /**
    * This scenario (two disconnected non overlapping faces) is, obviously, not possible in an origami and an error
    * could result, depending on what we control
    * on the input parameters therefore we might have to change the test somehow in the future
    */
   @Test
   fun bundleWithTwoNonOverlappingFaces_EveryVertexOfVisibleFacesMatchesThePositionOfSomeVertexOfTheOriginalFace(){
      val visibleFacesPerFace = 2
      val faceCount = 2
      //compare polygon count
      val expectedVisibleFaces =  visibleFacesPerFace * faceCount
      assertThat(visibleBundleWithTwoLayersNoOverlapping.visiblePolygons.size, `is`(expectedVisibleFaces) )
      //compare boundary of the bundles and boundary of the visible polygons
      val allVertices = bundleWithTwoLayersNoOverlapping.faces.flatMap { it.vertices }.toSet()
      visibleBundleWithTwoLayersNoOverlapping.visiblePolygons.forEach { polygon ->
         polygon.boundary.forEach { point ->
            assertTrue(allVertices.any { it.epsilonEquals(point) }) }
      }
   }
   @Test
   fun stackWithTwoLayersOnePolygonInEachPartialOverlapping_Calculated(){
      /*
      You can represent the following in https://technology.cpm.org/general/3dgraph/

      The following two triangles partially intersect, but there are only two visible polygons per side
      ie a triangle doesn't separate the other into two or more visible parts.
      */
      //plane equation: 4x - 7y + 4z = 25
      val bottomFacePoints =
               listOf ( Point(1, 1, 7), Point(2, 2, 7.75), Point(0, 2, 9.75))
      val plane = planeFromOrderedPoints(bottomFacePoints)
      val bottomFace = Face(bottomFacePoints.map { Vertex(it) } )
      val topFace = Face(
              listOf ( Vertex(1.5, 1.5, 7.375), Vertex(3.0, 3.0, 8.5), Vertex(2.0, 3.0, 9.5)) )
      val bundle = Bundle(plane, setOf(bottomFace, topFace), mapOf(bottomFace to setOf(topFace)))
//      //two faces per polygon => total = four faces
      val visibleFacePerFace = 2
      assertThat(VisibleBundle(bundle).visiblePolygons.size, `is`(visibleFacePerFace * bundle.faces.size))
      //there are two boundary where the edges of the polygons intersect. One is, of course,
      //(1.5, 1.5, 7.375). The other is calculated here:
      val intersectionSegment1 = LineSegment(Point(1.5, 1.5, 7.375), Point(2, 3, 9.5))
      val intersectionSegment2 = LineSegment(Point(0, 2, 9.75), Point(2, 2, 7.75))
      val intersection = intersectionSegment1.intersectionPoint(intersectionSegment2)!!

      val visiblePolygonsFromTop =
              VisibleBundle(bundle).visiblePolygonsFromTop.map { Polygon(it.boundary)}
      val expectedPartiallyCoveredPolygonFromTop =
              Polygon(listOf ( Point(1, 1, 7), Point(1.5, 1.5, 7.375), intersection, Point(0, 2, 9.75)))
      assertTrue(visiblePolygonsFromTop.any { pol -> pol.areaMatches(expectedPartiallyCoveredPolygonFromTop) })

      // the other visible face from the top is of course, the top face itself
      assertTrue(visiblePolygonsFromTop.any { pol -> pol.areaMatches(topFace) })
      val visiblePolygonsFromBottom =
              VisibleBundle(bundle).visiblePolygonsFromBottom.map { Polygon(it.boundary)}
      val expectedPartiallyCoveredPolygonFromBottom =
              Polygon(listOf (Point(2, 2, 7.75), Point(3, 3, 8.5), Point(2, 3, 9.5), intersection))
      assertTrue(visiblePolygonsFromBottom.any { pol -> pol.areaMatches(expectedPartiallyCoveredPolygonFromBottom) })
      //as you would expect, the bottom face should be completely visible from the bottom
      assertTrue(visiblePolygonsFromBottom.any { pol -> pol.areaMatches(bottomFace) })
   }
}