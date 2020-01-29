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

import com.google.common.annotations.VisibleForTesting
import com.moduleforge.libraries.geometry.Geometry.almostZero
import com.moduleforge.util.Util.addNewValueToEntryOfMap
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.rendering.FigureRendering
import com.whitebeluga.origami.rendering.OneSidedPolygon
import com.whitebeluga.origami.rendering.OneSidedPolygon.Companion.makeBackFaceWithCreasesFromFace
import com.whitebeluga.origami.rendering.OneSidedPolygon.Companion.makeFrontFaceWithCreasesFromFace
import com.whitebeluga.origami.rendering.layeredrendering.Layer.Companion.emptyLayer
import com.whitebeluga.origami.rendering.layeredrendering.SideToRender.*
import kotlin.math.absoluteValue

open class BundleLayeredRenderer(protected val bundle: Bundle, private val layerSeparation: Double) {
   protected val faceCount = bundle.faces.size
   private val halfLayerSeparation = layerSeparation/2.0

   internal fun layeredRendering(): LayeredRendering {
      val layers = LayerMaker().makeBottomToTopLayers()
      val distances = calculateDistances(layers.size)
      val originalToPolygons = mutableMapOf<Face, MutableSet<OneSidedPolygon>>()
      val polygons = mutableSetOf<OneSidedPolygon>()
      for((index, layer) in layers.withIndex()) {
         val distance = distances[index]
         updateCollections(layer.faceToSideToRender, distance, polygons, originalToPolygons)
      }
      return LayeredRendering(polygons, originalToPolygons)
   }
   /**
    * The values returned are the offset of the middle point of the layer with respect to the plane of the (flat)
    * original bundle.
    */
   @VisibleForTesting
   internal fun calculateDistances(layerCount: Int): List<Double> {
      if(layerCount == 1)
         return listOf(0.0)
      if(layerCount == 2)
         return listOf(-halfLayerSeparation, halfLayerSeparation)
      if(layerCount == 3)
         return listOf(-layerSeparation, 0.0, layerSeparation)
      return if(layerCount % 2 == 0)
            calculateDistancesForAnEvenNumberOfLayers(layerCount)
         else
            calculateDistancesForAnOddNumberOfLayers(layerCount)
   }
   private fun calculateDistancesForAnOddNumberOfLayers(layerCount: Int): List<Double> {
      val half = (layerCount - 1) / 2
      val negHalf = ((-half)..-1).map { it.toDouble() * layerSeparation }
      val posHalf = (1..half).map { it.toDouble() * layerSeparation }
      return negHalf + 0.0 + posHalf
   }
   private fun calculateDistancesForAnEvenNumberOfLayers(layerCount: Int): List<Double> {
      val half = layerCount / 2
      val negHalf = ((-half)..-1).map { (it.toDouble() * layerSeparation) + halfLayerSeparation }
      val posHalf = (1..half).map { (it.toDouble() * layerSeparation) - halfLayerSeparation }
      return negHalf + posHalf
   }
   private fun updateCollections(faceToSideToRender: Map<Face, SideToRender>, distance: Double,
                         polygons: MutableSet<OneSidedPolygon>,
                         faceToPolygons: MutableMap<Face, MutableSet<OneSidedPolygon>>) =
           faceToSideToRender.forEach { (face, sideToRender) ->
              if(sideToRender == BOTH) {
                 updateCollections(face, TOP, distance, polygons, faceToPolygons)
                 updateCollections(face, BOTTOM, distance, polygons, faceToPolygons)
              } else {
                 updateCollections(face, sideToRender, distance, polygons, faceToPolygons)
              }
           }
   private fun updateCollections(face: Face, sideToRender: SideToRender, distance: Double,
                         polygons: MutableSet<OneSidedPolygon>,
                         faceToPolygons: MutableMap<Face, MutableSet<OneSidedPolygon>>) {
      val polygon = makePolygon(face, sideToRender, distance)
      polygons.add(polygon)
      addNewValueToEntryOfMap(faceToPolygons, face, polygon)
   }
   private fun makePolygon(face: Face, sideToRender: SideToRender, distance: Double): OneSidedPolygon {
      val pol = makePolygon(face, sideToRender)
      return pushPolygon(pol, sideToRender, distance)
   }
   protected open fun makePolygon(face: Face, sideToRender: SideToRender): OneSidedPolygon {
      val polygonAndBundleFaceTheSameDirection = face.polygonPlane.approximatelyFacingTheSameWay(bundle.plane)
      return if(polygonAndBundleFaceTheSameDirection)
            if(sideToRender == TOP)
               makeFrontFaceWithCreasesFromFace(face)
            else
               makeBackFaceWithCreasesFromFace(face)
         else
            if(sideToRender == TOP)
               makeBackFaceWithCreasesFromFace(face)
            else
               makeFrontFaceWithCreasesFromFace(face)
   }
   private fun pushPolygon(polygon: OneSidedPolygon, sideToRender: SideToRender, distance: Double): OneSidedPolygon {
      val pushingVector = if(almostZero(distance)) {
            if(sideToRender == TOP)
               bundle.upwards
            else
               bundle.downwards
         } else {
            if(distance > 0.0)
               bundle.upwards
            else
               bundle.downwards
         }
      val adjustedDistance = adjustDistance(sideToRender, distance)
      return FigureRendering.push(polygon, pushingVector, adjustedDistance.absoluteValue, layerSeparation)
   }
   /**
    *
    */
   private fun adjustDistance(sideToRender: SideToRender, distance: Double): Double =
      if(sideToRender == BOTTOM)
            distance - halfLayerSeparation
         else
            distance + halfLayerSeparation
   inner class LayerMaker {
      /**
       * Returns a list of layers that include only the faces that are totally or partially visible from either side
       * of the bundle (or both sides).
       *
       * The first layer of the list returned corresponds to the faces at the bottom of the bundle.
       */
      fun makeBottomToTopLayers(): List<Layer> =
         makeBottomToTopLayers_Recursive(emptySet(), emptyList())
      /*
      The face set parameter includes all faces in the list of layers, plus those which would belong
      to one of those layers but are hidden and thus, need not be rendered.
       */
      private fun makeBottomToTopLayers_Recursive(alreadyAdded: Set<Face>, layers: List<Layer>): List<Layer> {
         val areAllFacesAlreadyAdded =  faceCount == alreadyAdded.size
         if (areAllFacesAlreadyAdded)
            return layers
         val facesOfNewLayer = findFacesOfNewLayer(alreadyAdded)
         val facesOfNewLayerToRender = facesOfNewLayer.filterValues { it != NONE }
         val thereIsAtLeastAVisibleFace = facesOfNewLayerToRender.isNotEmpty()
         val newLayer = if (thereIsAtLeastAVisibleFace) Layer(facesOfNewLayerToRender) else emptyLayer()
         val updatedLayerList = layers + newLayer
         val allFacesOfNewLayer = facesOfNewLayer.keys
         return makeBottomToTopLayers_Recursive(alreadyAdded + allFacesOfNewLayer, updatedLayerList)
      }
      /**
       * Returns a map with two entries: one for the faces that are completely hidden and are not to be rendered
       * another for the faces that are not completely hidden (on either side) and should be rendered.
       *
       * The returned map always contains both entries
       */
      private fun findFacesOfNewLayer(alreadyAdded: Set<Face>): Map<Face, SideToRender> {
         val result: MutableMap<Face, SideToRender> = mutableMapOf()
         val notYetAdded = bundle.faces - alreadyAdded
         for (face in notYetAdded) {
            val facesBelow = bundle.facesToFacesBelow[face]
            val faceCanBeAddedToNewLayer = facesBelow == null || alreadyAdded.containsAll(facesBelow)
            if (faceCanBeAddedToNewLayer) {
               val facesAbove = bundle.facesToFacesAbove[face]
               val hiddenFromAbove = if (facesAbove == null) false else face.isHiddenBy(facesAbove)
               val hiddenFromBelow = if (facesBelow == null) false else face.isHiddenBy(facesBelow)
               result[face] = when {
                  //this is a heuristic: because the bottom and top faces are likely to be later
                  //deformed in order to open them up, we should render both sides of them.
                  bundle.topFaces().contains(face) -> BOTH
                  bundle.bottomFaces().contains(face) -> BOTH
                  hiddenFromAbove && hiddenFromBelow -> NONE
                  hiddenFromAbove -> BOTTOM
                  hiddenFromBelow -> TOP
                  else -> BOTH
               }

            }
         }
         return result
      }
   }
}