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

import com.moduleforge.libraries.geometry._3d.Point
import com.moduleforge.libraries.geometry._3d.Vector
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.folding.linefolding.LineFoldParameters

internal object Frames {
   private const val DEFAULT_FRAME_COUNT = 10
   @JvmStatic
   @JvmOverloads
   fun ffLineFoldFrames(figure: Figure, foldParameters: LineFoldParameters, snapshotCount: Int = DEFAULT_FRAME_COUNT):
           List<Figure> {
      val foldParametersList= foldParameterList(foldParameters, snapshotCount)
      return foldParametersList.map { it -> figure.doFirstFlapLineFold(it) }
   }
   fun amapLineFoldFrames(figure: Figure, foldParameters: LineFoldParameters, snapshotCount: Int = DEFAULT_FRAME_COUNT):
           List<Figure> {
      val foldParametersList= foldParameterList(foldParameters, snapshotCount)
      return foldParametersList.map { it -> figure.doAMAPLineFold(it) }
   }
   private fun foldParameterList(foldParameters: LineFoldParameters, snapshotCount: Int): List<LineFoldParameters> {
      val angleIncrement = foldParameters.angle / snapshotCount
      val angles: List<Double> = (1..snapshotCount).map { it * angleIncrement }
      return angles.map { it -> foldParameters.copy(angle = it) }
   }
   @JvmStatic
   @JvmOverloads
   fun translationFrames(figure: Figure, newCenter: Point=Point(0, 0, 0), frameCount: Int = DEFAULT_FRAME_COUNT):
           List<Figure> {
      val origin = figure.center
      val adjustVector = origin.vectorTo(newCenter)
      if(origin.epsilonEquals(newCenter))
         return listOf(figure)
      fun calculateTranslationVectorForFrame(translationVector: Vector, frameNumber: Int): Vector =
              translationVector.scale(frameNumber.toDouble() / frameCount.toDouble())
      val frameCenterList = (1..frameCount).map {frame ->
         origin.translate(calculateTranslationVectorForFrame(adjustVector, frame)) }
      return frameCenterList.map { point -> figure.translated(point) }
   }
}
