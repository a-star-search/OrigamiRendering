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

import com.moduleforge.libraries.geometry._3d.Polygon
import com.moduleforge.libraries.geometry._3d.Polygon.Companion.lineSegmentDifferenceWithPolygons
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Crease
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.rendering.OneSidedPolygon.Companion.makeBackFaceFrom
import com.whitebeluga.origami.rendering.OneSidedPolygon.Companion.makeFrontFaceFrom

/**
 * A visible bundle object contains two sets of polygons, one for each side (top and bottom) of a bundle.
 *
 * Each polygon of this object is the visible part of a face of this object, that is the part not covered by
 * other faces as seen from a particular side.
 *
 * This class is used as an "action" class, that is used to convert a bundle into a set of polygons, rather than as a
 * container to be used in other classes.
 */
class VisibleBundle  (bundle: Bundle){
   val visiblePolygonsFromTop: Set<OneSidedPolygon>
   val visiblePolygonsFromBottom: Set<OneSidedPolygon>
   val visiblePolygons: Set<OneSidedPolygon>
      get() = visiblePolygonsFromTop + visiblePolygonsFromBottom

   init {
      visiblePolygonsFromTop = visibleOneSidedPolygonsFromTop(bundle)
      visiblePolygonsFromBottom = visibleOneSidedPolygonsFromBottom(bundle)
   }
   companion object {
      private fun visibleOneSidedPolygonsFromTop(bundle: Bundle): Set<OneSidedPolygon> =
              visiblePolygonsFromTop(bundle).map {
                 if(it.isShowingFront(bundle.downwards)) makeFrontFaceFrom(it)
                 else makeBackFaceFrom(it) }.toSet()
      private fun visiblePolygonsFromTop(bundle: Bundle): Set<CreasedPolygon> {
         val visiblePolygons = mutableSetOf<CreasedPolygon>()
         for(face in bundle.faces) {
            val allFacesAbove = bundle.facesAbove(face)
            if(allFacesAbove.isEmpty()) {
               val creasedPolygon = CreasedPolygon.fromFace(face)
               visiblePolygons.add(creasedPolygon)
            } else {
               val differencePolygons = visiblePartsOfFaceWhenThereAreCoveringFaces(face, allFacesAbove)
               visiblePolygons.addAll(differencePolygons)
            }
         }
         return visiblePolygons
      }
      /**
       * The covering faces are the faces above, if the top side visibility is considered
       * or the faces below, if the bottom side.
       */
      private fun visiblePartsOfFaceWhenThereAreCoveringFaces(face: Face, coveringFaces: Set<Face>): List<CreasedPolygon> {
         val diff = face.difference(coveringFaces).toList()
         if (diff.isEmpty())
            return emptyList()
         val creasesParts = face.creases.flatMap {
               lineSegmentDifferenceWithPolygons(it, coveringFaces)
            }
         val creasesOfDifference = creasesParts.map {
               Crease.creaseFromPoints(it.endsAsList[0], it.endsAsList[1])
            }.toSet()
         //I'll just assign all creases to any one of the face parts, it doesn't make a difference
         val creased = CreasedPolygon(diff.first().vertices, face.colors, creasesOfDifference)
         val notCreased = diff.drop(1).map { CreasedPolygon(it.vertices, face.colors) }
         return notCreased + creased
      }
      private fun visibleOneSidedPolygonsFromBottom(stack: Bundle): Set<OneSidedPolygon> =
              visiblePolygonsFromBottom(stack).map {
                 if(it.isShowingFront(stack.upwards)) makeFrontFaceFrom(it)
                 else makeBackFaceFrom(it)
              }.toSet()
      private fun visiblePolygonsFromBottom(bundle: Bundle): Set<Polygon> {
         val visiblePolygons = mutableSetOf<Polygon>()
         for(face in bundle.faces) {
            val allFacesBelow = bundle.facesBelow(face)
            if(allFacesBelow.isEmpty()) {
               visiblePolygons.add(face)
            } else {
               val differencePolygons = visiblePartsOfFaceWhenThereAreCoveringFaces(face, allFacesBelow)
               visiblePolygons.addAll(differencePolygons)
            }
         }
         return visiblePolygons
      }
   }
}